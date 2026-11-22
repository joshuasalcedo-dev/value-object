package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a JSON Web Token (JWT).
 * Stores the raw token string and provides methods to extract its parts.
 *
 * This class performs basic structural validation but does NOT verify the signature.
 * For signature verification, use JwtTokenPort with an appropriate implementation.
 *
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

    public JwtToken {
        Objects.requireNonNull(value, "JWT token cannot be null");
        value = value.trim();

        if (value.isBlank()) {
            throw new ValueObjectException("JWT token cannot be blank");
        }
        if (!JWT_PATTERN.matcher(value).matches()) {
            throw new ValueObjectException("Invalid JWT format. Expected: header.payload.signature");
        }
    }

    /**
     * Creates a JwtToken from a token string.
     *
     * @param token the JWT string
     * @return a new JwtToken
     */
    public static JwtToken of(String token) {
        return new JwtToken(token);
    }

    /**
     * Parses a JWT string, returning null if invalid.
     *
     * @param token the JWT string
     * @return the JwtToken or null if invalid
     */
    public static @Nullable JwtToken parse(String token) {
        try {
            return new JwtToken(token);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Checks if a string is a valid JWT format.
     *
     * @param token the token to check
     * @return true if valid JWT format
     */
    public static boolean isValid(String token) {
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
     * Decodes and returns the header as a JSON string.
     *
     * @return the decoded header JSON
     */
    public String decodedHeader() {
        return decodeBase64Url(header());
    }

    /**
     * Decodes and returns the payload as a JSON string.
     *
     * @return the decoded payload JSON
     */
    public String decodedPayload() {
        return decodeBase64Url(payload());
    }

    /**
     * Extracts the 'sub' (subject) claim from the payload if present.
     * Returns null if not found or if parsing fails.
     *
     * @return the subject or null
     */
    public @Nullable String subject() {
        return extractClaim("\"sub\"");
    }

    /**
     * Extracts the 'iss' (issuer) claim from the payload if present.
     *
     * @return the issuer or null
     */
    public @Nullable String issuer() {
        return extractClaim("\"iss\"");
    }

    /**
     * Extracts the 'aud' (audience) claim from the payload if present.
     *
     * @return the audience or null
     */
    public @Nullable String audience() {
        return extractClaim("\"aud\"");
    }

    /**
     * Extracts the 'exp' (expiration) claim and returns it as an Instant.
     *
     * @return the expiration time or null if not present
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
     * Extracts the 'iat' (issued at) claim and returns it as an Instant.
     *
     * @return the issued at time or null if not present
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
     * Extracts the 'nbf' (not before) claim and returns it as an Instant.
     *
     * @return the not before time or null if not present
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
     * Extracts the 'jti' (JWT ID) claim from the payload if present.
     *
     * @return the JWT ID or null
     */
    public @Nullable String jwtId() {
        return extractClaim("\"jti\"");
    }

    /**
     * Checks if this token appears to be expired based on the 'exp' claim.
     * Note: This does NOT verify the signature.
     *
     * @return true if expired or if exp claim is not present
     */
    public boolean isExpired() {
        Instant exp = expirationTime();
        if (exp == null) {
            return true; // Consider tokens without exp as potentially expired
        }
        return Instant.now().isAfter(exp);
    }

    /**
     * Checks if this token is not yet valid based on the 'nbf' claim.
     *
     * @return true if not yet valid
     */
    public boolean isNotYetValid() {
        Instant nbf = notBefore();
        if (nbf == null) {
            return false;
        }
        return Instant.now().isBefore(nbf);
    }

    /**
     * Returns the algorithm used from the header.
     *
     * @return the algorithm (e.g., "HS256", "RS256") or null
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
     * Returns a masked version of the token for logging/display.
     * Shows first 20 chars and last 10 chars.
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
     * Returns the token length.
     *
     * @return the length
     */
    public int length() {
        return value.length();
    }

    /**
     * Checks if the signature is present (non-empty).
     *
     * @return true if signature is present
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
}
