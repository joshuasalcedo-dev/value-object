package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.geo.Country;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdentifierTest {

    private static final UUID DNS_NAMESPACE = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    private static final UUID URL_NAMESPACE = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");

    // ---------------- UniqueId

    @Test
    void fromNameIsRfc4122Version3() {
        UniqueId id = UniqueId.fromName(DNS_NAMESPACE, "www.example.com");
        assertEquals("5df41881-3aed-3515-88a7-2f4a814cf09e", id.asString());
        assertEquals(3, id.version());
        assertEquals(2, id.variant());
        assertEquals(id, UniqueId.fromName(DNS_NAMESPACE, "www.example.com"));
        assertNotEquals(id, UniqueId.fromName(URL_NAMESPACE, "www.example.com"));
        assertThrows(ValueObjectException.class, () -> UniqueId.fromName(null, "x"));
        assertThrows(ValueObjectException.class, () -> UniqueId.fromName(DNS_NAMESPACE, null));
    }

    @Test
    void fromNameHashesTheNameInUtf8() {
        // python: uuid.uuid3(uuid.NAMESPACE_DNS, "bücher.example") is computed over the UTF-8 bytes
        UniqueId a = UniqueId.fromName(DNS_NAMESPACE, "bücher.example");
        byte[] name = "bücher.example".getBytes(StandardCharsets.UTF_8);
        byte[] all = new byte[16 + name.length];
        java.nio.ByteBuffer.wrap(all).putLong(DNS_NAMESPACE.getMostSignificantBits())
                .putLong(DNS_NAMESPACE.getLeastSignificantBits()).put(name);
        assertEquals(UUID.nameUUIDFromBytes(all), a.value());
    }

    @Test
    void uniqueIdParsesCanonicalTextOnly() {
        UniqueId id = UniqueId.parse("  123E4567-E89B-12D3-A456-426614174000 ");
        assertEquals("123e4567-e89b-12d3-a456-426614174000", id.toString());
        assertEquals("123e4567e89b12d3a456426614174000", id.compact());
        assertEquals(id.value().getMostSignificantBits(), id.mostSignificantBits());
        assertEquals(id.value().getLeastSignificantBits(), id.leastSignificantBits());
        assertTrue(UniqueId.isValid("123e4567-e89b-12d3-a456-426614174000"));
        assertEquals(4, UniqueId.generate().version());
        assertNotEquals(UniqueId.generate(), UniqueId.generate());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "1-2-3-4-5", "not-a-uuid", "123e4567e89b12d3a456426614174000", "123e4567-e89b-12d3-a456-42661417400g"})
    void uniqueIdRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> UniqueId.parse(text));
        assertTrue(UniqueId.tryParse(text).isEmpty());
        assertFalse(UniqueId.isValid(text));
    }

    @Test
    void uniqueIdNullIsValueObjectException() {
        assertThrows(ValueObjectException.class, () -> new UniqueId(null));
        assertThrows(ValueObjectException.class, () -> UniqueId.of((UUID) null));
        assertThrows(ValueObjectException.class, () -> UniqueId.of((String) null));
    }

    // ---------------- DeterministicUUID

    @Test
    void deterministicUuidSeparatesItsInputs() {
        assertNotEquals(DeterministicUUID.create("ab", "c"), DeterministicUUID.create("a", "bc"));
        assertNotEquals(DeterministicUUID.create("a1:", "b"), DeterministicUUID.create("a", "1:b"));
        assertEquals(DeterministicUUID.create("ab", "c"), DeterministicUUID.create("ab", "c"));
        assertEquals(DeterministicUUID.create("x", 1, null), DeterministicUUID.create("x", "1", "null"));
        assertEquals(3, DeterministicUUID.create("a").version());
        assertEquals(UUID.nameUUIDFromBytes("2:ab1:c".getBytes(StandardCharsets.UTF_8)),
                DeterministicUUID.create("ab", "c"));
        assertThrows(ValueObjectException.class, () -> DeterministicUUID.create((Object[]) null));
    }

    @Test
    void deterministicUuidOfIdentifiers() {
        UniqueId a = UniqueId.parse("123e4567-e89b-12d3-a456-426614174000");
        UniqueId b = UniqueId.fromName(DNS_NAMESPACE, "b");
        assertEquals(DeterministicUUID.create(a, b), DeterministicUUID.create(a, b));
        assertNotEquals(DeterministicUUID.create(a, b), DeterministicUUID.create(b, a));
        assertThrows(ValueObjectException.class, () -> DeterministicUUID.create(a, null));
        assertThrows(ValueObjectException.class, () -> DeterministicUUID.create((UniqueId[]) null));
    }

    // ---------------- Gtin

    @Test
    void gtinParsesAndKnowsItsKind() {
        Gtin ean = Gtin.parse("4006381333931");
        assertEquals(Gtin.Kind.EAN_13, ean.kind());
        assertEquals("04006381333931", ean.toGtin14());
        assertEquals("400", ean.prefix());
        assertEquals(Gtin.Kind.UPC_A, Gtin.parse("036000291452").kind());
        assertEquals(Gtin.Kind.EAN_8, Gtin.parse("96385074").kind());
        assertEquals(Gtin.Kind.GTIN_14, Gtin.parse("10036000291459").kind());
        assertEquals("4006381333931", Gtin.parse(" 400-6381 333931 ").value());
        assertTrue(Gtin.isValid("4006381333931"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "4006381333932", "1234567", "abcdefghijklm", "123456789012345"})
    void gtinRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> Gtin.parse(text));
        assertTrue(Gtin.tryParse(text).isEmpty());
        assertFalse(Gtin.isValid(text));
    }

    @Test
    void gtinOrderingIsInconsistentWithEqualsByDesign() {
        Gtin upc = Gtin.parse("036000291452");
        Gtin ean = Gtin.parse("0036000291452");
        assertNotEquals(upc, ean, "equals compares the digits as given");
        assertEquals(0, upc.compareTo(ean), "ordering compares the 14-digit forms");
        assertEquals(upc.toGtin14(), ean.toGtin14(), "the way to ask whether they are the same product");
        assertEquals(2, new HashSet<>(List.of(upc, ean)).size());
        assertEquals(1, new TreeSet<>(List.of(upc, ean)).size());
    }

    // ---------------- Slug

    @Test
    void slugFromTextAndConversions() {
        Slug s = Slug.fromText("Café Menu: 2026!");
        assertEquals("cafe-menu-2026", s.value());
        assertEquals("/cafe-menu-2026", s.toPath());
        assertEquals(3, s.segmentCount());
        assertEquals("cafeMenu2026", s.toCamelCase());
        assertEquals("CafeMenu2026", s.toPascalCase());
        assertEquals("cafe_menu_2026", s.toSnakeCase());
        assertEquals("cafe-menu-2026-2", s.withSuffix(2).value());
        assertEquals("cafe-menu-2026-draft", s.withSuffix("Draft!").value());
        assertSame(s, s.withSuffix("!!"));
        assertEquals("billing-report", Slug.parse("  Billing-Report ").value());
        assertEquals(100, Slug.fromText("a".repeat(150)).value().length());
        assertThrows(ValueObjectException.class, () -> s.withSuffix(null));
        assertThrows(ValueObjectException.class, () -> Slug.fromText(null));
        assertThrows(ValueObjectException.class, () -> Slug.fromText("!!!"));
        assertTrue(Slug.isValid(" Billing-Report "));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "has space", "-lead", "trail-", "double--hyphen", "ümlaut"})
    void slugRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> Slug.parse(text));
        assertTrue(Slug.tryParse(text).isEmpty());
        assertFalse(Slug.isValid(text));
    }

    // ---------------- JwtToken

    private static String b64(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void jwtReadsClaims() {
        String token = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}") + "."
                + b64("{\"sub\":\"1234567890\",\"iss\":\"northwind\",\"aud\":\"app\",\"jti\":\"j1\",\"iat\":1516239022,\"exp\":4102444800,\"nbf\":1516239022}")
                + ".c2lnbmF0dXJl";
        JwtToken jwt = JwtToken.parse("  " + token + " ");
        assertEquals(token, jwt.value());
        assertEquals("HS256", jwt.algorithm());
        assertEquals("1234567890", jwt.subject());
        assertEquals("northwind", jwt.issuer());
        assertEquals("app", jwt.audience());
        assertEquals("j1", jwt.jwtId());
        assertEquals(Instant.ofEpochSecond(1516239022), jwt.issuedAt());
        assertEquals(Instant.ofEpochSecond(4102444800L), jwt.expirationTime());
        assertEquals(Instant.ofEpochSecond(1516239022), jwt.notBefore());
        assertFalse(jwt.isExpired());
        assertFalse(jwt.isNotYetValid());
        assertTrue(jwt.hasSignature());
        assertEquals(token.length(), jwt.length());
        assertFalse(jwt.toString().contains("c2lnbmF0dXJl"), "toString must not leak the token");
        assertTrue(JwtToken.isValid(token));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "abc", "a.b", "a.b.c.d", "a b.c.d", "a.b!.c"})
    void jwtRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> JwtToken.parse(text));
        assertTrue(JwtToken.tryParse(text).isEmpty());
        assertFalse(JwtToken.isValid(text));
    }

    // ---------------- TaxId, Username, DisplayName

    @Test
    void taxIdReadsItsCountry() {
        assertEquals(Country.PHILIPPINES, TaxId.parse("123-456-789-000").country());
        assertEquals(Country.UNITED_STATES, TaxId.parse("12-3456789").country());
        TaxId vat = TaxId.parse(" DE 123456789 ");
        assertEquals(Country.GERMANY, vat.country());
        assertTrue(vat.isVat());
        assertEquals("123456789000", TaxId.parse("123-456-789-000").digits());
        assertTrue(TaxId.isValid("12-3456789"));
        assertThrows(ValueObjectException.class, () -> TaxId.parse(null));
        assertThrows(ValueObjectException.class, () -> TaxId.parse("abc"));
        assertTrue(TaxId.tryParse(null).isEmpty());
        assertFalse(TaxId.isValid("  "));
        assertEquals(Country.UNKNOWN, TaxId.of("ABC12345", null).country());
    }

    @Test
    void usernameNormalizes() {
        assertEquals("andrea.lim", Username.parse(" @Andrea.Lim ").value());
        assertTrue(Username.isValid("abc"));
        for (String bad : new String[]{null, "", "ab", "a..b", "-abc", "abc-", "a b c", "x".repeat(33)}) {
            assertFalse(Username.isValid(bad), String.valueOf(bad));
            assertTrue(Username.tryParse(bad).isEmpty());
            assertThrows(ValueObjectException.class, () -> Username.parse(bad));
        }
    }

    @Test
    void displayNameCollapsesWhitespace() {
        assertEquals("Andrea Lim", DisplayName.parse("  Andrea \t\n Lim ").value());
        assertTrue(DisplayName.isValid("Andrea"));
        for (String bad : new String[]{null, "", "  ", "x".repeat(65), "a\u0007b"}) {
            assertFalse(DisplayName.isValid(bad), String.valueOf(bad));
            assertTrue(DisplayName.tryParse(bad).isEmpty());
            assertThrows(ValueObjectException.class, () -> DisplayName.parse(bad));
        }
        assertTrue(DisplayName.of("a").compareTo(DisplayName.of("b")) < 0);
    }

    @Test
    void identifiersAreUsableAsSetKeys() {
        Set<Object> s = new HashSet<>(List.of(UniqueId.fromName(DNS_NAMESPACE, "x"), Slug.of("x"), Gtin.parse("96385074")));
        assertTrue(s.contains(UniqueId.fromName(DNS_NAMESPACE, "x")));
        assertTrue(s.contains(Slug.of("X")));
    }
}
