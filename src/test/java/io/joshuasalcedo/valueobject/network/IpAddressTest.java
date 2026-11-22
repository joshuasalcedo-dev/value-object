package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IpAddressTest {

    // ---------------- IPv6

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(delimiter = '|', value = {
            "::1                                     | ::1",
            "0:0:0:0:0:0:0:1                         | ::1",
            "0000:0000:0000:0000:0000:0000:0000:0001 | ::1",
            "::                                      | ::",
            "2001:DB8:0:0:0:0:0:1                    | 2001:db8::1",
            "2001:db8:0:0:1:0:0:1                    | 2001:db8::1:0:0:1",
            "2001:db8:0:1:1:1:1:1                    | 2001:db8:0:1:1:1:1:1",
            "2001:0db8:0000:0000:0001:0000:0000:0000 | 2001:db8:0:0:1::",
            "fe80::0:0:1                             | fe80::1",
            "1::                                     | 1::",
            "'  2001:db8::1  '                       | 2001:db8::1",
            "::ffff:192.0.2.1                        | ::ffff:192.0.2.1",
            "::FFFF:c000:0201                        | ::ffff:192.0.2.1",
            "0:0:0:0:0:ffff:10.0.0.1                 | ::ffff:10.0.0.1",
            "64:ff9b::192.0.2.33                     | 64:ff9b::c000:221",
    })
    void storesTheRfc5952CanonicalForm(String text, String canonical) {
        Ipv6Address a = Ipv6Address.parse(text);
        assertEquals(canonical, a.value());
        assertEquals(canonical, a.compressed());
        assertEquals(canonical, a.toString());
        assertEquals(a, Ipv6Address.parse(canonical));
        assertEquals(a, Ipv6Address.of(a.toBytes()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "localhost", "example.com", "ip6-localhost", "1.2.3.4", "::1%eth0", "[::1]",
            "1:2:3:4:5:6:7:8:9", "1::2::3", ":::", "1:2:3:4:5:6:7", "12345::1", "g::1", "::1:", ":1::",
            "1:2:3:4:5:6:7:1.2.3.4", "::1.2.3", "::256.1.1.1", "1.2.3.4::", "::١"})
    void rejectsWhatIsNotAnIpv6Literal(String text) {
        assertThrows(ValueObjectException.class, () -> Ipv6Address.parse(text));
        assertTrue(Ipv6Address.tryParse(text).isEmpty());
        assertFalse(Ipv6Address.isValid(text));
    }

    @Test
    void hostNamesAreNeverLookedUp() {
        // getByName would resolve these; a literal parser rejects them at once
        long start = System.nanoTime();
        assertFalse(Ipv6Address.isValid("this-host-does-not-exist.invalid"));
        assertFalse(Ipv4Address.isValid("localhost"));
        assertTrue(System.nanoTime() - start < 1_000_000_000L);
    }

    @Test
    void ipv4MappedAddressesWork() {
        Ipv6Address mapped = Ipv6Address.parse("::ffff:192.0.2.1");
        assertTrue(mapped.isIpv4Mapped());
        assertEquals(Ipv4Address.of("192.0.2.1"), mapped.toIpv4());
        assertNull(Ipv6Address.parse("2001:db8::1").toIpv4());
        assertFalse(Ipv6Address.LOCALHOST.isIpv4Mapped());
    }

    @Test
    void ipv6Properties() {
        assertTrue(Ipv6Address.LOCALHOST.isLoopback());
        assertTrue(Ipv6Address.parse("0:0:0:0:0:0:0:1").isLoopback());
        assertTrue(Ipv6Address.ANY.isAny());
        assertFalse(Ipv6Address.LOCALHOST.isAny());
        assertTrue(Ipv6Address.parse("fd12:3456::1").isPrivate());
        assertTrue(Ipv6Address.parse("fe80::1").isLinkLocal());
        assertTrue(Ipv6Address.parse("ff02::1").isMulticast());
        assertFalse(Ipv6Address.parse("2001:db8::1").isPrivate());
        assertEquals(6, Ipv6Address.LOCALHOST.version());
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", Ipv6Address.LOCALHOST.expanded());
        assertEquals(16, Ipv6Address.LOCALHOST.toBytes().length);
    }

    @Test
    void ipv6BytesFactoryChecksLength() {
        assertThrows(ValueObjectException.class, () -> Ipv6Address.of((byte[]) null));
        assertThrows(ValueObjectException.class, () -> Ipv6Address.of(new byte[4]));
        assertEquals(Ipv6Address.ANY, Ipv6Address.of(new byte[16]));
        assertThrows(ValueObjectException.class, () -> new Ipv6Address(null));
    }

    // ---------------- IPv4

    @Test
    void ipv4ParsesAndReports() {
        Ipv4Address a = Ipv4Address.parse(" 192.168.1.10 ");
        assertEquals("192.168.1.10", a.value());
        assertArrayEquals(new int[]{192, 168, 1, 10}, a.octets());
        assertEquals(3232235786L, a.toLong());
        assertTrue(a.isPrivate());
        assertEquals('C', a.networkClass());
        assertEquals(4, a.version());
        assertTrue(Ipv4Address.LOCALHOST.isLoopback());
        assertTrue(Ipv4Address.BROADCAST.isBroadcast());
        assertTrue(Ipv4Address.ANY.isAny());
        assertTrue(Ipv4Address.of(169, 254, 0, 1).isLinkLocal());
        assertTrue(Ipv4Address.of(224, 0, 0, 1).isMulticast());
        assertTrue(Ipv4Address.of("172.16.0.1").isPrivate());
        assertFalse(Ipv4Address.of("172.32.0.1").isPrivate());
        assertEquals(a, Ipv4Address.of(a.toBytes()));
        assertEquals('A', Ipv4Address.of(10, 0, 0, 1).networkClass());
        assertEquals('E', Ipv4Address.of(250, 0, 0, 1).networkClass());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "256.1.1.1", "1.2.3", "01.2.3.4", "1.2.3.4.5", "localhost", "a.b.c.d", "1.2.3.-1"})
    void ipv4RejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> Ipv4Address.parse(text));
        assertTrue(Ipv4Address.tryParse(text).isEmpty());
        assertFalse(Ipv4Address.isValid(text));
    }

    @Test
    void ipv4FactoriesRejectBadInput() {
        assertThrows(ValueObjectException.class, () -> new Ipv4Address(null));
        assertThrows(ValueObjectException.class, () -> Ipv4Address.of(256, 0, 0, 0));
        assertThrows(ValueObjectException.class, () -> Ipv4Address.of(new byte[3]));
        assertThrows(ValueObjectException.class, () -> Ipv4Address.of((byte[]) null));
    }

    // ---------------- IpAddress

    @Test
    void ipAddressPicksTheVersion() {
        assertInstanceOf(Ipv4Address.class, IpAddress.parse("10.0.0.1"));
        assertInstanceOf(Ipv6Address.class, IpAddress.parse("2001:db8::1"));
        assertEquals(Ipv6Address.parse("::ffff:10.0.0.1"), IpAddress.of("::ffff:10.0.0.1"));
        assertTrue(IpAddress.isValid("::1"));
        assertTrue(IpAddress.tryParse("1.2.3.4").isPresent());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "nope", "localhost", "1.2.3.256"})
    void ipAddressThrowsValueObjectException(String text) {
        assertThrows(ValueObjectException.class, () -> IpAddress.of(text));
        assertThrows(ValueObjectException.class, () -> IpAddress.parse(text));
        assertTrue(IpAddress.tryParse(text).isEmpty());
        assertFalse(IpAddress.isValid(text));
    }

    @Test
    void ipv4SortsBeforeIpv6() {
        List<IpAddress> list = new ArrayList<>(List.of(
                IpAddress.of("2001:db8::2"), IpAddress.of("10.0.0.2"), IpAddress.of("2001:db8::1"),
                IpAddress.of("10.0.0.1")));
        Collections.sort(list);
        assertEquals(List.of(IpAddress.of("10.0.0.1"), IpAddress.of("10.0.0.2"), IpAddress.of("2001:db8::1"),
                IpAddress.of("2001:db8::2")), list);
        assertTrue(Ipv6Address.parse("ff00::").compareTo(Ipv6Address.parse("1::")) > 0, "bytes compare unsigned");
    }
}
