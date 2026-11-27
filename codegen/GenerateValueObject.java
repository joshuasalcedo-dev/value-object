import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates {@code src/main/java/io/joshuasalcedo/valueobject/core/ValueObject.java}: the marker interface, with
 * <ul>
 *   <li>{@code ValueObject.Type}: an enum of every value object in the library,</li>
 *   <li>{@code ValueObject.Parse}: one {@code Parse.Money(Object)} per type, built from the type's own
 *       single-argument factories ({@code parse}, {@code of}, {@code from...}, its record constructor),</li>
 *   <li>{@code ValueObject.Validate}: one {@code Check} per type, with {@code is.<VALUE>} rules for enum
 *       values ({@code Validate.GENDER.is.MALE}) and for enum-typed record parts ({@code Validate.MONEY.is.USD}),</li>
 *   <li>{@code ValueObject.detect/find/identify/parse}: text detection, shaped by {@code codegen/detection.txt}.</li>
 * </ul>
 *
 * <p>A value object is any top-level type that implements (or extends) {@code ValueObject}, directly or
 * through another value-object interface. Generic ones ({@code Measurable<T, U>}) are left out.
 *
 * <p>Run from the project root, after adding or changing a value object:
 * <pre>
 *   java codegen/GenerateValueObject.java           writes ValueObject.java
 *   java codegen/GenerateValueObject.java --check   fails if ValueObject.java is out of date (for CI)
 * </pre>
 */
public class GenerateValueObject {

    static final String PACKAGE = "io.joshuasalcedo.valueobject.core";
    static final Path SOURCES = Path.of("src/main/java/io/joshuasalcedo/valueobject");
    static final Path OUTPUT = SOURCES.resolve("core/ValueObject.java");
    static final Path DETECTION = Path.of("codegen/detection.txt");

    /** Factory names that turn one argument into the value, most preferred first. */
    static final List<String> FACTORY_NAMES = List.of(
            "parse", "of", "from", "fromAny", "fromCode", "fromName", "fromString", "fromZoneId", "fromValue", "valueOf");

    /** Argument types a Parse method accepts, in the order its branches are tried: instanceof type, argument. */
    static final Map<String, String[]> ARGUMENTS = new LinkedHashMap<>();
    static {
        arg("UUID", "java.util.UUID");
        arg("Instant", "java.time.Instant");
        arg("LocalDate", "java.time.LocalDate");
        arg("LocalTime", "java.time.LocalTime");
        arg("LocalDateTime", "java.time.LocalDateTime");
        arg("OffsetDateTime", "java.time.OffsetDateTime");
        arg("ZonedDateTime", "java.time.ZonedDateTime");
        arg("Duration", "java.time.Duration");
        arg("Period", "java.time.Period");
        arg("ZoneId", "java.time.ZoneId");
        arg("URI", "java.net.URI");
        arg("URL", "java.net.URL");
        arg("byte[]", "byte[]");
        arg("int", "Integer");
        arg("Integer", "Integer");
        arg("long", "Long");
        arg("Long", "Long");
        arg("double", "Double");
        arg("Double", "Double");
        arg("float", "Float");
        arg("Float", "Float");
        arg("BigDecimal", "java.math.BigDecimal");
        arg("BigInteger", "java.math.BigInteger");
        arg("Number", "Number");
        arg("String", "CharSequence");
        arg("CharSequence", "CharSequence");
    }

    static void arg(String paramType, String instanceType) {
        ARGUMENTS.put(paramType, new String[] {instanceType});
    }

    // ── Model ──────────────────────────────────────────────────────────────

    record Factory(String name, String paramType, String kind /* plain | nullable | optional | constructor */) {}

    record Component(String type, String name) {}

    static final class TypeInfo {
        String pkg;
        String name;
        String kind;               // record, enum, interface, class
        String outer;              // for a nested enum: its top-level type; else null
        boolean generic;
        Set<String> supers = new LinkedHashSet<>();
        List<Component> components = new ArrayList<>();
        List<String> constants = new ArrayList<>();
        List<Factory> factories = new ArrayList<>();

        String ref() {
            return outer == null ? name : outer + "." + name;
        }

        String importName() {
            return pkg + "." + (outer == null ? name : outer);
        }

        boolean isEnum() {
            return kind.equals("enum");
        }
    }

    record Shape(String regex, boolean scan) {}

    // ── Main ───────────────────────────────────────────────────────────────

