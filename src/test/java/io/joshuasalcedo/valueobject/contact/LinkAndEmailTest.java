package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class LinkAndEmailTest {

    // ---------------- Link

    @Test
    void linkReadsItsParts() {
        Link l = Link.parse("  https://northwind.example:8443/billing/inv?tab=2#top ");
        assertEquals("https://northwind.example:8443/billing/inv?tab=2#top", l.value());
        assertEquals("https", l.scheme());
        assertEquals("northwind.example", l.host());
        assertEquals(8443, l.port().orElseThrow());
        assertEquals("/billing/inv", l.path());
        assertEquals("tab=2", l.query().orElseThrow());
        assertEquals("top", l.fragment().orElseThrow());
        assertTrue(l.isSecure());
        assertEquals("https://northwind.example:8443", l.baseUrl());
    }

    @Test
    void httpsAddsTheScheme() {
        assertEquals("https://northwind.example/billing", Link.https(" northwind.example/billing ").value());
        assertEquals("http://x.example", Link.https("http://x.example").value());
        assertThrows(ValueObjectException.class, () -> Link.https(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "northwind.example", "ht tp://x", "://nohost"})
    void linkRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> Link.parse(text));
        assertTrue(Link.tryParse(text).isEmpty());
        assertFalse(Link.isValid(text));
    }

    @Test
    void linkConstructorNullIsValueObjectException() {
        assertThrows(ValueObjectException.class, () -> new Link(null));
        assertThrows(ValueObjectException.class, () -> Link.of(null));
    }

    @Test
    void isLocalhostMatchesIpv6Loopback() {
        assertTrue(Link.parse("http://localhost:8080/").isLocalhost());
        assertTrue(Link.parse("http://127.0.0.1/").isLocalhost());
        assertTrue(Link.parse("http://[::1]:8080/x").isLocalhost());
        assertTrue(Link.parse("http://[0:0:0:0:0:0:0:1]/").isLocalhost());
        assertFalse(Link.parse("http://[2001:db8::1]/").isLocalhost());
        assertFalse(Link.parse("mailto:a@b.example").isLocalhost());
        assertFalse(Link.parse("https://northwind.example").isLocalhost());
    }

    @Test
    void appendPathGoesBeforeQueryAndFragment() {
        assertEquals("https://a.example/x/y?q=1", Link.parse("https://a.example/x?q=1").appendPath("y").value());
        assertEquals("https://a.example/x/y#f", Link.parse("https://a.example/x#f").appendPath("/y").value());
        assertEquals("https://a.example/x/y?q=1#f", Link.parse("https://a.example/x/?q=1#f").appendPath("/y").value());
        assertEquals("https://a.example/x/y", Link.parse("https://a.example/x").appendPath("y").value());
        assertThrows(ValueObjectException.class, () -> Link.parse("https://a.example").appendPath(null));
    }

    @Test
    void linksOrderByText() {
        assertTrue(Link.parse("https://a.example").compareTo(Link.parse("https://b.example")) < 0);
        assertEquals("https://a.example", Link.parse("https://a.example").toString());
    }

    // ---------------- Email

    @Test
    void emailIsTrimmedAndLowerCased() {
        Email e = Email.parse("  Andrea.Lim@Example.COM ");
        assertEquals("andrea.lim@example.com", e.value());
        assertEquals("andrea.lim", e.localPart());
        assertEquals("example.com", e.domain());
        assertEquals("a***m@example.com", e.masked());
        assertEquals("j***@x.example", Email.of("jo@x.example").masked());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "plain", "a@b", "a@@b.com", "a b@c.com", "@example.com", "a@example.c"})
    void emailRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> Email.parse(text));
        assertTrue(Email.tryParse(text).isEmpty());
        assertFalse(Email.isValid(text));
    }

    @Test
    void emailConstructorNullIsValueObjectException() {
        assertThrows(ValueObjectException.class, () -> new Email(null));
        assertThrows(ValueObjectException.class, () -> Email.of(null));
    }

    @Test
    void personalDomainMatchesExactlyOrAsSubdomain() {
        assertTrue(Email.parse("a@gmail.com").isPersonalDomain());
        assertTrue(Email.parse("a@mail.yahoo.com").isPersonalDomain());
        assertTrue(Email.parse("a@HOTMAIL.com").isPersonalDomain());
        assertFalse(Email.parse("a@notgmail.com").isPersonalDomain(), "notgmail.com is not gmail.com");
        assertFalse(Email.parse("a@myaol.com").isPersonalDomain());
        assertTrue(Email.parse("a@notgmail.com").isBusinessDomain());
        assertTrue(Email.parse("a@northwind.example").isBusinessDomain());
    }

    @Test
    void emailsOrderByText() {
        assertTrue(Email.parse("a@x.com").compareTo(Email.parse("b@x.com")) < 0);
        assertEquals("a@x.com", Email.parse("A@X.com").toString());
    }
}
