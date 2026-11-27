package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

/**
 * Which half of the day a 12-hour clock time is in: {@code AM} for hours 0-11, {@code PM} for hours 12-23
 * (24-hour clock).
 *
 * @author JoshuaSalcedo
 */
public enum Meridiem implements ValueObject {
    /** Ante Meridiem ({@code AM}). */
    AM("AM", "Ante Meridiem", 0, 11),
    /** Post Meridiem ({@code PM}). */
    PM("PM", "Post Meridiem", 12, 23);

    private final String abbreviation;
    private final String fullName;
    private final int startHour;
    private final int endHour;

    Meridiem(String abbreviation, String fullName, int startHour, int endHour) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    /**
     * Finds the half of the day an hour of the 24-hour clock is in.
     *
     * @param hour24 the hour, 0-23
     * @return {@code AM} for 0-11, {@code PM} for 12-23
     * @throws ValueObjectException if {@code hour24} is outside 0-23
     */
    public static Meridiem fromHour(int hour24) {
        if (hour24 < 0 || hour24 > 23) {
            throw new ValueObjectException("Hour must be between 0 and 23, got: " + hour24);
        }
        return hour24 < 12 ? AM : PM;
    }

    /**
     * Finds the half of the day a time is in.
     *
     * @param time the time of day
     * @return {@code AM} before noon, {@code PM} from noon on
     * @throws ValueObjectException if {@code time} is null
     */
    public static Meridiem fromLocalTime(java.time.LocalTime time) {
        if (time == null) throw new ValueObjectException("A time is required");
        return fromHour(time.getHour());
    }

    /**
     * Finds the half of the day it is now, on this machine's clock and zone.
     *
     * @return {@code AM} or {@code PM}
     */
    public static Meridiem now() {
        return fromLocalTime(java.time.LocalTime.now());
    }

    /**
     * The short form.
     *
     * @return {@code "AM"} or {@code "PM"}
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * The Latin name.
     *
     * @return {@code "Ante Meridiem"} or {@code "Post Meridiem"}
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * The first hour of this half, on the 24-hour clock.
     *
     * @return 0 for {@code AM}, 12 for {@code PM}
     */
    public int getStartHour() {
        return startHour;
    }

    /**
     * The last hour of this half, on the 24-hour clock.
     *
     * @return 11 for {@code AM}, 23 for {@code PM}
     */
    public int getEndHour() {
        return endHour;
    }

    /**
     * Tells whether this is {@code AM}.
     *
     * @return whether this is {@code AM}
     */
    public boolean isAm() {
        return this == AM;
    }

    /**
     * Tells whether this is {@code PM}.
     *
     * @return whether this is {@code PM}
     */
    public boolean isPm() {
        return this == PM;
    }

    /**
     * Writes the short form.
     *
     * @return {@code "AM"} or {@code "PM"}
     */
    @Override
    public String toString() {
        return abbreviation;
    }

    /**
     * Reads {@code AM} or {@code PM}, ignoring case, dots and blanks: {@code "am"}, {@code "P.M."} and
     * {@code "p. m."} are all read.
     *
     * @param text {@code "AM"}, {@code "p.m."}, ...
     * @return it
     * @throws ValueObjectException if it is not one
     */
    public static Meridiem parse(String text) {
        if (text == null) throw new ValueObjectException("A Meridiem is required");
        String t = text.strip();
        String key = t.replaceAll("[.\\s]", "");
        for (Meridiem v : values()) {
            if (v.name().equalsIgnoreCase(key)) return v;
        }
        throw new ValueObjectException("Not a Meridiem: '" + text + "'");
    }

    /**
     * Reads {@code AM} or {@code PM} like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link Meridiem}, or empty when it is not one; never throws
     */
    public static Optional<Meridiem> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is {@code AM} or {@code PM} as {@link #parse(String)} reads it.
     *
     * @param text any text, or null
     * @return whether it is a {@link Meridiem} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
