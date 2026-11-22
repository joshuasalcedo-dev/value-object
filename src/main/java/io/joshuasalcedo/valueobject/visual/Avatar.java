package io.joshuasalcedo.valueobject.visual;

import java.util.Locale;
import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
/**
 * A profile picture, held as the URL of the image: an absolute URL, trimmed, with a protocol Java knows
 * ({@code http}, {@code https}, {@code file}, {@code jar} and so on). Every way of making one checks this.
 *
 * @param value the image URL, trimmed
 */
public record Avatar(
        String value
) implements ValueObject, Comparable<Avatar> {
    static final String DEFAULT_AVATAR_URL =
            "https://upload.wikimedia.org/wikipedia/commons/2/2c/Default_pfp.svg";


    /**
     * Creates an avatar from the text of an absolute URL, trimmed.
     *
     * @throws ValueObjectException if {@code value} is {@code null} or not an absolute URL with a known protocol
     */
    public Avatar {
        if (value == null) {
            throw new ValueObjectException("An avatar URL is required");
        }
        value = value.trim();
        if (!isUrl(value)) {
            throw new ValueObjectException("Invalid avatar url: " + value);
        }
	}

    /**
     * Creates an avatar from the text of an absolute URL; the same as the constructor.
     *
     * @param value the image URL, e.g. {@code "https://example.com/me.png"}
     * @return the avatar
     * @throws ValueObjectException if it is {@code null} or not an absolute URL with a known protocol
     */
    public static Avatar create(String value){
        return new Avatar(value);
    }


    /**
     * Creates an avatar from a URL.
     *
     * @param url the image URL
     * @return the avatar
     * @throws ValueObjectException if {@code url} is {@code null} or not absolute
     */
    public static Avatar create(URL url){
        if (url == null) {
            throw new ValueObjectException("An avatar URL is required");
        }
        return new Avatar(url.toString());
    }

    /**
     * Creates an avatar from the text of an absolute URL; the same as {@link #create(String)}.
     *
     * @param value the image URL
     * @return the avatar
     * @throws ValueObjectException if it is {@code null} or not an absolute URL with a known protocol
     */
    public static Avatar of(String value){
        return create(value);
    }

    /**
     * Parses an avatar from the text of an absolute URL, trimmed; the same as the constructor.
     *
     * @param text the image URL
     * @return the avatar
     * @throws ValueObjectException if it is {@code null} or not an absolute URL with a known protocol
     */
    public static Avatar parse(String text) {
        return new Avatar(text);
    }

    /**
     * Parses an avatar as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as an {@link Avatar}, or empty when it is not one; never throws
     */
    public static Optional<Avatar> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text, once trimmed, is an absolute URL with a known protocol, as an avatar needs.
     *
     * @param text any text
     * @return whether it is an {@link Avatar}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

    private static boolean isUrl(String value){
        try {
            URI.create(value).toURL();
            return true;
        }catch (Exception _){
            return false;
        }
    }

    /**
     * Returns the image URL as a {@link URL}.
     *
     * @return the URL
     * @throws ValueObjectException never in practice, since the constructor checks the URL
     */
    public URL toURL(){
        try {
            return URI.create(value).toURL();
        } catch (MalformedURLException | IllegalArgumentException e){
            throw new ValueObjectException("Invalid avatar url: " + value, e);
        }
    }

    /** {@return the default avatar: a generic profile picture on Wikimedia Commons} */
    public static Avatar createDefault(){
        return new Avatar(DEFAULT_AVATAR_URL);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "'avatar' : '%s'", value);
    }

    /**
     * Compares avatars by their URL text.
     *
     * @param other the avatar to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    @Override
    public int compareTo(Avatar other) {
        return this.value.compareTo(other.value);
    }
}
