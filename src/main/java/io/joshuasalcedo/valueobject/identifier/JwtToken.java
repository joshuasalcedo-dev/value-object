package io.joshuasalcedo.valueobject.identifier;

import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * A JSON Web Token in compact form: {@code header.payload.signature}, each part Base64URL text, the signature
 * possibly empty. Only the shape is checked; the signature is <b>not</b> verified, so do not trust its claims
 * without verifying it elsewhere.
 *
 * <p>The claim accessors find a claim by a plain text search of the decoded payload, not a JSON parse: they
 * read only top-level string claims and whole-second numeric claims, and give {@code null} for anything else
 * (an {@code aud} array, for example).
 *
 * @param value the token, trimmed
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record JwtToken(String value) implements ValueObject {

    // JWT format: header.payload.signature (each part is Base64URL encoded)
    private static final Pattern JWT_PATTERN = Pattern.compile(
        "^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]*$"
    );

    private static final int HEADER_INDEX = 0;
    private static final int PAYLOAD_INDEX = 1;
    private static final int SIGNATURE_INDEX = 2;

    /**
     * Creates a token, trimming it.
     *
     * @throws ValueObjectException if it is {@code null}, blank, or not three dot-separated Base64URL parts
     */
    public JwtToken {
        if (value == null) {
            throw new ValueObjectException("JWT token cannot be null");
        }
        value = value.trim();

        if (value.isBlank()) {
            throw new ValueObjectException("JWT token cannot be blank");
        }
        if (!JWT_PATTERN.matcher(value).matches()) {
            throw new ValueObjectException("Invalid JWT format. Expected: header.payload.signature");
        }
    }

    /**
     * Creates a token; the same as the constructor.
     *
     * @param token the JWT string
     * @return the token
     * @throws ValueObjectException if it is {@code null}, blank, or not three dot-separated Base64URL parts
     */
    public static JwtToken of(String token) {
        return new JwtToken(token);
    }


    /**
     * Parses a token, trimming it.
     *
     * @param text the text
     * @return the token
     * @throws ValueObjectException if it is {@code null}, blank or not three dot-separated Base64URL parts
     */
    public static JwtToken parse(String text) {
        if (text == null) throw new ValueObjectException("A JwtToken is required");
        return of(text);
    }

    /**
     * Checks whether a text, once trimmed, has the shape of a JWT; the signature is not checked.
     *
     * @param token the token to check; may be {@code null}
     * @return whether it is shaped like a JWT
     */
    public static boolean isValid(@Nullable String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return JWT_PATTERN.matcher(token.trim()).matches();
    }

    /**
     * Returns the header part of the JWT (Base64URL encoded).
     *
     * @return the encoded header
     */
    public String header() {
        return getPart(HEADER_INDEX);
    }

    /**
     * Returns the payload part of the JWT (Base64URL encoded).
     *
     * @return the encoded payload
     */
    public String payload() {
        return getPart(PAYLOAD_INDEX);
    }

    /**
     * Returns the signature part of the JWT (Base64URL encoded).
     *
     * @return the encoded signature
     */
    public String signature() {
        return getPart(SIGNATURE_INDEX);
    }

    /**
     * Decodes the header from Base64URL, as UTF-8.
     *
     * @return the decoded header JSON, or {@code "{}"} when it is not valid Base64URL
     */
    public String decodedHeader() {
        return decodeBase64Url(header());
    }

    /**
     * Decodes the payload from Base64URL, as UTF-8.
     *
     * @return the decoded payload JSON, or {@code "{}"} when it is not valid Base64URL
     */
    public String decodedPayload() {
        return decodeBase64Url(payload());
    }

    /**
     * Reads the {@code sub} (subject) claim from the payload.
     *
     * @return the subject, or {@code null} when it is missing or not a string
     */
    public @Nullable String subject() {
        return extractClaim("\"sub\"");
    }

    /**
     * Reads the {@code iss} (issuer) claim from the payload.
     *
     * @return the issuer, or {@code null} when it is missing or not a string
     */
    public @Nullable String issuer() {
        return extractClaim("\"iss\"");
    }

    /**
     * Reads the {@code aud} (audience) claim from the payload, when it is a single string.
     *
     * @return the audience, or {@code null} when it is missing or not a string (an array of audiences gives
     *         {@code null})
     */
    public @Nullable String audience() {
        return extractClaim("\"aud\"");
    }

    /**
     * Reads the {@code exp} (expiration) claim, in seconds since the epoch.
     *
     * @return the expiration time, or {@code null} when it is missing or not a number
     */
    public @Nullable Instant expirationTime() {
        String exp = extractNumericClaim("\"exp\"");
        if (exp != null) {
            try {
                return Instant.ofEpochSecond(Long.parseLong(exp));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Reads the {@code iat} (issued at) claim, in seconds since the epoch.
     *
     * @return the issue time, or {@code null} when it is missing or not a number
     */
    public @Nullable Instant issuedAt() {
        String iat = extractNumericClaim("\"iat\"");
        if (iat != null) {
            try {
                return Instant.ofEpochSecond(Long.parseLong(iat));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Reads the {@code nbf} (not before) claim, in seconds since the epoch.
     *
     * @return the time it becomes valid, or {@code null} when it is missing or not a number
     */
    public @Nullable Instant notBefore() {
        String nbf = extractNumericClaim("\"nbf\"");
        if (nbf != null) {
            try {
                return Instant.ofEpochSecond(Long.parseLong(nbf));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Reads the {@code jti} (JWT ID) claim from the payload.
     *
     * @return the JWT ID, or {@code null} when it is missing or not a string
     */
    public @Nullable String jwtId() {
        return extractClaim("\"jti\"");
    }

    /**
     * Checks whether the {@code exp} claim is in the past. A token without a readable {@code exp} counts as
     * expired. The signature is not verified.
     *
     * @return whether it is expired, or has no expiration time
     */
    public boolean isExpired() {
        Instant exp = expirationTime();
        if (exp == null) {
            return true; // Consider tokens without exp as potentially expired
        }
        return Instant.now().isAfter(exp);
    }

    /**
     * Checks whether the {@code nbf} claim is in the future. A token without a readable {@code nbf} is valid
     * already.
     *
     * @return whether it is not yet valid
     */
    public boolean isNotYetValid() {
        Instant nbf = notBefore();
        if (nbf == null) {
            return false;
        }
        return Instant.now().isBefore(nbf);
    }

    /**
     * Reads the {@code alg} (signing algorithm) from the header.
     *
     * @return the algorithm, e.g. {@code "HS256"} or {@code "RS256"}, or {@code null} when it is missing
     */
    public @Nullable String algorithm() {
        String header = decodedHeader();
        int algIndex = header.indexOf("\"alg\"");
        if (algIndex < 0) return null;

        int colonIndex = header.indexOf(':', algIndex);
        if (colonIndex < 0) return null;

        int startQuote = header.indexOf('"', colonIndex);
        if (startQuote < 0) return null;

        int endQuote = header.indexOf('"', startQuote + 1);
        if (endQuote < 0) return null;

        return header.substring(startQuote + 1, endQuote);
    }

    /**
     * Returns the token shortened for logs: its first 20 and last 10 characters around {@code "..."}; a token of
     * 40 characters or fewer shows only its first 10.
     *
     * @return the masked token
     */
    public String masked() {
        if (value.length() <= 40) {
            return value.substring(0, Math.min(10, value.length())) + "...";
        }
        return value.substring(0, 20) + "..." + value.substring(value.length() - 10);
    }

    /**
     * Returns the length of the token text.
     *
     * @return the number of characters
     */
    public int length() {
        return value.length();
    }

    /**
     * Checks whether the signature part is non-empty; an unsigned ({@code alg: none}) token has an empty one.
     *
     * @return whether a signature is present
     */
    public boolean hasSignature() {
        return !signature().isEmpty();
    }

    @Override
    public String toString() {
        return "JwtToken[" + masked() + "]";
    }

    // ==================== Private Helper Methods ====================

    private String getPart(int index) {
        String[] parts = value.split("\\.");
        return parts.length > index ? parts[index] : "";
    }

    private String decodeBase64Url(String encoded) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encoded);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "{}";
        }
    }

    private @Nullable String extractClaim(String claimKey) {
        String payload = decodedPayload();
        int keyIndex = payload.indexOf(claimKey);
        if (keyIndex < 0) return null;

        int colonIndex = payload.indexOf(':', keyIndex);
        if (colonIndex < 0) return null;

        // Skip whitespace
        int valueStart = colonIndex + 1;
        while (valueStart < payload.length() && Character.isWhitespace(payload.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= payload.length()) return null;

        // Check if value is a string (starts with quote)
        if (payload.charAt(valueStart) == '"') {
            int endQuote = payload.indexOf('"', valueStart + 1);
            if (endQuote < 0) return null;
            return payload.substring(valueStart + 1, endQuote);
        }

        return null;
    }

    private @Nullable String extractNumericClaim(String claimKey) {
        String payload = decodedPayload();
        int keyIndex = payload.indexOf(claimKey);
        if (keyIndex < 0) return null;

        int colonIndex = payload.indexOf(':', keyIndex);
        if (colonIndex < 0) return null;

        // Skip whitespace
        int valueStart = colonIndex + 1;
        while (valueStart < payload.length() && Character.isWhitespace(payload.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= payload.length()) return null;

        // Extract numeric value
        StringBuilder numBuilder = new StringBuilder();
        for (int i = valueStart; i < payload.length(); i++) {
            char c = payload.charAt(i);
            if (Character.isDigit(c) || c == '-') {
                numBuilder.append(c);
            } else {
                break;
            }
        }

        return numBuilder.isEmpty() ? null : numBuilder.toString();
    }

    /**
     * Parses a token as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link JwtToken}, or empty when it is not one; never throws
     */
    public static Optional<JwtToken> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
