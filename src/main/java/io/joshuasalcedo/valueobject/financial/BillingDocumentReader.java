package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.contact.Address;
import io.joshuasalcedo.valueobject.contact.Email;
import io.joshuasalcedo.valueobject.contact.Link;
import io.joshuasalcedo.valueobject.contact.Party;
import io.joshuasalcedo.valueobject.contact.PhoneNumber;
import io.joshuasalcedo.valueobject.geo.Country;
import io.joshuasalcedo.valueobject.table.Table;
import io.joshuasalcedo.valueobject.temporal.DateRange;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads bills, invoices and receipts from text. Hand-written; used by {@link BillingDocument#parse},
 * {@link Bill#parse}, {@link Invoice#parse} and {@link Receipt#parse}.
 *
 * <p>Steps:
 * <ol>
 *   <li>Labelled values: {@code "Label: value"}, {@code "Label   value"} (cells set apart by spaces) and known
 *       labels followed by their value ({@code "Invoice number NWC-1"}). Known labels (in many wordings) fill the
 *       document's fields; every other one goes to the metadata.</li>
 *   <li>Parties: the block under or beside a "Bill to", "Bill from", "Merchant", ... label; then unlabelled blocks
 *       that hold an address, a country or an email (the issuer's, usually top left). Each becomes a {@link Party}
 *       with its {@link Address}, email, phone, website and tax id.</li>
 *   <li>The line items: the table ({@link io.joshuasalcedo.valueobject.table.TableDetector}) whose columns are
 *       description, quantity, unit price and amount, by name or by what the cells are. Its "Subtotal", "Total",
 *       "VAT" rows are totals, not lines.</li>
 *   <li>The currency: the one most amounts name; else the one the symbols mean; else the currency of the country
 *       found on the document.</li>
 * </ol>
 */
final class BillingDocumentReader {

    enum Kind { BILL, INVOICE, RECEIPT }

    private BillingDocumentReader() {}

    // ── Entry points ──────────────────────────────────────────────────────

    static Optional<Kind> kind(String input) {
        String text = io.joshuasalcedo.valueobject.table.Layout.text(input);
        String lower = text.toLowerCase(Locale.ROOT);
        List<String> top = text.lines().filter(l -> !l.isBlank()).limit(6).map(l -> l.toLowerCase(Locale.ROOT)).toList();
        Map<Kind, Integer> score = new EnumMap<>(Kind.class);
        for (Kind k : Kind.values()) score.put(k, 0);
        add(score, Kind.RECEIPT, lower, 3, "receipt");
        add(score, Kind.RECEIPT, lower, 5, "official receipt", "payment confirmation", "acknowledgement receipt");
        add(score, Kind.RECEIPT, lower, 2, "amount paid", "date paid", "paid on", "payment method", "change", "cash",
                "thank you", "transaction", "reference no", "status");
        add(score, Kind.RECEIPT, lower, 3, "you received", "you sent", "amount received", "payment details",
                "transfer number", "payment successful", "payment received");
        add(score, Kind.INVOICE, lower, 3, "invoice");
        add(score, Kind.INVOICE, lower, 2, "tax invoice", "bill to", "bill from", "due date", "total due", "issue date");
        add(score, Kind.BILL, lower, 5, "statement of account", "billing statement", "billing period");
        add(score, Kind.BILL, lower, 3, "account number", "account no", "service address", "previous balance",
                "amount due", "total amount due", "statement date", "bill date");
        // Filipino
        add(score, Kind.RECEIPT, lower, 5, "opisyal na resibo", "resibo");
        add(score, Kind.RECEIPT, lower, 2, "sukli", "salamat po", "ibinayad", "bayad");
        add(score, Kind.BILL, lower, 4, "singil", "bayarin", "pahayag ng account");
        add(score, Kind.BILL, lower, 2, "takdang petsa", "panahon ng singil");
        for (String line : top) {
            if (line.matches(".*\\b(official )?receipt\\b.*") || line.contains("payment confirmation")
                    || line.matches(".*\\b(opisyal na )?resibo\\b.*")) score.merge(Kind.RECEIPT, 6, Integer::sum);
            if (line.matches(".*\\b(tax )?invoice\\b.*") && !line.contains("number")) score.merge(Kind.INVOICE, 6, Integer::sum);
            if (line.contains("statement of account") || line.matches(".*\\bbill(ing)?\\b(?! (to|from)).*")) score.merge(Kind.BILL, 6, Integer::sum);
        }
        return score.entrySet().stream().max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() >= 5).map(Map.Entry::getKey);
    }

    private static void add(Map<Kind, Integer> score, Kind kind, String text, int weight, String... words) {
        for (String w : words) {
            if (Pattern.compile("\\b" + Pattern.quote(w) + "\\b").matcher(text).find()) score.merge(kind, weight, Integer::sum);
        }
    }

    static Invoice invoice(String text) {
        Read r = new Read(text, Kind.INVOICE);
        Optional<PaymentTerms> terms = r.best(Field.TERMS).flatMap(p -> PaymentTerms.tryParse(p.value()));
        Optional<LocalDate> due = r.date(Field.DUE);
        boolean dueFromTerms = false;
        if (due.isEmpty() && terms.isPresent()) {
            Optional<LocalDate> issued = r.date(Field.DATE);
            if (issued.isPresent()) {
                due = terms.get().dueDate(issued.get());
                dueFromTerms = due.isPresent();
            }
        }
        BillingDocument.Fields fields = r.fields(Set.of(Field.DUE, Field.PO, Field.TERMS));
        if (dueFromTerms) {
            Map<String, BillingDocument.Source> sources = new LinkedHashMap<>(fields.sources());
            sources.put("dueDate", new BillingDocument.Source("dueDate", "", terms.get() + " from " + r.date(Field.DATE).get(),
                    -1, BillingDocument.Form.INFERRED));
            fields = new BillingDocument.Fields(fields.number(), fields.date(), fields.issuer(), fields.customer(),
                    fields.lineItems(), fields.tables(), fields.subtotal(), fields.tax(), fields.total(), fields.currency(),
                    fields.metadata(), fields.found(), sources, fields.text());
        }
        return new Invoice.Data(fields, due, r.string(Field.PO), terms);
    }

    static Bill bill(String text) {
        Read r = new Read(text, Kind.BILL);
        return new Bill.Data(r.fields(Set.of(Field.ACCOUNT, Field.PERIOD, Field.DUE)), r.string(Field.ACCOUNT),
                r.period(), r.date(Field.DUE));
    }

    static Receipt receipt(String text) {
        Read r = new Read(text, Kind.RECEIPT);
        Optional<LocalDate> paidOn = r.date(Field.PAID_ON).or(() -> r.date(Field.DATE));
        return new Receipt.Data(r.fields(Set.of(Field.METHOD, Field.PAID_ON, Field.PAID, Field.CHANGE, Field.REFERENCE)),
                r.string(Field.METHOD), paidOn, r.money(Field.PAID), r.money(Field.CHANGE), r.string(Field.REFERENCE));
    }

    // ── Labels ────────────────────────────────────────────────────────────

    /** What a label can mean. */
    enum Field {
        /** Number. */
        NUMBER, DATE, DUE, PAID_ON, TOTAL, SUBTOTAL, TAX, PAID, CHANGE, METHOD, REFERENCE, ACCOUNT, PERIOD, PO,
        /** Issuer. */
        ISSUER, CUSTOMER, TAX_ID, TERMS
    }

    /** Label wordings (lower case, no punctuation), by field; the order within a field is its preference. */
    private static final Map<String, Field> LABELS = new LinkedHashMap<>();
    /** For fields seen more than once (TOTAL), a lower rank wins. */
    private static final Map<String, Integer> RANK = new HashMap<>();

    static {
        label(Field.NUMBER, "invoice number", "invoice no", "invoice", "receipt number", "receipt no",
                "official receipt no", "or no", "or number", "bill number", "bill no", "statement number", "statement no",
                "document number", "document no", "document");
        label(Field.DATE, "invoice date", "issue date", "date issued", "bill date", "statement date", "receipt date",
                "transaction date", "date time", "date and time", "date");
        label(Field.DUE, "due date", "payment due", "due on", "pay before", "due");
        label(Field.PAID_ON, "date paid", "paid on", "payment date");
        label(Field.TOTAL, "total amount due", "total due", "amount due", "amount to pay", "amount payable",
                "balance due", "grand total", "total amount", "net amount", "total", "amount");
        label(Field.SUBTOTAL, "subtotal", "sub total");
        label(Field.TAX, "sales tax", "vat", "gst", "tax");
        label(Field.PAID, "amount paid", "amount received", "amount tendered", "tendered", "paid", "cash");
        label(Field.CHANGE, "change");
        label(Field.METHOD, "payment method", "paid by", "payment type", "mode of payment", "channel", "payment", "card");
        label(Field.REFERENCE, "reference number", "reference no", "reference", "ref no", "ref", "transaction id",
                "transaction no", "txn id", "txnid", "trace no", "transfer number", "transfer no", "confirmation number",
                "confirmation no");
        label(Field.ACCOUNT, "account number", "account no", "customer number", "customer no", "subscriber number",
                "account");
        label(Field.PERIOD, "billing period", "service period", "period covered", "statement period", "period");
        label(Field.PO, "purchase order number", "purchase order no", "purchase order", "po number", "po no", "po");
        label(Field.ISSUER, "bill from", "billed from", "from", "seller", "vendor", "merchant", "issued by", "supplier",
                "sold by", "biller name", "biller", "payee", "merchant name");
        label(Field.CUSTOMER, "bill to", "billed to", "sold to", "customer name", "customer", "client", "account name",
                "payer");
        label(Field.TAX_ID, "vat id", "vat number", "tax id", "vat reg tin", "tin");
        label(Field.TERMS, "payment terms", "terms of payment", "credit terms", "terms");

        // Filipino (Tagalog) wordings, as Philippine receipts and bills print them; "blg." is "bilang" (number)
        label(Field.NUMBER, "resibo blg", "opisyal na resibo blg", "numero ng resibo", "blg ng resibo", "numero ng invoice",
                "invoice blg", "blg ng invoice");
        label(Field.DATE, "petsa", "petsa ng resibo", "petsa ng invoice", "araw");
        label(Field.DUE, "takdang petsa", "huling araw ng pagbabayad", "dapat bayaran bago", "takdang araw");
        label(Field.PAID_ON, "petsa ng pagbabayad", "petsa ng bayad");
        label(Field.TOTAL, "kabuuang halaga", "kabuuang dapat bayaran", "kabuuan", "halagang dapat bayaran", "babayaran",
                "halaga");
        label(Field.SUBTOTAL, "kabuuang halaga bago ang buwis", "bahagyang kabuuan");
        label(Field.TAX, "buwis", "vat na buwis");
        label(Field.PAID, "halagang ibinayad", "ibinayad", "naibayad", "bayad", "salapi");
        label(Field.CHANGE, "sukli");
        label(Field.METHOD, "paraan ng pagbabayad", "paraan ng bayad");
        label(Field.REFERENCE, "reperensya", "numero ng reperensya", "ref blg");
        label(Field.ACCOUNT, "numero ng account", "account blg", "numero ng akawnt");
        label(Field.PERIOD, "panahon ng singil", "saklaw na panahon");
        label(Field.ISSUER, "nagbenta", "tindahan", "mula kay", "galing kay");
        label(Field.CUSTOMER, "pangalan ng mamimili", "mamimili", "kostumer", "para kay", "pangalan");
    }

    private static void label(Field f, String... words) {
        for (int i = 0; i < words.length; i++) {
            LABELS.putIfAbsent(words[i], f);
            RANK.putIfAbsent(words[i], i);
        }
    }

    /** {@code "Invoice No.:"} -> {@code "invoice no"}; {@code "VAT (12%)"} -> {@code "vat"}. */
    static String normalize(String label) {
        return label.toLowerCase(Locale.ROOT).replaceAll("\\(.*?\\)", " ").replaceAll("[.:#/*_-]", " ")
                .replaceAll("\\s+", " ").strip();
    }

    static @Nullable Field field(String label) {
        String n = normalize(label);
        Field exact = LABELS.get(n);
        if (exact != null) return exact;
        // a brand or qualifier before a known label: "Paylo Ref No", "Wallet Reference Number"
        String best = null;
        for (String known : LABELS.keySet()) {
            if (known.contains(" ") && n.endsWith(" " + known) && n.split(" ").length <= known.split(" ").length + 2
                    && (best == null || known.length() > best.length())) best = known;
        }
        return best == null ? null : LABELS.get(best);
    }

    /** Whether a value fits what a field holds (a number has a digit, a method is not an amount, ...). */
    static boolean fits(@Nullable Field f, String value) {
        if (f == null) return true;
        String v = value.strip();
        if (v.isEmpty()) return f == Field.ISSUER || f == Field.CUSTOMER;
        return switch (f) {
            case NUMBER, REFERENCE, ACCOUNT, PO, TAX_ID -> v.matches(".*\\d.*") && v.length() <= 40;
            case DATE, DUE, PAID_ON -> parseDate(v) != null;
            case PERIOD -> range(v) != null || parseDate(v) != null;
            case TOTAL, SUBTOTAL, TAX, PAID, CHANGE -> looksLikeMoney(v);
            case METHOD -> !looksLikeMoney(v) && v.matches(".*\\p{L}.*");
            case ISSUER, CUSTOMER -> v.matches(".*\\p{L}{2}.*") && !looksLikeMoney(v);
            case TERMS -> PaymentTerms.tryParse(v).isPresent();
        };
    }

    // ── One reading ───────────────────────────────────────────────────────

    /** A cell of a line: its text and the column it starts at. */
    record Cell(String text, int x, int end) {}

    /** A labelled value: where it was, the label as printed, and the value text. */
    record Pair(int line, int x, String label, String value, @Nullable Field field, BillingDocument.Form form) {}

    private static final Pattern CELL = Pattern.compile("\\S+(?: \\S+)*");
    private static final Pattern COLON = Pattern.compile("^(\\p{L}[\\p{L}\\p{N} .#/()'&-]{0,38}?)\\s*:\\s*(.*)$");

    static final class Read {

        final String text;
        final List<List<Cell>> lines = new ArrayList<>();
        final List<Pair> pairs = new ArrayList<>();
        final Set<String> usedCells = new HashSet<>();      // "line:x" of cells read as labels or values
        final @Nullable Currency currency;
        final List<Table> tables;
        final Map<ValueObject.Type, List<ValueObject>> detected;
        final List<io.joshuasalcedo.valueobject.Found> found;
        final Map<String, BillingDocument.Source> sources = new LinkedHashMap<>();
        @Nullable String currencyHow;
        final List<Address> addresses = new ArrayList<>();
        @Nullable Party issuer;
        BillingDocument.@Nullable Source issuerHow;
        @Nullable Party customer;
        final List<LineItem> items = new ArrayList<>();
        final Map<Field, Money> tableTotals = new EnumMap<>(Field.class);

        final Kind kind;

        Read(String input, Kind kind) {
            this.kind = kind;
            // OCR words with boxes (Tesseract TSV, positioned words) are laid out as on the page first
            String text = io.joshuasalcedo.valueobject.table.Layout.text(input);
            this.text = text;
            for (String raw : text.split("\\R", -1)) {
                List<Cell> cells = new ArrayList<>();
                Matcher m = CELL.matcher(raw);
                while (m.find()) cells.add(new Cell(m.group(), m.start(), m.end()));
                lines.add(cells);
            }
            readPairs();
            this.found = ValueObject.scan(text);
            Map<ValueObject.Type, List<ValueObject>> byType = new EnumMap<>(ValueObject.Type.class);
            for (var f : found) byType.computeIfAbsent(f.type(), k -> new ArrayList<>()).add(f.value());
            this.detected = byType;
            readParties();
            this.currency = currency();
            this.tables = ValueObject.tables(text);
            readLineItems();
        }

        // labelled values

        private void readPairs() {
            readVerticalPairs();
            for (int i = 0; i < lines.size(); i++) {
                List<Cell> cells = lines.get(i);
                for (int c = 0; c < cells.size(); c++) {
                    Cell cell = cells.get(c);
                    if (usedCells.contains(i + ":" + cell.x())) continue;
                    Cell next = c + 1 < cells.size() ? cells.get(c + 1) : null;
                    // "VAT  (12%)  41.79": a bracketed note after a label is part of the label
                    if (next != null && next.text().matches("^\\(.*\\)$") && c + 2 < cells.size()
                            && LABELS.containsKey(normalize(cell.text()))) {
                        Cell merged = new Cell(cell.text() + " " + next.text(), cell.x(), next.end());
                        used(i, next);
                        cells = new ArrayList<>(cells);
                        cells.set(c, merged);
                        cells.remove(c + 1);
                        cell = merged;
                        next = cells.get(c + 1);
                    }

                    Matcher colon = COLON.matcher(cell.text());
                    if (colon.matches() && !cell.text().matches("(?i).*\\bhttps?:.*") && !colon.group(2).startsWith("//")) {
                        String value = colon.group(2).strip();
                        if (value.isEmpty() && next != null) {
                            value = next.text();
                            used(i, next);
                        }
                        if (!value.isEmpty()) add(i, cell, colon.group(1), value, BillingDocument.Form.LABEL_COLON);
                        continue;
                    }
                    String whole = normalize(cell.text());
                    if (LABELS.containsKey(whole) && next != null && !isLabel(next.text()) && fits(field(whole), next.text())) {
                        add(i, cell, cell.text(), next.text(), BillingDocument.Form.LABEL_BESIDE);
                        used(i, next);
                        c++;
                        continue;
                    }
                    // "Invoice number NWC-1", "OFFICIAL RECEIPT No. 000123": a known label, a space, its value
                    String known = knownPrefix(cell.text());
                    if (known != null && !known.strip().contains(" ") && !known.strip().matches(".*[.:#]$")
                            && !cell.text().substring(known.length()).matches("^\\s*[:#.].*|^\\s+\\d.*")) known = null;
                    if (known != null) {
                        String value = cell.text().substring(known.length()).replaceFirst("^[\\s.:#-]+", "").strip();
                        if (!value.isEmpty() && value.matches(".*[\\p{N}\\p{L}].*") && fits(field(known), value)) {
                            add(i, cell, known, value, BillingDocument.Form.LABEL_PREFIX);
                            continue;
                        }
                    }
                    // "Previous balance    PHP 1,499.00": a short phrase, then an amount or a date
                    if (c == 0 && cells.size() == 2 && next != null && cell.text().matches("[\\p{L}][\\p{L} ()%.'&/-]{0,30}(\\(\\d+(\\.\\d+)?%\\))?[\\p{L} ]{0,10}")
                            && (looksLikeMoney(next.text()) || parseDate(next.text()) != null)) {
                        add(i, cell, cell.text(), next.text(), BillingDocument.Form.PHRASE_AMOUNT);
                        used(i, next);
                        c++;
                    }
                }
            }
        }

        private static boolean isLabel(String s) {
            return field(s) != null;
        }

        /** Label cells with their values on the next line, under them ("Account Number" over "8812-4407"). */
        private void readVerticalPairs() {
            for (int i = 0; i < lines.size(); i++) {
                List<Cell> cells = lines.get(i);
                if (cells.isEmpty() || cells.size() > 2) continue;
                if (!cells.stream().allMatch(c -> c.text().matches("[\\p{L}][\\p{L} ./()&'-]{0,38}:?"))) continue;
                for (Cell label : cells) {
                    boolean colon = label.text().endsWith(":");
                    Field f = field(label.text());
                    if (f == null && !colon) continue;
                    if (!colon && !label.text().strip().contains(" ") && (f == Field.TOTAL || f == Field.TAX)) continue;
                    // a value beside the label on its own line ("Mamimili:  Juan dela Cruz") is read beside, not below
                    int at = cells.indexOf(label);
                    // ("Billing Period   Plan" is two headings, the values below: "Plan" is no billing period)
                    if (at + 1 < cells.size() && !isLabel(cells.get(at + 1).text()) && !cells.get(at + 1).text().endsWith(":")
                            && fits(f, cells.get(at + 1).text())) continue;
                    // the first line below with a cell under the label (other columns' lines may come between)
                    int j = i + 1;
                    while (j < lines.size() && j <= i + 4
                            && lines.get(j).stream().noneMatch(v -> Math.abs(v.x() - label.x()) <= 4
                                    || (cells.size() == 1 && Math.abs((v.x() + v.end()) / 2 - (label.x() + label.end()) / 2) <= 10))) j++;
                    if (j >= lines.size() || j > i + 4) continue;
                    List<Cell> below = lines.get(j);
                    for (Cell v : below) {
                        boolean under = Math.abs(v.x() - label.x()) <= 4
                                || (cells.size() == 1 && Math.abs((v.x() + v.end()) / 2 - (label.x() + label.end()) / 2) <= 10);
                        if (!under || usedCells.contains(j + ":" + v.x()) || isLabel(v.text()) || !fits(f, v.text())) continue;
                        add(i, label, label.text(), v.text(), BillingDocument.Form.LABEL_ABOVE);
                        used(j, v);
                        break;
                    }
                }
            }
        }

        /** The printed form of a known label the cell starts with, longest first. */
        private static @Nullable String knownPrefix(String cell) {
            String lower = cell.toLowerCase(Locale.ROOT);
            String best = null;
            for (String label : LABELS.keySet()) {
                if (label.length() < 3) continue;
                Matcher m = Pattern.compile("^" + Pattern.quote(label).replace(" ", "\\E[\\s.:#]+\\Q") + "\\.?(?=[\\s:#]|$)")
                        .matcher(lower);
                if (m.find() && (best == null || m.end() > best.length())) best = cell.substring(0, m.end());
            }
            return best;
        }

        private void add(int line, Cell cell, String label, String value, BillingDocument.Form form) {
            used(line, cell);
            String l = label.strip().replaceAll("[:\\s]+$", "");
            Field f = field(l);
            pairs.add(new Pair(line, cell.x(), l, value.strip(), fits(f, value) ? f : null, form));
        }

        /** Notes how a field was read: from its chosen labelled value. */
        void source(String field, @Nullable Field f) {
            if (f == null) return;
            best(f).ifPresent(p -> sources.put(field, new BillingDocument.Source(field, p.label(), p.value(), p.line(), p.form())));
        }

        private void used(int line, Cell cell) {
            usedCells.add(line + ":" + cell.x());
        }

        Optional<Pair> best(Field f) {
            return pairs.stream().filter(p -> p.field() == f)
                    .min(Comparator.comparingInt((Pair p) -> f == Field.NUMBER && ownNumber(p.label()) ? 0 : 1)
                            .thenComparingInt(p -> RANK.getOrDefault(normalize(p.label()), 99))
                            .thenComparingInt(Pair::line));
        }

        /** A receipt's own number is its receipt number, an invoice's its invoice number, a bill's its statement's. */
        private boolean ownNumber(String label) {
            String l = normalize(label);
            return switch (kind) {
                case RECEIPT -> l.contains("receipt") || l.startsWith("or ");
                case INVOICE -> l.contains("invoice") || l.startsWith("document");
                case BILL -> l.contains("statement") || l.contains("bill");
            };
        }

        Optional<String> string(Field f) {
            return best(f).map(Pair::value).filter(s -> !s.isBlank());
        }

        Optional<LocalDate> date(Field f) {
            return best(f).map(p -> parseDate(p.value()));
        }

        Optional<Money> money(Field f) {
            Optional<Money> labelled = pairs.stream().filter(p -> p.field() == f)
                    .sorted(Comparator.comparingInt((Pair p) -> RANK.getOrDefault(normalize(p.label()), 99))
                            .thenComparingInt(Pair::line))
                    .map(p -> parseMoney(p.value(), currency)).flatMap(Optional::stream).findFirst();
            return labelled.or(() -> Optional.ofNullable(tableTotals.get(f)));
        }

        Optional<DateRange> period() {
            return best(Field.PERIOD).map(p -> range(p.value()));
        }

        // parties

        private void readParties() {
            Set<Integer> partyLines = new HashSet<>();
            for (Pair p : List.copyOf(pairs)) {
                if (p.field() != Field.ISSUER && p.field() != Field.CUSTOMER) continue;
                List<String> block = block(p, partyLines);
                Party party = party(block);
                if (party == null) continue;
                if (p.field() == Field.ISSUER && issuer == null) issuer = party;
                if (p.field() == Field.CUSTOMER && customer == null) customer = party;
            }
            // header-only labels ("Bill to" alone, the block below it)
            for (int i = 0; i < lines.size(); i++) {
                for (Cell c : lines.get(i)) {
                    Field f = field(c.text());
                    if ((f != Field.ISSUER && f != Field.CUSTOMER) || usedCells.contains(i + ":" + c.x())) continue;
                    if ((f == Field.ISSUER ? issuer : customer) != null) continue;
                    List<String> block = column(i + 1, c.x(), partyLines);
                    Party party = party(block);
                    if (party == null) continue;
                    if (f == Field.ISSUER) issuer = party; else customer = party;
                    used(i, c);
                }
            }
            // a business name near the top ("Skyline Telecom, Inc.") is the issuer
            if (issuer == null) {
                outer:
                for (int i = 0; i < Math.min(lines.size(), 12); i++) {
                    for (Cell c : lines.get(i)) {
                        if (usedCells.contains(i + ":" + c.x())) continue;
                        if (BUSINESS.matcher(c.text()).find() && c.text().length() <= 50 && !kindWord(c.text())) {
                            issuer = Party.of(c.text().replaceAll("[.,]+$", ""));
                            issuerHow = new BillingDocument.Source("issuer", "", c.text(), i, BillingDocument.Form.INFERRED);
                            break outer;
                        }
                    }
                }
            }
            // unlabelled address blocks: a person's is the customer's; otherwise the first is the issuer's
            for (List<String> block : addressBlocks(partyLines)) {
                Party party = party(block);
                if (party == null) continue;
                if (party.isPerson() && customer == null && kind != Kind.RECEIPT) customer = party;
                else if (issuer == null) issuer = party;
                else if (customer == null && !party.name().equals(issuer.name())) customer = party;
            }
            // a title line naming the issuer ("SKYLINE TELECOM", "BEAN & LEAF CAFE")
            if (issuer == null) {
                for (int i = 0; i < Math.min(lines.size(), 8); i++) {
                    if (lines.get(i).isEmpty()) continue;
                    Cell first = lines.get(i).getFirst();
                    String t = first.text();
                    if (usedCells.contains(i + ":" + first.x()) || kindWord(t) || !t.matches(".*\\p{L}{3}.*")
                            || t.matches("(?i)^(hello|hi|dear)\\b.*") || t.length() > 50) continue;
                    List<String> block = new ArrayList<>(List.of(t));
                    for (int j = i + 1; j < Math.min(lines.size(), i + 4); j++) {
                        if (lines.get(j).isEmpty()) continue;
                        String next = lines.get(j).getFirst().text();
                        if (lines.get(j).size() == 1 && !isLabel(next) && knownPrefix(next) == null && !kindWord(next)
                                && !next.contains(":")) block.add(next);
                        else break;
                    }
                    issuer = party(block);
                    break;
                }
            }
        }

        /** A line that can belong to a party's block: not an amount, a date, a label or a sentence. */
        private static boolean partyLine(String s) {
            String t = s.strip();
            if (t.isEmpty() || t.length() > 60) return false;
            if (looksLikeMoney(t) || t.matches(".*[$€£¥₱]\\s?\\d.*") || t.matches("(?i).*\\b[A-Z]{3}\\s?\\$?\\d[\\d,]*\\.\\d{2}.*")) return false;
            if (range(t) != null || (parseDate(t) != null && !t.matches(".*\\p{L}{4,}.*\\p{L}{4,}.*"))) return false;
            if (isLabel(t) || t.matches("(?i).*\\b(paid on|page \\d|description|amount|qty|subtotal|total)\\b.*")) return false;
            return true;
        }

        private static boolean contactLine(String s) {
            var found = ValueObject.find(s);
            return found.containsKey(ValueObject.Type.EMAIL) || found.containsKey(ValueObject.Type.PHONE_NUMBER)
                    || found.containsKey(ValueObject.Type.LINK) || TAX_LINE.matcher(s.strip()).matches();
        }

        /** Adds a line to a block; after the country only contact lines (email, phone, website, tax id) follow. */
        private static boolean accept(List<String> block, String line) {
            if (!partyLine(line)) return false;
            boolean pastCountry = block.stream().anyMatch(l -> countryOf(l) != null);
            if (pastCountry && !contactLine(line)) return false;
            block.add(line);
            return true;
        }

        private static boolean kindWord(String s) {
            return s.toLowerCase(Locale.ROOT).matches(
                    ".*\\b(receipt|invoice|statement|bill|billing|payment confirmation|official)\\b.*");
        }

        /** The block a party label names: its value, then the lines below starting at the value's column. */
        private List<String> block(Pair p, Set<Integer> taken) {
            List<String> out = new ArrayList<>();
            if (!p.value().isBlank()) out.add(p.value());
            List<Cell> line = lines.get(p.line());
            int x = line.stream().filter(c -> c.text().equals(p.value()) || c.text().endsWith(p.value()))
                    .mapToInt(Cell::x).findFirst().orElse(p.x());
            if (p.value().isBlank()) x = p.x();
            int blanks = 0;
            for (int j = p.line() + 1; j < lines.size() && out.size() < 7; j++) {
                List<Cell> cells = lines.get(j);
                if (cells.isEmpty()) {
                    if (++blanks > 1) break;
                    continue;
                }
                int col = x;
                // another column's line (all its cells to the right): step over it
                if (cells.getFirst().x() > col + 12) continue;
                // another label to the left ends the block ("Bill to:" under "Bill from:")
                if (cells.getFirst().x() < col - 2) break;
                Optional<Cell> here = cells.stream().filter(c -> Math.abs(c.x() - col) <= 4).findFirst();
                // a tax id under the party ("VAT ID: 555012345") is the party's, though read as a labelled value
                int lineNo = j;
                Optional<Pair> tax = pairs.stream().filter(q -> q.line() == lineNo && q.field() == Field.TAX_ID
                        && Math.abs(q.x() - col) <= 4).findFirst();
                if (tax.isPresent()) {
                    out.add(tax.get().label() + ": " + tax.get().value());
                    blanks = 0;
                    continue;
                }
                if (here.isEmpty() || usedCells.contains(j + ":" + here.get().x())) break;
                if (!accept(out, here.get().text())) break;
                blanks = 0;
                used(j, here.get());
                taken.add(j);
            }
            return out;
        }

        /** The lines from {@code start} down whose cell starts at column {@code x} (a block under a label). */
        private List<String> column(int start, int x, Set<Integer> taken) {
            List<String> out = new ArrayList<>();
            int blanks = 0;
            for (int j = start; j < lines.size() && out.size() < 7; j++) {
                List<Cell> cells = lines.get(j);
                if (cells.isEmpty()) {
                    if (++blanks > 1) break;
                    continue;
                }
                Optional<Cell> here = cells.stream().filter(c -> Math.abs(c.x() - x) <= 6).findFirst();
                if (here.isEmpty() && cells.stream().allMatch(c -> c.x() > x + 12 || c.end() < x - 4)) continue;
                if (here.isEmpty() || usedCells.contains(j + ":" + here.get().x())) {
                    if (out.isEmpty() && j < start + 2) continue;
                    break;
                }
                if (!accept(out, here.get().text())) break;
                blanks = 0;
                used(j, here.get());
                taken.add(j);
            }
            return out;
        }

        /** Unlabelled blocks of 2+ lines at one column that hold an address, a country or an email. */
        private List<List<String>> addressBlocks(Set<Integer> taken) {
            List<List<String>> out = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < lines.size(); i++) {
                for (Cell c : lines.get(i)) {
                    String key = i + ":" + c.x();
                    if (usedCells.contains(key) || seen.contains(key)) continue;
                    List<String> block = new ArrayList<>();
                    int blanks = 0;
                    for (int j = i; j < lines.size() && block.size() < 7; j++) {
                        if (lines.get(j).isEmpty()) {
                            if (++blanks > 1) break;
                            continue;
                        }
                        Optional<Cell> here = lines.get(j).stream().filter(o -> Math.abs(o.x() - c.x()) <= 2).findFirst();
                        if (here.isEmpty() && lines.get(j).stream().allMatch(o -> o.x() > c.end() + 8 || o.end() < c.x() - 4)) continue;
                        if (here.isEmpty() || usedCells.contains(j + ":" + here.get().x())) break;
                        if (!accept(block, here.get().text())) break;
                        blanks = 0;
                        seen.add(j + ":" + here.get().x());
                    }
                    if (block.size() >= 2 && block.stream().anyMatch(l -> countryOf(l) != null || cityCountry(l) != null
                            || (POSTAL.matcher(l).find() && l.contains(",")) || Email.isValid(l))) {
                        out.add(block);
                    }
                }
            }
            return out;
        }

        private static final Pattern POSTAL = Pattern.compile("(?<![\\d-])\\d{4,6}(?![\\d-])");
        private static final Pattern BUSINESS = Pattern.compile(
                "(?i)\\b(inc|llc|ltd|corp|corporation|co|company|gmbh|plc|pty|telecom|bank|cafe|store|shop|restaurant|enterprises?|trading|services)\\b\\.?");

        /** The country of a city named in the line ("Cebu City" -> Philippines). */
        static @Nullable Country cityCountry(String line) {
            for (String part : line.split(",")) {
                Country c = CITIES.get(part.strip().toLowerCase(Locale.ROOT));
                if (c != null) return c;
            }
            return null;
        }
        private static final Pattern TAX_LINE = Pattern.compile("(?i)^(vat id|vat no|vat number|tax id|tin)[:\\s.#]*(.+)$");

        private @Nullable Party party(List<String> block) {
            List<String> lines = block.stream().map(String::strip).filter(s -> !s.isEmpty()).toList();
            if (lines.isEmpty()) return null;
            Email email = null;
            PhoneNumber phone = null;
            Link website = null;
            String taxId = null;
            List<String> addressLines = new ArrayList<>();
            String name = null;
            for (String l : lines) {
                Matcher tax = TAX_LINE.matcher(l);
                if (tax.matches()) {
                    taxId = tax.group(2).strip();
                    continue;
                }
                var found = ValueObject.find(l);
                boolean contactOnly = false;
                if (found.containsKey(ValueObject.Type.EMAIL)) {
                    if (email == null) email = (Email) found.get(ValueObject.Type.EMAIL).getFirst();
                    contactOnly = l.strip().equalsIgnoreCase(found.get(ValueObject.Type.EMAIL).getFirst().toString());
                }
                if (found.containsKey(ValueObject.Type.PHONE_NUMBER)) {
                    if (phone == null) phone = (PhoneNumber) found.get(ValueObject.Type.PHONE_NUMBER).getFirst();
                    contactOnly = contactOnly || l.replaceAll("[\\d\\s()+.-]", "").isEmpty();
                }
                if (found.containsKey(ValueObject.Type.LINK)) {
                    if (website == null) website = (Link) found.get(ValueObject.Type.LINK).getFirst();
                    contactOnly = true;
                }
                if (contactOnly) continue;
                if (name == null && !l.matches("^\\d.*") && !isLabel(l)) {
                    name = l;
                    continue;
                }
                addressLines.add(l);
            }
            if (name == null) return null;
            Address address = address(addressLines);
            if (address != null) addresses.add(address);
            return new Party(name, Optional.ofNullable(address), Optional.ofNullable(email), Optional.ofNullable(phone),
                    Optional.ofNullable(website), Optional.ofNullable(taxId).flatMap(BillingDocumentReader::taxId), lines);
        }

        private static @Nullable Address address(List<String> lines) {
            if (lines.isEmpty()) return null;
            List<String> rest = new ArrayList<>(lines);
            Country country = null;
            for (int i = rest.size() - 1; i >= 0 && country == null; i--) {
                Country c = countryOf(rest.get(i));
                if (c != null) {
                    country = c;
                    rest.remove(i);
                }
            }
            if (country == null) {
                for (String l : rest) {
                    Country c = cityCountry(l);
                    if (c != null) {
                        country = c;
                        break;
                    }
                }
            }
            boolean addressLike = country != null || rest.stream().anyMatch(l -> l.matches(
                    "(?i)^\\d.*|.*\\b(st|street|ave|avenue|road|rd|blvd|lot|block|unit|suite|ste|barangay|brgy)\\b.*"));
            if (rest.isEmpty() || !addressLike) return country == null ? null : Address.of(country);
            String postal = null;
            // a line that is only the postal code ("9000")
            for (int i = rest.size() - 1; i >= 0; i--) {
                if (rest.get(i).strip().matches("\\d{4,6}")) {
                    postal = rest.remove(i).strip();
                    break;
                }
            }
            String city = null;
            String state = null;
            for (int i = rest.size() - 1; i >= 0; i--) {
                Matcher m = POSTAL.matcher(rest.get(i));
                if (postal == null && m.find() && rest.get(i).contains(",")) {
                    postal = m.group();
                    String line = rest.remove(i);
                    String[] parts = line.replace(postal, "").split(",");
                    city = parts[0].strip();
                    if (parts.length > 1 && !parts[1].isBlank()) state = parts[1].strip();
                    break;
                }
            }
            if (city == null && rest.size() >= 2) {
                String[] parts = rest.removeLast().split(",");
                city = parts[0].strip();
                if (parts.length > 1 && !parts[1].isBlank()) state = parts[1].strip();
            }
            String street = rest.isEmpty() ? null : rest.removeFirst();
            String line2 = rest.isEmpty() ? null : String.join(", ", rest);
            return Address.full(street, line2, city, state, postal, country == null ? Country.UNKNOWN : country);
        }

        /** A line that is a country's name ("United States", "Philippines"), or ends in one after a comma. */
        static @Nullable Country countryOf(String line) {
            String s = line.strip();
            if (s.length() < 4) return null;
            Country c = Country.fromAny(s);
            if (c != null && c != Country.UNKNOWN && s.length() > 3) return c;
            int comma = s.lastIndexOf(',');
            if (comma > 0) {
                String last = s.substring(comma + 1).strip();
                if (last.length() >= 4) {
                    c = Country.fromAny(last);
                    if (c != null && c != Country.UNKNOWN) return c;
                }
            }
            return null;
        }

        // currency

        private static final Pattern CODE_AMOUNT = Pattern.compile("\\b([A-Z]{3})\\s?[$€£¥₱]?\\s?-?\\d|\\d\\s?([A-Z]{3})\\b");

        private @Nullable Currency currency() {
            Map<Currency, Integer> votes = new LinkedHashMap<>();
            Matcher m = CODE_AMOUNT.matcher(text);
            while (m.find()) {
                String code = m.group(1) != null ? m.group(1) : m.group(2);
                Currency.tryParse(code).ifPresent(c -> votes.merge(c, 1, Integer::sum));
            }
            if (!votes.isEmpty()) {
                currencyHow = "the currency code most amounts are written with";
                return votes.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
            }
            for (var e : SYMBOLS.entrySet()) {
                if (!e.getKey().equals("$") && text.contains(e.getKey())) {
                    currencyHow = "the " + e.getKey() + " symbol";
                    return e.getValue();
                }
            }
            // no currency printed: the one of the country the document is from; with a "$", that country's dollar
            // when it has one (AUD, CAD, SGD, ...), else US dollars
            boolean dollar = text.contains("$");
            Country country = country();
            if (country != null) {
                Optional<FiatCurrency> local = FiatCurrency.forCountry(country);
                if (local.isPresent() && (!dollar || local.get().symbol().contains("$"))) {
                    currencyHow = dollar ? "the $ symbol, in " + country.displayName() + ", whose currency is "
                            + local.get().code() : "the currency of " + country.displayName() + ", where it is from";
                    return local.get();
                }
            }
            if (text.contains("$")) {
                currencyHow = "the $ symbol";
                return FiatCurrency.USD;
            }
            return null;
        }

        /** The issuer's country, else the customer's, else the first country named on its own line. */
        @Nullable Country country() {
            for (Party p : java.util.Arrays.asList(issuer, customer)) {
                if (p != null && p.address().isPresent() && p.address().get().country() != Country.UNKNOWN) {
                    return p.address().get().country();
                }
            }
            for (Address a : addresses) if (a.country() != Country.UNKNOWN) return a.country();
            for (List<Cell> line : lines) {
                for (Cell c : line) {
                    Country found = countryOf(c.text());
                    if (found == null) found = cityCountry(c.text());
                    if (found != null) return found;
                    // "Cebu City", "Davao City": a city whose country is the text's
                    String[] parts = c.text().split(",");
                    for (String part : parts) {
                        Country byCity = CITIES.get(part.strip().toLowerCase(Locale.ROOT));
                        if (byCity != null) return byCity;
                    }
                }
            }
            return null;
        }

        // line items

        private void readLineItems() {
            Table best = null;
            int bestScore = 0;
            for (Table t : tables) {
                int score = 0;
                if (t.columns().stream().anyMatch(c -> role(c.name()) == Role.DESCRIPTION)) score += 3;
                if (t.columns().stream().anyMatch(c -> role(c.name()) == Role.AMOUNT)) score += 3;
                if (t.columns().stream().anyMatch(c -> c.numeric() || c.type().filter(ValueObject.Type.MONEY::equals).isPresent())) score += 2;
                score += Math.min(3, t.height());
                if (score > bestScore) {
                    bestScore = score;
                    best = t;
                }
            }
            if (best == null || bestScore < 6) return;

            Map<Role, Integer> columns = new EnumMap<>(Role.class);
            for (Table.Column c : best.columns()) {
                Role r = role(c.name());
                if (r != null) columns.putIfAbsent(r, c.index());
            }
            List<Table.Column> moneyLike = best.columns().stream()
                    .filter(c -> c.numeric() || c.type().filter(ValueObject.Type.MONEY::equals).isPresent()).toList();
            if (!columns.containsKey(Role.AMOUNT) && !moneyLike.isEmpty()) columns.put(Role.AMOUNT, moneyLike.getLast().index());
            if (!columns.containsKey(Role.DESCRIPTION)) {
                best.columns().stream().filter(c -> !c.numeric() && c.type().isEmpty()).findFirst()
                        .ifPresent(c -> columns.put(Role.DESCRIPTION, c.index()));
            }

            for (int r = 0; r < best.height(); r++) {
                List<String> row = best.rows().get(r);
                String description = cell(row, columns.get(Role.DESCRIPTION));
                // a totals row: "Subtotal", "VAT (12%)", "Total" in any text cell
                Field total = null;
                for (String c : row) {
                    Field f = c.isBlank() ? null : field(c);
                    if (f == Field.TOTAL || f == Field.SUBTOTAL || f == Field.TAX || f == Field.PAID || f == Field.CHANGE) {
                        total = f;
                        break;
                    }
                }
                Optional<Money> amount = parseMoney(cell(row, columns.get(Role.AMOUNT)), currency);
                if (total != null) {
                    if (amount.isEmpty()) {
                        for (int c = row.size() - 1; c >= 0 && amount.isEmpty(); c--) amount = parseMoney(row.get(c), currency);
                    }
                    Field f = total;
                    amount.ifPresent(a -> tableTotals.putIfAbsent(f, a));
                    continue;
                }
                Map<String, String> printed = new LinkedHashMap<>();
                for (Table.Column c : best.columns()) {
                    if (c.index() < row.size() && !row.get(c.index()).isBlank()) printed.put(c.name(), row.get(c.index()));
                }
                Optional<io.joshuasalcedo.valueobject.measurement.Quantity> quantity =
                        quantity(cell(row, columns.get(Role.QUANTITY)), cell(row, columns.get(Role.UOM)));
                Optional<Money> unit = parseMoney(cell(row, columns.get(Role.UNIT_PRICE)), currency);
                if (amount.isEmpty() && unit.isEmpty() && quantity.isEmpty()) {
                    // a wrapped line ("Dec 15 - Jan 14, 2026"): part of the line above
                    if (!items.isEmpty() && !description.isBlank()) {
                        LineItem last = items.removeLast();
                        Map<String, String> joined = new LinkedHashMap<>(last.printed());
                        printed.forEach((k, v) -> joined.merge(k, v, (a, b) -> a + " " + b));
                        items.add(new LineItem(last.description() + " " + description, last.quantity(), last.unitPrice(),
                                last.amount(), last.other(), joined));
                    }
                    continue;
                }
                Map<String, String> other = new LinkedHashMap<>();
                for (Table.Column c : best.columns()) {
                    if (!columns.containsValue(c.index()) && !row.get(c.index()).isBlank()) other.put(c.name(), row.get(c.index()));
                }
                if (description.isBlank() && amount.isEmpty() && unit.isEmpty()) continue;
                items.add(new LineItem(description, quantity, unit, amount, other, printed));
            }
        }

        private static String cell(List<String> row, @Nullable Integer column) {
            return column == null || column >= row.size() ? "" : row.get(column);
        }

        // the result

        BillingDocument.Fields fields(Set<Field> ownFields) {
            Money subtotal = money(Field.SUBTOTAL).orElse(null);
            Money tax = money(Field.TAX).orElse(null);
            Money total = money(Field.TOTAL).or(() -> money(Field.PAID)).orElse(null);
            if (total == null && !items.isEmpty() && currency != null) {
                Money sum = Money.zero(currency);
                boolean all = true;
                for (LineItem i : items) {
                    Optional<Money> t = i.total();
                    if (t.isPresent() && t.get().currency().code().equals(currency.code())) sum = sum.add(t.get());
                    else all = false;
                }
                if (all) total = sum;
            }
            Set<Field> consumed = new HashSet<>(ownFields);
            consumed.addAll(List.of(Field.NUMBER, Field.DATE, Field.TOTAL, Field.SUBTOTAL, Field.TAX, Field.ISSUER,
                    Field.CUSTOMER));
            String number = string(Field.NUMBER).or(() -> string(Field.REFERENCE)).orElse(null);
            Set<String> itemLabels = new HashSet<>();
            for (LineItem i : items) itemLabels.add(normalize(i.description()));

            Map<String, Object> metadata = new LinkedHashMap<>();
            for (Pair p : pairs) {
                if (p.field() != null && consumed.contains(p.field())) {
                    // keep a second, different value of a field (the invoice number on a receipt)
                    boolean chosen = best(p.field()).filter(b -> b.equals(p)).isPresent();
                    if (chosen || p.field() == Field.ISSUER || p.field() == Field.CUSTOMER) continue;
                }
                if (itemLabels.contains(normalize(p.label())) || !p.label().matches(".*\\p{L}{2}.*")) continue;
                metadata.putIfAbsent(p.label(), value(p.value()));
            }
            if (!addresses.isEmpty()) metadata.put("addresses", List.copyOf(addresses));
            putDetected(metadata, "emails", ValueObject.Type.EMAIL);
            putDetected(metadata, "phones", ValueObject.Type.PHONE_NUMBER);
            putDetected(metadata, "links", ValueObject.Type.LINK);
            Country country = country();
            if (country != null) metadata.put("country", country);

            LocalDate date = date(Field.DATE).or(() -> date(Field.PAID_ON)).orElse(null);
            source("number", best(Field.NUMBER).isPresent() ? Field.NUMBER : Field.REFERENCE);
            source("date", best(Field.DATE).isPresent() ? Field.DATE : Field.PAID_ON);
            source("subtotal", Field.SUBTOTAL);
            source("tax", Field.TAX);
            source("total", best(Field.TOTAL).isPresent() ? Field.TOTAL : Field.PAID);
            for (Field f : ownFields) source(fieldName(f), f);
            for (var e : List.of(Map.entry("subtotal", Field.SUBTOTAL), Map.entry("tax", Field.TAX), Map.entry("total", Field.TOTAL))) {
                if (!sources.containsKey(e.getKey()) && tableTotals.containsKey(e.getValue())) {
                    sources.put(e.getKey(), new BillingDocument.Source(e.getKey(), "", tableTotals.get(e.getValue()).toString(), -1,
                            BillingDocument.Form.TABLE_ROW));
                }
            }
            if (total != null && !sources.containsKey("total")) {
                sources.put("total", new BillingDocument.Source("total", "", total.toString(), -1, BillingDocument.Form.INFERRED));
            }
            partySource("issuer", issuer);
            if (issuerHow != null && issuer != null && issuer.name().equals(issuerHow.value().replaceAll("[.,]+$", ""))) sources.put("issuer", issuerHow);
            partySource("customer", customer);
            if (!items.isEmpty()) {
                sources.put("lineItems", new BillingDocument.Source("lineItems", "", items.size() + " lines", -1,
                        BillingDocument.Form.TABLE_ROW));
            }
            if (currency != null && currencyHow != null) {
                sources.put("currency", new BillingDocument.Source("currency", "", currencyHow, -1,
                        currencyHow.startsWith("the currency code") ? BillingDocument.Form.LABEL_BESIDE : BillingDocument.Form.INFERRED));
            }
            return new BillingDocument.Fields(number, date, issuer, customer, items, tables,
                    subtotal, tax, total, currency, metadata, found, sources, text);
        }

        private void partySource(String field, @Nullable Party party) {
            if (party == null || sources.containsKey(field)) return;
            Field f = field.equals("issuer") ? Field.ISSUER : Field.CUSTOMER;
            Optional<Pair> labelled = best(f);
            int line = -1;
            for (int i = 0; i < lines.size() && line < 0; i++) {
                for (Cell c : lines.get(i)) if (c.text().equals(party.name())) line = i;
            }
            sources.put(field, new BillingDocument.Source(field, labelled.map(Pair::label).orElse(""),
                    String.join(" / ", party.lines()), line, BillingDocument.Form.BLOCK));
        }

        private static String fieldName(Field f) {
            return switch (f) {
                case DUE -> "dueDate";
                case PO -> "purchaseOrder";
                case ACCOUNT -> "accountNumber";
                case PERIOD -> "billingPeriod";
                case METHOD -> "paymentMethod";
                case PAID_ON -> "paidOn";
                case PAID -> "amountPaid";
                case CHANGE -> "change";
                case REFERENCE -> "reference";
                case TERMS -> "paymentTerms";
                default -> f.name().toLowerCase(Locale.ROOT);
            };
        }

        private void putDetected(Map<String, Object> metadata, String key, ValueObject.Type type) {
            List<ValueObject> values = detected.get(type);
            if (values != null && !values.isEmpty()) metadata.put(key, List.copyOf(values));
        }

        private Object value(String s) {
            Optional<ValueObject> vo = ValueObject.parse(s);
            if (vo.isPresent()) return vo.get();
            Matcher dm = DATE_TEXT.matcher(s);
            if (dm.find() && dm.group().length() >= s.strip().length() * 0.7) {
                LocalDate d = parseDate(s);
                if (d != null) return d;
            }
            if (looksLikeMoney(s) && s.matches("(?i).*([$€£¥₱₹₩₿]|\\b[A-Z]{3}\\b).*") && currency != null) {
                Optional<Money> m = parseMoney(s, currency);
                if (m.isPresent()) return m.get();
            }
            return s;
        }
    }

    private static Optional<io.joshuasalcedo.valueobject.identifier.TaxId> taxId(String text) {
        try {
            return Optional.of(io.joshuasalcedo.valueobject.identifier.TaxId.parse(text));
        } catch (io.joshuasalcedo.valueobject.exception.ValueObjectException e) {
            return Optional.empty();
        }
    }

    // ── Line-item columns ─────────────────────────────────────────────────

    enum Role { DESCRIPTION, QUANTITY, UOM, UNIT_PRICE, AMOUNT }

    static @Nullable Role role(String header) {
        String h = normalize(header);
        if (h.matches(".*\\b(unit price|unit cost|price each|rate|price)\\b.*")) return Role.UNIT_PRICE;
        if (h.matches("(uom|u m|um|unit|units|unit of measure|measure|unit measure)")) return Role.UOM;
        if (h.matches(".*\\b(qty|quantity|units|hrs|hours|pcs)\\b.*")) return Role.QUANTITY;
        if (h.matches(".*\\b(amount|line total|total|value|charges?)\\b.*")) return Role.AMOUNT;
        if (h.matches(".*\\b(description|item|items|particulars|product|service|details|memo|summary)\\b.*")) return Role.DESCRIPTION;
        return null;
    }

    // ── Values ────────────────────────────────────────────────────────────

    private static final Map<String, Currency> SYMBOLS = new LinkedHashMap<>();
    static {
        SYMBOLS.put("₱", FiatCurrency.PHP);
        SYMBOLS.put("€", FiatCurrency.EUR);
        SYMBOLS.put("£", FiatCurrency.GBP);
        SYMBOLS.put("¥", FiatCurrency.JPY);
        SYMBOLS.put("₹", FiatCurrency.INR);
        SYMBOLS.put("₩", FiatCurrency.KRW);
        SYMBOLS.put("₿", CryptoCoin.Known.BTC);
        SYMBOLS.put("$", FiatCurrency.USD);
    }

    /** A few cities that name their country on their own ("Cebu City" is in the Philippines). */
    private static final Map<String, Country> CITIES = new HashMap<>();
    static {
        for (String c : List.of("manila", "makati", "makati city", "quezon city", "pasig", "pasig city", "taguig", "cebu",
                "cebu city", "davao", "davao city", "cagayan de oro", "iloilo", "iloilo city", "baguio", "baguio city"))
            CITIES.put(c, Country.PHILIPPINES);
    }

    private static final Pattern AMOUNT = Pattern.compile("\\(?-?\\s?\\d[\\d,]*(?:\\.\\d+)?\\)?");
    private static final Pattern EXPLICIT_CODE = Pattern.compile("\\b([A-Za-z]{3})\\b");

    static boolean looksLikeMoney(String s) {
        String t = s.strip();
        if (t.isEmpty() || !t.matches(".*\\d.*")) return false;
        String stripped = t.replaceAll("(?i)\\b[A-Z]{3}\\b", "").replaceAll("[$€£¥₱₹₩₿\\s]", "");
        return stripped.matches("\\(?-?\\d[\\d,]*(\\.\\d{1,8})?\\)?");
    }

    /**
     * @param s        e.g. {@code "USD 42.50"}, {@code "$34.32"}, {@code "-PHP 1,499.00"}, {@code "390.00"}
     * @param fallback the document's currency, for amounts printed without one
     * @return the amount, or empty when it is not one
     */
    static Optional<Money> parseMoney(@Nullable String s, @Nullable Currency fallback) {
        if (s == null || !looksLikeMoney(s)) return Optional.empty();
        String t = s.strip();
        Currency currency = null;
        Matcher code = EXPLICIT_CODE.matcher(t);
        while (code.find() && currency == null) currency = Currency.tryParse(code.group(1).toUpperCase(Locale.ROOT)).orElse(null);
        if (currency == null) {
            for (var e : SYMBOLS.entrySet()) {
                if (t.contains(e.getKey())) {
                    // "$" means the document's dollar when it has one (AUD, SGD, ...), else US dollars
                    currency = e.getKey().equals("$") && fallback != null && fallback.symbol().contains("$") ? fallback : e.getValue();
                    break;
                }
            }
        }
        if (currency == null) currency = fallback;
        if (currency == null) currency = FiatCurrency.UNKNOWN;
        Matcher m = AMOUNT.matcher(t.replaceAll("(?i)\\b[A-Z]{3}\\b", "").replaceAll("[$€£¥₱₹₩₿]", ""));
        if (!m.find()) return Optional.empty();
        String n = m.group().replace(",", "").replace(" ", "");
        boolean negative = n.startsWith("(") || n.startsWith("-") || t.startsWith("-");
        n = n.replaceAll("[()\\-]", "");
        try {
            BigDecimal amount = new BigDecimal(n);
            return Optional.of(Money.of(negative ? amount.negate() : amount, currency));
        } catch (NumberFormatException | io.joshuasalcedo.valueobject.exception.ValueObjectException e) {
            return Optional.empty();
        }
    }

    /** A quantity cell ("10", "10 pcs", "Nos 3") with the unit column's unit when the cell has none. */
    static Optional<io.joshuasalcedo.valueobject.measurement.Quantity> quantity(String cell, String uom) {
        Optional<io.joshuasalcedo.valueobject.measurement.Quantity> q = io.joshuasalcedo.valueobject.measurement.Quantity.tryParse(cell);
        if (q.isEmpty()) return Optional.empty();
        if (q.get().unit() == io.joshuasalcedo.valueobject.measurement.UnitOfMeasure.ONE && !uom.isBlank()) {
            Optional<io.joshuasalcedo.valueobject.measurement.UnitOfMeasure> u = io.joshuasalcedo.valueobject.measurement.UnitOfMeasure.find(uom);
            if (u.isPresent()) return Optional.of(io.joshuasalcedo.valueobject.measurement.Quantity.of(q.get().amount(), u.get()));
        }
        return q;
    }

    static Optional<BigDecimal> number(@Nullable String s) {
        if (s == null || s.isBlank()) return Optional.empty();
        String t = s.strip().replace(",", "");
        if (!t.matches("-?\\d+(\\.\\d+)?")) return Optional.empty();
        return Optional.of(new BigDecimal(t));
    }

    private static final List<DateTimeFormatter> DATES = new ArrayList<>();
    static {
        for (String p : List.of("MMMM d, uuuu", "MMM d, uuuu", "MMMM d uuuu", "MMM d uuuu", "d MMMM uuuu", "d MMM uuuu",
                "d-MMM-uuuu", "d-MMM-uu", "uuuu-MM-dd", "MM/dd/uuuu", "dd/MM/uuuu", "M/d/uuuu", "MM-dd-uuuu", "dd.MM.uuuu",
                "MMM. d, uuuu", "MM/dd/uu", "M/d/uu", "dd MMMM uuuu")) {
            DATES.add(new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(p).toFormatter(Locale.ENGLISH));
        }
    }
    private static final Pattern DATE_TEXT = Pattern.compile(
            "(?i)\\b(?:\\d{4}-\\d{2}-\\d{2}|\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}|\\d{1,2}[ -](?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.?[ -]\\d{2,4}"
                    + "|(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.? \\d{1,2},? \\d{4})\\b");

    /** The first date in the text, in any common form ("January 15, 2026", "2026-03-10", "15-Jan-2026"). */
    static @Nullable LocalDate parseDate(@Nullable String s) {
        if (s == null) return null;
        Optional<io.joshuasalcedo.valueobject.temporal.CalendarDate> calendar = io.joshuasalcedo.valueobject.temporal.CalendarDate.find(s);
        if (calendar.isPresent()) return calendar.get().value();
        Matcher m = DATE_TEXT.matcher(s);
        if (!m.find()) return null;
        String d = m.group().replaceAll("\\s+", " ").replace("Sept", "Sep");
        if (d.matches(".*[/.-]\\d{2}$")) {
            for (String p : List.of("MM/dd/uu", "M/d/uu", "dd/MM/uu", "d-MMM-uu", "MM-dd-uu", "dd.MM.uu")) {
                try {
                    return LocalDate.parse(d, new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(p)
                            .toFormatter(Locale.ENGLISH));
                } catch (DateTimeParseException next) {
                    // try the next form
                }
            }
        }
        for (DateTimeFormatter f : DATES) {
            try {
                return LocalDate.parse(d, f);
            } catch (DateTimeParseException next) {
                // try the next form
            }
        }
        return null;
    }

    private static final Pattern RANGE = Pattern.compile(
            "(?i)((?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.? \\d{1,2})(?:,? (\\d{4}))?\\s*(?:-|–|to|until)\\s*"
                    + "((?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.? \\d{1,2},? \\d{4})");

    /** "Jan 1 - Jan 31, 2026", "2026-01-01 to 2026-01-31". */
    static @Nullable DateRange range(@Nullable String s) {
        if (s == null) return null;
        Matcher m = RANGE.matcher(s);
        if (m.find()) {
            LocalDate end = parseDate(m.group(3));
            if (end != null) {
                String startText = m.group(1) + ", " + (m.group(2) != null ? m.group(2) : String.valueOf(end.getYear()));
                LocalDate start = parseDate(startText);
                if (start != null) {
                    if (start.isAfter(end)) start = start.minusYears(1);
                    return DateRange.of(start, end);
                }
            }
        }
        Matcher all = DATE_TEXT.matcher(s);
        List<LocalDate> dates = new ArrayList<>();
        while (all.find()) {
            LocalDate d = parseDate(all.group());
            if (d != null) dates.add(d);
        }
        return dates.size() >= 2 && !dates.get(0).isAfter(dates.get(1)) ? DateRange.of(dates.get(0), dates.get(1)) : null;
    }
}
