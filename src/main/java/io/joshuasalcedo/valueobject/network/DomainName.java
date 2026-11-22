package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.net.IDN;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A domain name (RFC 1035 / 5890): dot-separated labels of letters, digits and hyphens, ending in a top-level
 * domain of letters. Lower-cased; international names are kept in their ASCII (punycode) form.
 *
 * <pre>{@code
 * DomainName d = DomainName.parse("Billing.Northwind.example");
 * d.tld();                 // "example"
 * d.registrable();         // "northwind.example"
 * d.isSubdomainOf(DomainName.parse("northwind.example"));   // true
 * }</pre>
 *
 * @param value the name, lower case, no trailing dot
 * @author JoshuaSalcedo
 */
public record DomainName(String value) implements ValueObject, Comparable<DomainName> {

    /** Second-level labels under which names are registered ("example.co.uk", "example.com.ph"). */
    private static final Set<String> SECOND_LEVEL = Set.of("co", "com", "net", "org", "gov", "edu", "ac", "or", "ne", "go");

    /**
     * @throws ValueObjectException if it is not a domain name
     */
    public DomainName {
        if (value == null || value.isBlank()) throw new ValueObjectException("A domain name is required");
        String v = value.strip().toLowerCase(Locale.ROOT).replaceAll("\\.$", "");
        try {
            v = IDN.toASCII(v, IDN.ALLOW_UNASSIGNED);
        } catch (IllegalArgumentException e) {
            throw new ValueObjectException("Not a domain name: " + value);
        }
        if (v.length() > 253) throw new ValueObjectException("A domain name is at most 253 characters");
        String[] labels = v.split("\\.", -1);
        if (labels.length < 2) throw new ValueObjectException("A domain name has at least two labels: " + value);
        for (String l : labels) {
            if (!l.matches("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?"))
                throw new ValueObjectException("Not a domain name label: '" + l + "' in " + value);
        }
        if (!labels[labels.length - 1].matches("[a-z]{2,63}|xn--[a-z0-9-]+"))
            throw new ValueObjectException("Not a top-level domain: " + labels[labels.length - 1]);
        value = v;
    }

    /**
     * @param text e.g. {@code "northwind.example"}; a URL's or email's host is taken from it
     * @return the domain name
     * @throws ValueObjectException if there is none
     */
    public static DomainName parse(String text) {
        if (text == null) throw new ValueObjectException("A domain name is required");
        String t = text.strip();
        if (t.contains("@")) t = t.substring(t.lastIndexOf('@') + 1);
        t = t.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "").replaceFirst("[/:?#].*$", "");
        return new DomainName(t);
    }

    /**
     * @param text any text
     * @return whether it is a domain name
     */
    public static boolean isValid(@Nullable String text) {
        if (text == null) return false;
        try {
            new DomainName(text);
            return true;
        } catch (ValueObjectException e) {
            return false;
        }
    }

    /** @return its labels, left to right */
    public List<String> labels() {
        return List.of(value.split("\\."));
    }

    /** @return its top-level domain, e.g. {@code "com"} */
    public String tld() {
        return labels().getLast();
    }

    /**
     * @return the part a person or company registers: the last two labels, or three under a second-level label
     *         ({@code "shop.example.com.ph"} -> {@code "example.com.ph"})
     */
    public DomainName registrable() {
        List<String> l = labels();
        int n = l.size() >= 3 && SECOND_LEVEL.contains(l.get(l.size() - 2)) && l.getLast().length() == 2 ? 3 : 2;
        return new DomainName(String.join(".", l.subList(Math.max(0, l.size() - n), l.size())));
    }

    /**
     * @param parent another domain
     * @return whether this is it or under it
     */
    public boolean isSubdomainOf(DomainName parent) {
        return value.equals(parent.value) || value.endsWith("." + parent.value);
    }

    /** @return it in Unicode, as people read it */
    public String unicode() {
        return IDN.toUnicode(value);
    }

    @Override
    public int compareTo(DomainName other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
