package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NetworkTypesTest {

    // ---------------- Port

    @Test
    void portParsesTrimmedText() {
        assertEquals(8080, Port.parse(" 8080 ").value());
        assertEquals(Port.HTTPS, Port.of("443"));
        assertEquals("443", Port.HTTPS.toString());
        assertTrue(Port.isValid("0"));
        assertTrue(Port.isValid("65535"));
        assertTrue(Port.isValid(65535));
        assertFalse(Port.isValid(-1));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "65536", "-1", "+80", "8o", "1e3", "٨٠", "99999999999"})
    void portRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> Port.parse(text));
        assertThrows(ValueObjectException.class, () -> Port.of(text));
        assertTrue(Port.tryParse(text).isEmpty());
        assertFalse(Port.isValid(text));
    }

    @Test
    void portRangesAndServices() {
        assertTrue(Port.SSH.isSystemPort());
        assertTrue(Port.SSH.requiresPrivileges());
        assertEquals("System/Well-Known", Port.SSH.category());
        assertTrue(Port.of(8080).isRegisteredPort());
        assertEquals("Registered/User", Port.of(8080).category());
        assertTrue(Port.of(50000).isDynamicPort());
        assertEquals("Dynamic/Private", Port.of(50000).category());
        assertEquals(Optional.of("PostgreSQL"), Port.POSTGRESQL.commonServiceName());
        assertEquals(Optional.empty(), Port.of(12345).commonServiceName());
        assertThrows(ValueObjectException.class, () -> Port.of(70000));
        assertTrue(Port.HTTP.compareTo(Port.HTTPS) < 0);
    }

    // ---------------- OperatingSystem

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource({
            "Windows 11, WINDOWS",
            "Windows Server 2022, WINDOWS",
            "win32, WINDOWS",
            "Darwin, MACOS",
            "Mac OS X, MACOS",
            "macOS, MACOS",
            "Linux, LINUX",
            "GNU/Linux, LINUX",
            "FreeBSD, UNKNOWN",
            "Twin Peaks OS, UNKNOWN",
            "' ', UNKNOWN",
    })
    void readsOsName(String osName, OperatingSystem expected) {
        assertEquals(expected, OperatingSystem.fromOsName(osName));
    }

    @Test
    void darwinIsNotWindows() {
        assertEquals(OperatingSystem.MACOS, OperatingSystem.fromOsName("Darwin"));
        assertEquals(OperatingSystem.UNKNOWN, OperatingSystem.fromOsName(null));
        assertNotNull(OperatingSystem.detect());
    }

    @Test
    void operatingSystemCodesAndParse() {
        assertEquals(OperatingSystem.MACOS, OperatingSystem.parse(" mac "));
        assertEquals(OperatingSystem.WINDOWS, OperatingSystem.parse("WINDOWS"));
        assertEquals(OperatingSystem.LINUX, OperatingSystem.fromCode(" LIN "));
        assertEquals(OperatingSystem.UNKNOWN, OperatingSystem.fromCode(null));
        assertThrows(ValueObjectException.class, () -> OperatingSystem.parse("unknown"));
        assertThrows(ValueObjectException.class, () -> OperatingSystem.parse(null));
        assertTrue(OperatingSystem.tryParse(null).isEmpty());
        assertFalse(OperatingSystem.isValid("beos"));
        assertEquals("\r\n", OperatingSystem.WINDOWS.lineEnding());
        assertEquals("\n", OperatingSystem.LINUX.lineEnding());
        assertEquals("\\", OperatingSystem.WINDOWS.pathSeparator());
        assertEquals("/", OperatingSystem.MACOS.pathSeparator());
        assertTrue(OperatingSystem.MACOS.isUnixLike());
        assertFalse(OperatingSystem.WINDOWS.isUnixLike());
        assertEquals("macOS", OperatingSystem.MACOS.displayName());
    }

    // ---------------- MacAddress

    @ParameterizedTest
    @ValueSource(strings = {"00:1A:2B:3C:4D:5E", "00-1a-2b-3c-4d-5e", "001A.2B3C.4D5E", "001a2b3c4d5e", "  00:1A:2B:3C:4D:5E  "})
    void macReadsEveryForm(String text) {
        MacAddress m = MacAddress.parse(text);
        assertEquals("00:1A:2B:3C:4D:5E", m.toColonFormat());
        assertEquals("00-1A-2B-3C-4D-5E", m.toHyphenFormat());
        assertEquals("001A.2B3C.4D5E", m.toDotFormat());
        assertEquals("001A2B3C4D5E", m.toPlainFormat());
        assertEquals("00:1A:2B", m.oui());
        assertEquals("3C:4D:5E", m.nic());
        assertTrue(MacAddress.isValid(text));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "00:1A:2B:3C:4D", "00:1A:2B:3C:4D:5G", "001A2B3C4D5E6F", "00:1A-2B:3C:4D:5E"})
    void macRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> MacAddress.parse(text));
        assertTrue(MacAddress.tryParse(text).isEmpty());
        assertFalse(MacAddress.isValid(text));
    }

    @Test
    void macBytesAndFlags() {
        assertThrows(ValueObjectException.class, () -> new MacAddress(null));
        assertThrows(ValueObjectException.class, () -> MacAddress.of((String) null));
        assertThrows(ValueObjectException.class, () -> MacAddress.of(new byte[5]));
        byte[] raw = {0, 0x1A, 0x2B, 0x3C, 0x4D, 0x5E};
        MacAddress m = MacAddress.of(raw);
        raw[0] = 9;
        assertEquals(0, m.bytes()[0], "copied in");
        m.bytes()[0] = 9;
        assertEquals(0, m.bytes()[0], "copied out");
        assertTrue(m.isUnicast());
        assertTrue(m.isUniversallyAdministered());
        assertTrue(MacAddress.parse("FF:FF:FF:FF:FF:FF").isBroadcast());
        assertTrue(MacAddress.parse("01:00:5E:00:00:01").isMulticast());
        assertTrue(MacAddress.parse("02:00:00:00:00:01").isLocallyAdministered());
        assertEquals(MacAddress.parse("00-1a-2b-3c-4d-5e"), m);
        assertEquals(MacAddress.parse("00-1a-2b-3c-4d-5e").hashCode(), m.hashCode());
        assertTrue(m.compareTo(MacAddress.parse("FF:FF:FF:FF:FF:FF")) < 0);
    }

    // ---------------- DomainName

    @Test
    void domainNameNormalizes() {
        DomainName d = DomainName.parse(" Billing.Northwind.example. ");
        assertEquals("billing.northwind.example", d.value());
        assertEquals(List.of("billing", "northwind", "example"), d.labels());
        assertEquals("example", d.tld());
        assertEquals("northwind.example", d.registrable().value());
        assertTrue(d.isSubdomainOf(DomainName.parse("northwind.example")));
        assertFalse(DomainName.parse("notnorthwind.example").isSubdomainOf(DomainName.parse("northwind.example")));
        assertEquals("example.com.ph", DomainName.parse("shop.example.com.ph").registrable().value());
        assertEquals("northwind.example", DomainName.parse("andrea@northwind.example").value());
        assertEquals("northwind.example", DomainName.parse("https://northwind.example:8443/a?b#c").value());
    }

    @Test
    void domainNameInternational() {
        DomainName d = DomainName.parse("bücher.example");
        assertEquals("xn--bcher-kva.example", d.value());
        assertEquals("bücher.example", d.unicode());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "localhost", "-bad.example", "a..b", "x.123", "under_score.example"})
    void domainNameRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> DomainName.parse(text));
        assertTrue(DomainName.tryParse(text).isEmpty());
        assertFalse(DomainName.isValid(text));
    }
}
