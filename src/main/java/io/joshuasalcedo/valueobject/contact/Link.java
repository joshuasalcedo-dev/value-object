package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * Value object representing a URL.
 * Validates URL structure and provides convenient accessors for components.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Link(String value) implements ValueObject, Comparable<Link> {

    /** Null object representing an absent Link. */
    public static final Link NULL = new Link("");

    public Link {
        Objects.requireNonNull(value, "URL cannot be null");
        if (!value.isEmpty()) {
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

    
    public boolean isNull() {
        return value.isEmpty();
    }

    /**
     * Creates a Link from a string value.
     *
     * @param url the URL string
     * @return a new Link instance
     */
    public static Link of(String url) {
        return new Link(url);
    }

    /**
     * Creates a Link, adding https:// if no scheme is present.
     *
     * @param url the URL string
     * @return a new Link instance
     */
    public static Link https(String url) {
        if (!url.contains("://")) {
            url = "https://" + url;
        }
        return new Link(url);
    }

    /**
     * Parses a URL string, returning null for invalid URLs instead of throwing.
     *
     * @param url the URL string
     * @return the Link or null if invalid
     */
    public static @Nullable Link parse(String url) {
        try {
            return new Link(url);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Returns the null object representation of Link.
     *
     * @return the NULL link constant
     */
    public static Link nullValue() {
        return NULL;
    }

    /**
     * Checks if a string is a valid URL format.
     *
     * @param url the URL string to check
     * @return true if valid format
     */
    public static boolean isValid(String url) {
        if (url.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(url.trim());
            return uri.getScheme() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Returns the URI representation.
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
     * Returns the scheme (protocol) of the URL (e.g., "https").
     *
     * @return the scheme
     */
    public String scheme() {
        return toUri().getScheme();
    }

    /**
     * Returns the host of the URL.
     *
     * @return the host
     */
    public String host() {
        return toUri().getHost();
    }

    /**
     * Returns the port if specified, or empty if using default.
     *
     * @return the port or empty
     */
    public Optional<Integer> port() {
        int port = toUri().getPort();
        return port == -1 ? Optional.empty() : Optional.of(port);
    }

    /**
     * Returns the path of the URL.
     *
     * @return the path or empty string
     */
    public String path() {
        String path = toUri().getPath();
        return path == null ? "" : path;
    }

    /**
     * Returns the query string if present.
     *
     * @return the query or empty
     */
    public Optional<String> query() {
        return Optional.ofNullable(toUri().getQuery());
    }

    /**
     * Returns the fragment (anchor) if present.
     *
     * @return the fragment or empty
     */
    public Optional<String> fragment() {
        return Optional.ofNullable(toUri().getFragment());
    }

    /**
     * Checks if this URL uses HTTPS.
     *
     * @return true if HTTPS
     */
    public boolean isSecure() {
        return "https".equalsIgnoreCase(scheme());
    }

    /**
     * Checks if this is a localhost URL.
     *
     * @return true if localhost
     */
    public boolean isLocalhost() {
        String h = host();
        return "localhost".equalsIgnoreCase(h) || "127.0.0.1".equals(h) || "::1".equals(h);
    }

    /**
     * Returns the base URL (scheme + host + port if non-default).
     *
     * @return the base URL
     */
    public String baseUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(scheme()).append("://").append(host());
        port().ifPresent(p -> sb.append(":").append(p));
        return sb.toString();
    }

    /**
     * Creates a new URL with an additional path segment.
     *
     * @param segment the path segment to append
     * @return a new Link with the appended path
     */
    public Link appendPath(String segment) {
        String base = value;
        if (!base.endsWith("/") && !segment.startsWith("/")) {
            base += "/";
        } else if (base.endsWith("/") && segment.startsWith("/")) {
            segment = segment.substring(1);
        }
        return new Link(base + segment);
    }

    @Override
    public int compareTo(Link other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