    public static void main(String[] args) throws IOException {
        boolean check = List.of(args).contains("--check");

        Map<String, TypeInfo> topLevel = new LinkedHashMap<>();
        List<TypeInfo> nestedEnums = new ArrayList<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).sorted().toList()) {
                if (file.getFileName().toString().equals("package-info.java") || file.equals(OUTPUT)) continue;
                readFile(file, topLevel, nestedEnums);
            }
        }

        List<TypeInfo> values = topLevel.values().stream()
                .filter(t -> !t.generic && isValueObject(t, topLevel, new LinkedHashSet<>()))
                .toList();

        Map<String, Shape> shapes = readDetection(values);
        List<TypeInfo> ordered = new ArrayList<>();
        shapes.keySet().forEach(n -> values.stream().filter(t -> t.name.equals(n)).forEach(ordered::add));
        values.stream().filter(t -> !shapes.containsKey(t.name))
                .sorted(Comparator.comparing(t -> constantName(t.name)))
                .forEach(ordered::add);

        String source = render(ordered, shapes, topLevel, nestedEnums);

        String current = Files.exists(OUTPUT) ? Files.readString(OUTPUT).replace("\r\n", "\n") : "";
        if (check) {
            if (!current.equals(source)) {
                System.err.println(OUTPUT + " is out of date: run `java codegen/GenerateValueObject.java`");
                System.exit(1);
            }
            System.out.println(OUTPUT + " is up to date (" + ordered.size() + " value objects)");
            return;
        }
        Files.writeString(OUTPUT, source, StandardCharsets.UTF_8);
        System.out.println("Wrote " + OUTPUT + ": " + ordered.size() + " value objects, "
                + shapes.size() + " detectable");
    }

    // ── Reading sources ────────────────────────────────────────────────────

    static final Pattern PACKAGE_DECL = Pattern.compile("^package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
    static final Pattern TOP_DECL = Pattern.compile(
            "^public\\s+(?:(?:final|sealed|non-sealed|abstract|static|strictfp)\\s+)*(record|enum|interface|class)\\s+(\\w+)",
            Pattern.MULTILINE);
    static final Pattern NESTED_ENUM = Pattern.compile("^[ \\t]+(?:(?:public|static|private|protected)\\s+)*enum\\s+(\\w+)",
            Pattern.MULTILINE);
    static final Pattern FACTORY = Pattern.compile(
            "^[ \\t]*(?:public[ \\t]+)?static[ \\t]+(?:<[^>]*>[ \\t]*)?(@Nullable[ \\t]+)?([\\w.]+(?:<[\\w.?, ]+>)?)[ \\t]+(\\w+)"
                    + "[ \\t]*\\([ \\t]*(?:final[ \\t]+)?(?:@Nullable[ \\t]+)?([\\w.]+(?:[ \\t]*@Nullable)?(?:[ \\t]*\\[\\])?)[ \\t]+\\w+[ \\t]*\\)",
            Pattern.MULTILINE);

    static void readFile(Path file, Map<String, TypeInfo> topLevel, List<TypeInfo> nestedEnums) throws IOException {
        String code = stripComments(Files.readString(file));
        Matcher pm = PACKAGE_DECL.matcher(code);
        if (!pm.find()) return;
        String pkg = pm.group(1);

        Matcher dm = TOP_DECL.matcher(code);
        if (!dm.find()) return;
        TypeInfo t = new TypeInfo();
        t.pkg = pkg;
        t.kind = dm.group(1);
        t.name = dm.group(2);
        int bodyStart = readHeader(code, dm.end(), t);
        if (t.isEnum()) t.constants = enumConstants(code, bodyStart);

        // a signature split over lines ("parse(\n String text)") reads as one line
        String flat = code.replaceAll("\\(\\s*\\R\\s*", "(").replaceAll(",\\s*\\R\\s*", ", ")
                .replaceAll("\\R\\s*\\)", ")").replaceAll("(static[^;{=]*?)\\R\\s+", "$1 ");
        Matcher fm = FACTORY.matcher(flat);
        int read = 0;
        while (fm.find()) {
            read++;
            String returnType = fm.group(2);
            String name = fm.group(3);
            String param = fm.group(4).replace("@Nullable", "").replaceAll("\\s+", "");
            param = param.substring(param.lastIndexOf('.') + 1);
            if (param.equals("[]")) continue;
            if (!FACTORY_NAMES.contains(name) || !ARGUMENTS.containsKey(param)) continue;
            String kind;
            if (returnType.equals(t.name)) kind = fm.group(1) != null ? "nullable" : "plain";
            else if (returnType.equals("Optional<" + t.name + ">")) kind = "optional";
            else continue;
            t.factories.add(new Factory(name, param, kind));
        }
        // a one-argument parse(...) or of(...) the pattern could not read is reported, not skipped quietly
        Matcher declared = Pattern.compile("(?m)^[ \\t]*(?:public[ \\t]+)?static\\b[^;=]*?\\b(parse|of)\\(\\s*(?:final\\s+)?"
                + "(?:@Nullable\\s+)?[\\w.<>\\[\\]]+\\s+\\w+\\s*\\)").matcher(flat);
        int declaredCount = 0;
        while (declared.find()) declaredCount++;
        if (declaredCount > read) {
            System.err.println("warning: " + file + ": " + declaredCount + " one-argument parse/of methods, "
                    + read + " read; check their signatures");
        }
        if (t.kind.equals("record") && t.components.size() == 1) {
            String param = t.components.getFirst().type();
            if (ARGUMENTS.containsKey(param)) t.factories.add(new Factory("new", param, "constructor"));
        }
        topLevel.put(t.name, t);

        Matcher nm = NESTED_ENUM.matcher(code);
        while (nm.find()) {
            TypeInfo n = new TypeInfo();
            n.pkg = pkg;
            n.kind = "enum";
            n.name = nm.group(1);
            n.outer = t.name;
            int start = readHeader(code, nm.end(), n);
            n.constants = enumConstants(code, start);
            nestedEnums.add(n);
        }
    }

    /** Reads type parameters, record components and supertypes after the type name; returns the body's '{'. */
    static int readHeader(String code, int i, TypeInfo t) {
        i = skipSpace(code, i);
        if (code.charAt(i) == '<') {
            t.generic = true;
            i = skipBalanced(code, i, '<', '>');
        }
        i = skipSpace(code, i);
        if (code.charAt(i) == '(') {
            int end = skipBalanced(code, i, '(', ')');
            for (String part : splitTopLevel(code.substring(i + 1, end - 1))) {
                String p = part.replaceAll("@\\w+(?:\\([^)]*\\))?", " ").trim();
                if (p.isEmpty()) continue;
                String[] words = p.split("\\s+");
                String type = String.join("", List.of(words).subList(0, words.length - 1)).replaceAll("<.*>", "");
                t.components.add(new Component(type.substring(type.lastIndexOf('.') + 1), words[words.length - 1]));
            }
            i = end;
        }
        int brace = code.indexOf('{', i);
        String rest = code.substring(i, brace).replaceAll("\\bpermits\\b[\\s\\S]*$", "");
        String stripped = stripGenerics(rest);
        Matcher sm = Pattern.compile("\\b(?:extends|implements)\\b([^{]*?)(?=\\b(?:extends|implements)\\b|$)").matcher(stripped);
        while (sm.find()) {
            for (String s : sm.group(1).split(",")) {
                String name = s.trim();
                if (!name.isEmpty()) t.supers.add(name.substring(name.lastIndexOf('.') + 1));
            }
        }
        return brace;
    }

    /** The constant names of an enum whose body opens at {@code brace}. */
    static List<String> enumConstants(String code, int brace) {
        List<String> names = new ArrayList<>();
        int depth = 0;
        StringBuilder item = new StringBuilder();
        for (int i = brace + 1; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '"' || c == '\'') {
                int end = skipLiteral(code, i);
                if (depth == 0) item.append("\"\"");
                i = end - 1;
                continue;
            }
            if (c == '(' || c == '{') depth++;
            else if (c == ')' || c == '}') {
                if (depth == 0) {
                    addConstant(item, names);
                    break;
                }
                depth--;
            } else if (depth == 0 && (c == ',' || c == ';')) {
                addConstant(item, names);
                item.setLength(0);
                if (c == ';') break;
                continue;
            }
            if (depth == 0) item.append(c);
        }
        return names;
    }

    static void addConstant(StringBuilder item, List<String> names) {
        Matcher m = Pattern.compile("^\\s*(?:@\\w+\\s*)*([A-Za-z_][A-Za-z0-9_]*)").matcher(item);
        if (m.find()) names.add(m.group(1));
    }

    static boolean isValueObject(TypeInfo t, Map<String, TypeInfo> all, Set<String> seen) {
        if (!seen.add(t.name)) return false;
        for (String s : t.supers) {
            if (s.equals("ValueObject")) return true;
            TypeInfo st = all.get(s);
            if (st != null && isValueObject(st, all, seen)) return true;
        }
        return false;
    }

    static boolean reaches(TypeInfo t, String target, Map<String, TypeInfo> all, Set<String> seen) {
        if (!seen.add(t.ref())) return false;
        for (String s : t.supers) {
            if (s.equals(target)) return true;
            TypeInfo st = all.get(s);
            if (st != null && reaches(st, target, all, seen)) return true;
        }
        return false;
    }

    static Map<String, Shape> readDetection(List<TypeInfo> values) throws IOException {
        Set<String> known = values.stream().map(t -> t.name).collect(Collectors.toSet());
        Map<String, Shape> shapes = new LinkedHashMap<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(DETECTION)) {
            lineNo++;
            if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
            String[] parts = line.split("\\|", 3);
            if (parts.length != 3) throw new IllegalStateException(DETECTION + ":" + lineNo + ": expected `Type | mode | regex`");
            String name = parts[0].trim();
            String mode = parts[1].trim();
            String regex = parts[2].trim();
            if (!known.contains(name)) throw new IllegalStateException(DETECTION + ":" + lineNo + ": no value object " + name);
            if (!mode.equals("scan") && !mode.equals("whole"))
                throw new IllegalStateException(DETECTION + ":" + lineNo + ": mode must be scan or whole");
            Pattern.compile(regex);
            if (shapes.put(name, new Shape(regex, mode.equals("scan"))) != null)
                throw new IllegalStateException(DETECTION + ":" + lineNo + ": " + name + " is listed twice");
        }
        return shapes;
    }

    // ── Rendering ──────────────────────────────────────────────────────────

    static String render(List<TypeInfo> types, Map<String, Shape> shapes, Map<String, TypeInfo> topLevel,
                         List<TypeInfo> nestedEnums) {
        StringBuilder out = new StringBuilder();
        Set<String> imports = new TreeSet<>();
        for (TypeInfo t : types) if (!t.pkg.equals(PACKAGE)) imports.add(t.importName());

        List<TypeInfo> allEnums = new ArrayList<>(nestedEnums);
        topLevel.values().stream().filter(TypeInfo::isEnum).forEach(allEnums::add);

        // Validate rules first, so the enums they name are imported too.
        Map<TypeInfo, Map<String, String[]>> rules = new LinkedHashMap<>();
        for (TypeInfo t : types) {
            Map<String, String[]> is = isRules(t, topLevel, allEnums);
            if (!is.isEmpty()) rules.put(t, is);
            for (String[] r : is.values()) imports.add(r[2]);
        }
        imports.removeIf(i -> i.startsWith(PACKAGE + ".") && i.lastIndexOf('.') == PACKAGE.length());

        out.append("""
                // GENERATED by codegen/GenerateValueObject.java from the value objects in this library and
                // codegen/detection.txt. Do not edit: change those, then run `java codegen/GenerateValueObject.java`.
                package %s;

                """.formatted(PACKAGE));
        for (String i : imports) out.append("import ").append(i).append(";\n");
        out.append("""
                import org.jspecify.annotations.Nullable;

                import java.util.List;
                import java.util.Map;
                import java.util.Optional;

                /**
                 * A value object: small, immutable, self-validating, equal by value. Every value object in this
                 * library implements it, and it is the way in to all of them:
                 *
                 * <pre>{@code
                 * ValueObject.detect("mail jane@example.com or +63 917 123 4567");  // {EMAIL=1, PHONE_NUMBER=1}
                 * ValueObject.identify("192.168.0.1");                               // Optional[IPV4_ADDRESS]
                 * ValueObject.parse("149.99 USD");                                   // Optional[Money]
                 * ValueObject.Parse.Money("149.99 USD");                             // Money, or throws
                 * ValueObject.Validate.MONEY.is.USD.test("149.99 USD");              // true
                 * ValueObject.Validate.GENDER.is.MALE.test("male");                  // true
                 * ValueObject.Type.EMAIL.type();                                     // Email.class
                 * ValueObject.tables(ocrText);                                       // List<Table>
                 * }</pre>
                 */
                public interface ValueObject {

                    /**
                     * Counts the value objects found in a text.
                     *
                     * @param text any text
                     * @return how many of each value object it contains; types not found are absent
                     */
                    static Map<Type, Integer> detect(@Nullable String text) {
                        return Detection.detect(text);
                    }

                    /**
                     * Finds the value objects in a text.
                     *
                     * @param text any text
                     * @return the value objects it contains, by type, in the order they appear
                     */
                    static Map<Type, List<ValueObject>> find(@Nullable String text) {
                        return Detection.find(text);
                    }

                    /**
                     * Names the value object a whole string is.
                     *
                     * @param text a whole value, e.g. {@code "jane@example.com"}
                     * @return the most specific value object it is, or empty
                     */
                    static Optional<Type> identify(@Nullable String text) {
                        return Detection.identify(text);
                    }

                    /**
                     * Lists every value object a whole string could be.
                     *
                     * @param text a whole value, e.g. {@code "jane@example.com"}
                     * @return every value object it could be, most specific first
                     */
                    static List<Type> candidates(@Nullable String text) {
                        return Detection.candidates(text);
                    }

                    /**
                     * Reads a whole string as the most specific value object it is.
                     *
                     * @param text a whole value, e.g. {@code "149.99 USD"}
                     * @return it as the most specific value object it is, or empty
                     */
                    static Optional<ValueObject> parse(@Nullable String text) {
                        return Detection.parseAny(text);
                    }

                    /**
                     * Finds the value objects in a text, each with how it was found.
                     *
                     * @param text any text
                     * @return every value object in it, in order, with the shape that matched it and where it is;
                     *         {@code Json.of(...)} or {@code Csv.of(...)} write them out
                     */
                    static List<Found> scan(@Nullable String text) {
                        return Detection.scan(text);
                    }

                    /**
                     * Finds the tables in a long string: Tesseract TSV, positioned OCR words, or plain text with
                     * columns set apart by spaces, tabs or pipes. Columns are named by the header row and typed by
                     * the value objects most of their cells are.
                     *
                     * @param text any text
                     * @return the tables in it, in reading order
                     * @see io.joshuasalcedo.valueobject.table.TableDetector
                     */
                    static List<io.joshuasalcedo.valueobject.table.Table> tables(@Nullable String text) {
                        return io.joshuasalcedo.valueobject.table.TableDetector.detect(text);
                    }

                """);

        renderType(out, types, shapes);
        renderParse(out, types);
        renderValidate(out, types, rules);
        out.append("}\n");
        return out.toString();
    }

    static void renderType(StringBuilder out, List<TypeInfo> types, Map<String, Shape> shapes) {
        out.append("""
                    /** Every value object in this library; detectable ones first, most specific first. */
                    enum Type {
                """);
        for (int i = 0; i < types.size(); i++) {
            TypeInfo t = types.get(i);
            Shape s = shapes.get(t.name);
            out.append("        /** {@link ").append(t.ref()).append("}").append(s == null ? "" : s.scan() ? ", found in text" : ", recognised as a whole value")
                    .append(". */\n");
            out.append("        ").append(constantName(t.name)).append('(').append(t.name).append(".class, ")
                    .append(s == null ? "null" : javaString(s.regex())).append(", ").append(s != null && s.scan())
                    .append(')').append(i == types.size() - 1 ? ";" : ",").append('\n');
        }
        out.append("""

                        private final Class<? extends ValueObject> type;
                        private final @Nullable String shape;
                        private final boolean scannable;

                        Type(Class<? extends ValueObject> type, @Nullable String shape, boolean scannable) {
                            this.type = type;
                            this.shape = shape;
                            this.scannable = scannable;
                        }

                        /** {@return its Java type, e.g. {@code Email.class}} */
                        public Class<? extends ValueObject> type() {
                            return type;
                        }

                        /** {@return whether {@link ValueObject#detect} and {@link ValueObject#identify} recognise it} */
                        public boolean detectable() {
                            return shape != null;
                        }

                        /** {@return the regex that recognises it in text ({@code codegen/detection.txt}), or null when it is not detectable} */
                        public @Nullable String shape() {
                            return shape;
                        }

                        boolean scannable() {
                            return scannable;
                        }

                        /**
                         * Makes this value object from anything.
                         *
                         * @param value the value object itself, or anything one of its factories takes
                         * @return it as this value object
                         * @throws io.joshuasalcedo.valueobject.core.ValueObjectException if it is not one
                         */
                        public ValueObject parse(@Nullable Object value) {
                            return switch (this) {
                """);
        for (TypeInfo t : types) {
            out.append("                case ").append(constantName(t.name)).append(" -> Parse.").append(t.name).append("(value);\n");
        }
        out.append("""
                            };
                        }

                        /**
                         * Makes this value object from anything, if it can.
                         *
                         * @param value anything
                         * @return it as this value object, or empty when it is not one
                         */
                        public Optional<ValueObject> tryParse(@Nullable Object value) {
                            try {
                                return Optional.of(parse(value));
                            } catch (RuntimeException e) {
                                return Optional.empty();
                            }
                        }

                        /** {@return its checks, the same as {@code ValueObject.Validate.<TYPE>}} */
                        public Check<? extends ValueObject> check() {
                            return switch (this) {
                """);
        for (TypeInfo t : types) {
            out.append("                case ").append(constantName(t.name)).append(" -> Validate.").append(constantName(t.name)).append(";\n");
        }
        out.append("""
                            };
                        }

                        /**
                         * Finds the type of a value object's class.
                         *
                         * @param javaType a value object's class, e.g. {@code Email.class}, or a subtype of one
                         * @return its type, or empty
                         */
                        public static Optional<Type> of(Class<?> javaType) {
                            for (Type t : values()) if (t.type == javaType) return Optional.of(t);
                            for (Type t : values()) if (t.type.isAssignableFrom(javaType)) return Optional.of(t);
                            return Optional.empty();
                        }
                    }

                """);
    }

    static void renderParse(StringBuilder out, List<TypeInfo> types) {
        out.append("""
                    /**
                     * Makes a value object from anything its own factories take: {@code Parse.Money("149.99 USD")},
                     * {@code Parse.Port(8080)}, {@code Parse.UniqueId(uuid)}. A value that already is one is returned as is.
                     * Every method throws {@link io.joshuasalcedo.valueobject.core.ValueObjectException} when the input
                     * is not one.
                     */
                    final class Parse {

                        private Parse() {}
                """);
        for (TypeInfo t : types) {
            Map<String, List<Factory>> byInstance = new LinkedHashMap<>();
            for (String param : ARGUMENTS.keySet()) {
                for (Factory f : sortedFactories(t)) {
                    if (f.paramType().equals(param)) {
                        byInstance.computeIfAbsent(ARGUMENTS.get(param)[0], k -> new ArrayList<>()).add(f);
                    }
                }
            }
            boolean hasNumber = byInstance.containsKey("Number");
            String numeric = null;
            for (String n : List.of("Long", "Double", "Integer")) {   // widest first: no silent truncation
                if (byInstance.containsKey(n)) {
                    numeric = n;
                    break;
                }
            }
            String c = constantName(t.name);
            out.append("\n        /**\n         * Makes a {@link ").append(t.name).append("} from anything its factories take.\n         *");
            out.append("\n         * @param value a {@link ").append(t.name).append('}');
            List<String> accepted = new ArrayList<>();
            byInstance.forEach((inst, fs) -> accepted.add("a " + simple(inst) + " ("
                    + fs.stream().map(f -> f.name().equals("new") ? "its constructor" : "{@code " + t.name + "." + f.name() + "}")
                    .collect(Collectors.joining(", ")) + ")"));
            if (t.isEnum() && !byInstance.containsKey("CharSequence")) accepted.add("a String (a constant name)");
            for (String a : accepted) out.append(", or ").append(a);
            out.append("\n         * @return it as a {@link ").append(t.name).append("}\n         */\n");
            out.append("        public static ").append(t.name).append(' ').append(t.name).append("(@Nullable Object value) {\n");
            out.append("            return Parsing.parse(Type.").append(c).append(", ").append(t.name).append(".class, value, v -> {\n");
            int n = 0;
            for (Map.Entry<String, List<Factory>> e : byInstance.entrySet()) {
                String inst = e.getKey();
                String var = "a" + (n++);
                String arg = inst.equals("CharSequence") ? var + ".toString()" : var;
                List<String> calls = new ArrayList<>();
                for (Factory f : e.getValue()) calls.add(call(t, f, "x"));
                if (inst.equals("CharSequence") && t.isEnum()) calls.add("Parsing.constant(" + t.name + ".class, x)");
                out.append("                if (v instanceof ").append(inst).append(' ').append(var).append(") return Parsing.firstOf(")
                        .append(arg);
                for (String call : calls) out.append(", x -> ").append(call);
                out.append(");\n");
            }
            if (!hasNumber && numeric != null) {
                String conv = switch (numeric) {
                    case "Integer" -> "intValue()";
                    case "Long" -> "longValue()";
                    default -> "doubleValue()";
                };
                List<Factory> fs = byInstance.get(numeric);
                out.append("                if (v instanceof Number num) return Parsing.firstOf(num.").append(conv).append(')');
                out.setLength(out.length() - 1);
                for (Factory f : fs) out.append(", x -> ").append(call(t, f, "x"));
                out.append(");\n");
            }
            if (t.isEnum() && !byInstance.containsKey("CharSequence")) {
                out.append("                if (v instanceof CharSequence s) return Parsing.constant(").append(t.name)
                        .append(".class, s.toString());\n");
            }
            out.append("                return null;\n            });\n        }\n");
        }
        out.append("    }\n\n");
    }

    static String call(TypeInfo t, Factory f, String x) {
        String invoke = f.kind().equals("constructor")
                ? "new " + t.name + "(" + x + ")"
                : t.name + "." + f.name() + "(" + x + ")";
        if (f.kind().equals("optional")) invoke += ".orElse(null)";
        if (t.isEnum() && t.constants.contains("UNKNOWN")) invoke = "Parsing.notUnknown(" + invoke + ", " + t.name + ".UNKNOWN)";
        return invoke;
    }

    static List<Factory> sortedFactories(TypeInfo t) {
        return t.factories.stream()
                .sorted(Comparator.comparingInt(f -> f.name().equals("new") ? 99 : FACTORY_NAMES.indexOf(f.name())))
                .toList();
    }

    /** {@code is.<VALUE>} rules: name -> [part accessor, constant reference, import]. */
    static Map<String, String[]> isRules(TypeInfo t, Map<String, TypeInfo> topLevel, List<TypeInfo> allEnums) {
        Map<String, String[]> rules = new LinkedHashMap<>();
        if (t.isEnum()) {
            for (String k : t.constants) rules.putIfAbsent(k, new String[] {"v -> v", t.ref() + "." + k, t.importName()});
        } else if (t.kind.equals("interface")) {
            for (TypeInfo e : implementers(t.name, topLevel, allEnums)) {
                for (String k : e.constants) rules.putIfAbsent(k, new String[] {"v -> v", e.ref() + "." + k, e.importName()});
            }
        } else if (t.kind.equals("record")) {
            for (Component comp : t.components) {
                for (TypeInfo e : enumsFor(comp.type(), t, topLevel, allEnums)) {
                    for (String k : e.constants) {
                        rules.putIfAbsent(k, new String[] {t.name + "::" + comp.name(), e.ref() + "." + k, e.importName()});
                    }
                }
            }
        }
        return rules;
    }

    /** The enums a record part of this type can hold: the enum itself, or every enum implementing the interface. */
    static List<TypeInfo> enumsFor(String type, TypeInfo owner, Map<String, TypeInfo> topLevel, List<TypeInfo> allEnums) {
        for (TypeInfo e : allEnums) {
            if (e.outer != null && e.outer.equals(owner.name) && e.name.equals(type)) return List.of(e);
        }
        TypeInfo top = topLevel.get(type);
        if (top == null) return List.of();
        if (top.isEnum()) return List.of(top);
        if (top.kind.equals("interface")) return implementers(type, topLevel, allEnums);
        return List.of();
    }

    static List<TypeInfo> implementers(String iface, Map<String, TypeInfo> topLevel, List<TypeInfo> allEnums) {
        return allEnums.stream().filter(e -> reaches(e, iface, topLevel, new LinkedHashSet<>()))
                .sorted(Comparator.comparing((TypeInfo e) -> e.outer != null).thenComparing(TypeInfo::ref))
                .toList();
    }

    static void renderValidate(StringBuilder out, List<TypeInfo> types, Map<TypeInfo, Map<String, String[]>> rules) {
        out.append("""
                    /**
                     * Checks on any input, which is parsed first: {@code Validate.EMAIL.valid.test("a@b.co")},
                     * {@code Validate.MONEY.is.USD.test("149.99 USD")}, {@code Validate.GENDER.is.MALE.test(gender)}.
                     * Input that is not the value object fails the check; it never throws.
                     */
                    final class Validate {

                        private Validate() {}

                """);
        for (TypeInfo t : types) {
            String c = constantName(t.name);
            out.append("        /** Checks for {@link ").append(t.name).append("}. */\n");
            if (rules.containsKey(t)) {
                out.append("        public static final ").append(t.name).append("Check ").append(c).append(" = new ")
                        .append(t.name).append("Check();\n");
            } else {
                out.append("        public static final Check<").append(t.name).append("> ").append(c)
                        .append(" = new Check<>(Type.").append(c).append(", Parse::").append(t.name).append(");\n");
            }
        }
        for (Map.Entry<TypeInfo, Map<String, String[]>> e : rules.entrySet()) {
            TypeInfo t = e.getKey();
            String c = constantName(t.name);
            out.append("\n        /** The checks for {@link ").append(t.name).append("}, with one rule per value in {@link #is}. */\n");
            out.append("        public static final class ").append(t.name).append("Check extends Check<").append(t.name).append("> {\n\n");
            out.append("            /** One rule per value, e.g. {@code Validate.").append(c).append(".is.")
                    .append(e.getValue().keySet().stream().filter(k -> !k.equals("UNKNOWN")).findFirst()
                            .orElse(e.getValue().keySet().iterator().next()))
                    .append("}. */\n");
            out.append("            public final Is is = new Is();\n\n");
            out.append("            private ").append(t.name).append("Check() {\n                super(Type.").append(c)
                    .append(", Parse::").append(t.name).append(");\n            }\n\n");
            out.append("            /** The values a {@link ").append(t.name).append("} can be checked against. */\n");
            out.append("            public final class Is {\n\n                private Is() {}\n\n");
            for (Map.Entry<String, String[]> r : e.getValue().entrySet()) {
                String part = r.getValue()[0].equals("v -> v") ? "it is"
                        : "its {@code " + r.getValue()[0].substring(r.getValue()[0].indexOf("::") + 2) + "} is";
                out.append("                /** Passes a {@link ").append(t.name).append("} when ").append(part)
                        .append(" {@link ").append(r.getValue()[1].replaceFirst("\\.([A-Z0-9_]+)$", "#$1")).append("}. */\n");
                out.append("                public final Rule<").append(t.name).append("> ").append(r.getKey())
                        .append(" = isRule(\"").append(r.getKey()).append("\", ").append(r.getValue()[0]).append(", ")
                        .append(r.getValue()[1]).append(");\n");
            }
            out.append("            }\n        }\n");
        }
        out.append("    }\n");
    }

    // ── Text helpers ───────────────────────────────────────────────────────

    /** {@code IpAddress} -> {@code IP_ADDRESS}, {@code Ipv4Address} -> {@code IPV4_ADDRESS}. */
    static String constantName(String name) {
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").replaceAll("([A-Z])([A-Z][a-z])", "$1_$2").toUpperCase(Locale.ROOT);
    }

    static String simple(String type) {
        String s = type.substring(type.lastIndexOf('.') + 1);
        return s.equals("CharSequence") ? "String" : s;
    }

    static String javaString(String s) {
        return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    static String stripComments(String src) {
        StringBuilder out = new StringBuilder(src.length());
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == '"' || c == '\'') {
                int end = skipLiteral(src, i);
                out.append(src, i, end);
                i = end - 1;
            } else if (c == '/' && i + 1 < src.length() && src.charAt(i + 1) == '/') {
                while (i < src.length() && src.charAt(i) != '\n') i++;
                out.append('\n');
            } else if (c == '/' && i + 1 < src.length() && src.charAt(i + 1) == '*') {
                int end = src.indexOf("*/", i + 2);
                String comment = src.substring(i, end < 0 ? src.length() : end + 2);
                out.append(comment.replaceAll("[^\n]", " "));
                i = (end < 0 ? src.length() : end + 2) - 1;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** @return the index just past the string, text block or char literal starting at {@code i} */
    static int skipLiteral(String s, int i) {
        char q = s.charAt(i);
        if (q == '"' && s.startsWith("\"\"\"", i)) {
            int end = s.indexOf("\"\"\"", i + 3);
            return end < 0 ? s.length() : end + 3;
        }
        for (int j = i + 1; j < s.length(); j++) {
            char c = s.charAt(j);
            if (c == '\\') j++;
            else if (c == q) return j + 1;
        }
        return s.length();
    }

    static int skipSpace(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    static int skipBalanced(String s, int i, char open, char close) {
        int depth = 0;
        for (int j = i; j < s.length(); j++) {
            char c = s.charAt(j);
            if (c == '"' || c == '\'') {
                j = skipLiteral(s, j) - 1;
                continue;
            }
            if (c == open) depth++;
            else if (c == close && --depth == 0) return j + 1;
        }
        return s.length();
    }

    static String stripGenerics(String s) {
        String prev;
        do {
            prev = s;
            s = s.replaceAll("<[^<>]*>", "");
        } while (!s.equals(prev));
        return s;
    }

    static List<String> splitTopLevel(String s) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '<' || c == '(') depth++;
            else if (c == '>' || c == ')') depth--;
            if (c == ',' && depth == 0) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        parts.add(cur.toString());
        return parts;
    }
}
