package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import io.joshuasalcedo.valueobject.network.Ipv6Address;
import java.util.Optional;

/**
 * A URL: trimmed text that parses as a {@link URI} with a scheme ({@code https:}, {@code mailto:}, ...), with
 * accessors for its parts.
 *
 * <pre>{@code
 * Link l = Link.https("northwind.example/billing?tab=2");
 * l.host();      // "northwind.example"
 * l.query();     // Optional["tab=2"]
 * l.baseUrl();   // "https://northwind.example"
 * }</pre>
 *
 * @param value the URL, trimmed
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Link(String value) implements ValueObject, Comparable<Link> {


    /**
     * Creates a link, trimming the text.
     *
     * @throws ValueObjectException if {@code value} is {@code null}, blank, not a valid URI, or has no scheme
     */
    public Link {
        if (value == null) {
            throw new ValueObjectException("URL cannot be null");
        }
        {
            value = value.trim();
            if (value.isBlank()) {
                throw new ValueObjectException("URL cannot be blank");
            }
            // Validate by trying to parse as URI
            try {
                URI uri = new URI(value);
                if (uri.getScheme() == null) {
                    throw new ValueObjectException("URL must have a scheme: " + value);
                }
            } catch (URISyntaxException e) {
                throw new ValueObjectException("Invalid URL format: " + value, e);
            }
        }
    }

    

    /**
     * Creates a link; the same as the constructor.
     *
     * @param url the URL, with a scheme
     * @return the link
     * @throws ValueObjectException if it is {@code null}, blank, not a valid URI, or has no scheme
     */
    public static Link of(String url) {
        return new Link(url);
    }

    /**
     * Creates a link, putting {@code https://} in front when the text has no {@code ://}.
     *
     * @param url the URL, e.g. {@code "northwind.example/billing"}
     * @return the link
     * @throws ValueObjectException if it is {@code null}, or not a valid URL even then
     */
    public static Link https(String url) {
        if (url == null) {
            throw new ValueObjectException("A Link is required");
        }
        url = url.trim();
        if (!url.contains("://")) {
            url = "https://" + url;
        }
        return new Link(url);
    }


    /**
     * Parses a link; the text must have a scheme.
     *
     * @param text the text
     * @return the link
     * @throws ValueObjectException if it is {@code null}, blank, not a valid URI, or has no scheme
     */
    public static Link parse(String text) {
        if (text == null) throw new ValueObjectException("A Link is required");
        return of(text);
    }


    /**
     * Checks whether a text, once trimmed, parses as a URI with a scheme.
     *
     * @param url the text to check; may be {@code null}
     * @return whether it is a valid link
     */
    public static boolean isValid(@Nullable String url) {
        return tryParse(url).isPresent();
    }

    /**
     * Returns the link as a {@link URI}.
     *
     * @return the URI
     */
    public URI toUri() {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new ValueObjectException("Invalid URI", e);
        }
    }

    /**
     * Returns the scheme (protocol) of the URL, e.g. {@code "https"}.
     *
     * @return the scheme
     */
    public String scheme() {
        return toUri().getScheme();
    }

    /**
     * Returns the host of the URL.
     *
     * @return the host, or {@code null} when it has none (e.g. a {@code mailto:} link); an IPv6 host keeps
     *         its brackets, {@code "[::1]"}
     */
    public @Nullable String host() {
        return toUri().getHost();
    }

    /**
     * Returns the port when the URL gives one.
     *
     * @return the port, or empty when the URL has none
     */
    public Optional<Integer> port() {
        int port = toUri().getPort();
        return port == -1 ? Optional.empty() : Optional.of(port);
    }

    /**
     * Returns the path of the URL, decoded.
     *
     * @return the path, or an empty string when it has none
     */
    public String path() {
        String path = toUri().getPath();
        return path == null ? "" : path;
    }

    /**
     * Returns the query string, decoded, without the {@code ?}.
     *
     * @return the query, or empty when there is none
     */
    public Optional<String> query() {
        return Optional.ofNullable(toUri().getQuery());
    }

    /**
     * Returns the fragment (anchor), decoded, without the {@code #}.
     *
     * @return the fragment, or empty when there is none
     */
    public Optional<String> fragment() {
        return Optional.ofNullable(toUri().getFragment());
    }

    /**
     * Checks whether the scheme is {@code https}, ignoring case.
     *
     * @return whether it uses HTTPS
     */
    public boolean isSecure() {
        return "https".equalsIgnoreCase(scheme());
    }

    /**
     * Checks whether the host is {@code localhost}, {@code 127.0.0.1} or the IPv6 loopback {@code [::1]}
     * (in any spelling, such as {@code [0:0:0:0:0:0:0:1]}).
     *
     * @return whether it points to the local machine
     */
    public boolean isLocalhost() {
        String h = host();
        if (h == null) return false;
        if (h.startsWith("[") && h.endsWith("]")) {
            return Ipv6Address.tryParse(h.substring(1, h.length() - 1)).map(Ipv6Address::isLoopback).orElse(false);
        }
        return "localhost".equalsIgnoreCase(h) || "127.0.0.1".equals(h);
    }

    /**
     * Returns the base URL: {@code scheme://host}, plus {@code :port} when the URL gives a port. A link with no
     * host ({@code mailto:}, {@code tel:}) gives just {@code scheme:}.
     *
     * @return the base URL
     */
    public String baseUrl() {
        StringBuilder sb = new StringBuilder();
        String host = host();
        if (host == null) return scheme() + ":";
        sb.append(scheme()).append("://").append(host);
        port().ifPresent(p -> sb.append(":").append(p));
        return sb.toString();
    }

    /**
     * Returns a link with text appended to the path, putting exactly one {@code /} between them. Any query or
     * fragment stays after it: {@code https://a.example/x?q=1} with {@code "y"} gives
     * {@code https://a.example/x/y?q=1}.
     *
     * @param segment the path segment to append, e.g. {@code "invoices"}
     * @return the new link
     * @throws ValueObjectException if {@code segment} is {@code null} or the result is not a valid URL
     */
    public Link appendPath(String segment) {
        if (segment == null) {
            throw new ValueObjectException("A path segment is required");
        }
        int q = value.indexOf('?');
        int f = value.indexOf('#');
        int cut = q < 0 ? f : (f < 0 ? q : Math.min(q, f));
        String base = cut < 0 ? value : value.substring(0, cut);
        String tail = cut < 0 ? "" : value.substring(cut);
        if (!base.endsWith("/") && !segment.startsWith("/")) {
            base += "/";
        } else if (base.endsWith("/") && segment.startsWith("/")) {
            segment = segment.substring(1);
        }
        return new Link(base + segment + tail);
    }

    @Override
    public int compareTo(Link other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Parses a link as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link Link}, or empty when it is not one; never throws
     */
    public static Optional<Link> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
