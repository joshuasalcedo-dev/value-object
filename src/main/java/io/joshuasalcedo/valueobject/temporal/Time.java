package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;

import java.time.LocalTime;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable value object representing a time in 12-hour format, to the minute.
 * Supports conversion to/from 24-hour format and various string representations.
 * Midnight is {@code 12:00 AM} and noon {@code 12:00 PM}. Times order from midnight to {@code 11:59 PM}.
 * Bad values (an hour or minute out of range, a missing meridiem, an unreadable text) throw
 * {@link ValueObjectException}.
 *
 * @param hour     the hour on the 12-hour clock, 1-12
 * @param minute   the minute, 0-59
 * @param meridiem {@code AM} or {@code PM}
 * @author JoshuaSalcedo
 */
public record Time(int hour, int minute, Meridiem meridiem) implements ValueObject, Comparable<Time> {

    // ==================== COMPACT CONSTRUCTOR ====================

    /**
     * Checks the hour, minute and meridiem.
     *
     * @throws ValueObjectException if {@code hour} is outside 1-12, {@code minute} outside 0-59, or
     *                              {@code meridiem} is null
     */
    public Time {
        validateHour12(hour);
        validateMinute(minute);
        if (meridiem == null) throw new ValueObjectException("Meridiem cannot be null");
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Creates a Time from 12-hour format components.
     *
     * @param hour     the hour (1-12)
     * @param minute   the minute (0-59)
     * @param meridiem AM or PM
     * @return a new Time instance
     * @throws ValueObjectException if hour or minute is out of range
     */
    public static Time of(int hour, int minute, Meridiem meridiem) {
        return new Time(hour, minute, meridiem);
    }

    /**
     * Creates a Time from 24-hour format.
     *
     * @param hour24 the hour in 24-hour format (0-23)
     * @param minute the minute (0-59)
     * @return a new Time instance
     * @throws ValueObjectException if hour or minute is out of range
     */
    public static Time of24(int hour24, int minute) {
        validateHour24(hour24);
        validateMinute(minute);

        Meridiem meridiem = Meridiem.fromHour(hour24);
        int hour12 = convertTo12Hour(hour24);

        return new Time(hour12, minute, meridiem);
    }

    /**
     * Creates a Time from a LocalTime; seconds and smaller are dropped.
     *
     * @param localTime the LocalTime to convert
     * @return a new Time instance
     * @throws ValueObjectException if {@code localTime} is null
     */
    public static Time from(LocalTime localTime) {
        if (localTime == null) throw new ValueObjectException("LocalTime cannot be null");
        return of24(localTime.getHour(), localTime.getMinute());
    }

    /**
     * Creates a Time representing the current time, on this machine's clock and zone, to the minute.
     *
     * @return a new Time instance for now
     */
    public static Time now() {
        return from(LocalTime.now());
    }

    /**
     * Parses a time string in various formats, ignoring case and blanks around it.
     * Supported formats: {@code "11:30 AM"}, {@code "11:30AM"}, {@code "11:30 a.m."}, {@code "23:30"},
     * {@code "23:30:00"}, {@code "9:05:30 PM"}. A text with a meridiem ({@code AM}, {@code PM}, {@code a.m.},
     * {@code p.m.}) is read on the 12-hour clock (hour 1-12), any other on the 24-hour clock (hour 0-23).
     * Seconds, when given, must be 0-59 and are dropped, since a {@code Time} is to the minute.
     *
     * @param timeString the string to parse
     * @return a new Time instance
     * @throws ValueObjectException if {@code timeString} is null or cannot be parsed
     */
    public static Time parse(String timeString) {
        if (timeString == null) throw new ValueObjectException("Time string cannot be null");
        Matcher m = PATTERN.matcher(timeString.strip());
        if (!m.matches()) {
            throw new ValueObjectException("Invalid time format: '" + timeString + "'");
        }
        int hour = Integer.parseInt(m.group(1));
        int minute = Integer.parseInt(m.group(2));
        if (m.group(3) != null) {
            int second = Integer.parseInt(m.group(3));
            if (second > 59) throw new ValueObjectException("Second must be between 0 and 59, got: " + second);
        }
        String mer = m.group(4);
        if (mer == null) {
            return of24(hour, minute);
        }
        return of(hour, minute, Character.toUpperCase(mer.charAt(0)) == 'P' ? Meridiem.PM : Meridiem.AM);
    }

    private static final Pattern PATTERN = Pattern.compile(
            "(\\d{1,2})\\s*:\\s*(\\d{2})(?:\\s*:\\s*(\\d{2}))?\\s*([AaPp]\\.?\\s*[Mm]\\.?)?");

    // ==================== DERIVED GETTERS ====================

    /**
     * The hour on the 24-hour clock: {@code 12 AM} is 0, {@code 12 PM} is 12.
     *
     * @return the hour in 24-hour format (0-23)
     */
    public int hour24() {
        return convertTo24Hour(hour, meridiem);
    }

    // ==================== CONVERSION METHODS ====================

    /**
     * Converts to a LocalTime.
     *
     * @return the equivalent LocalTime, with zero seconds
     */
    public LocalTime toLocalTime() {
        return LocalTime.of(hour24(), minute);
    }

    /**
     * Returns the time in 24-hour format string (HH:mm).
     *
     * @return formatted string like "23:30" or "09:05"
     */
    public String to24HourFormat() {
        return String.format(Locale.ROOT, "%02d:%02d", hour24(), minute);
    }

    /**
     * Returns the time in 12-hour format string (h:mm AM/PM).
     *
     * @return formatted string like "11:30 PM" or "9:05 AM"
     */
    public String to12HourFormat() {
        return String.format(Locale.ROOT, "%d:%02d %s", hour, minute, meridiem.getAbbreviation());
    }

    /**
     * Returns the time in 12-hour format with padded hour (hh:mm AM/PM).
     *
     * @return formatted string like "11:30 PM" or "09:05 AM"
     */
    public String to12HourFormatPadded() {
        return String.format(Locale.ROOT, "%02d:%02d %s", hour, minute, meridiem.getAbbreviation());
    }

    /**
     * Returns the time in ISO format (HH:mm:ss).
     *
     * @return formatted string like "23:30:00"
     */
    public String toIsoFormat() {
        return String.format(Locale.ROOT, "%02d:%02d:00", hour24(), minute);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Tells whether this time is {@code AM}, from midnight to {@code 11:59 AM}.
     *
     * @return true if this time is in the morning (AM)
     */
    public boolean isMorning() {
        return meridiem.isAm();
    }

    /**
     * Tells whether this time is {@code PM}, from noon to {@code 11:59 PM}.
     *
     * @return true if this time is in the afternoon/evening (PM)
     */
    public boolean isAfternoon() {
        return meridiem.isPm();
    }

    /**
     * Tells whether this time is exactly midnight.
     *
     * @return true if this time is midnight (12:00 AM)
     */
    public boolean isMidnight() {
        return hour == 12 && minute == 0 && meridiem.isAm();
    }

    /**
     * Tells whether this time is exactly noon.
     *
     * @return true if this time is noon (12:00 PM)
     */
    public boolean isNoon() {
        return hour == 12 && minute == 0 && meridiem.isPm();
    }

    /**
     * Returns a new Time with the specified number of minutes added, wrapping round midnight.
     *
     * @param minutes the minutes to add (can be negative)
     * @return a new Time instance
     */
    public Time plusMinutes(int minutes) {
        LocalTime newTime = toLocalTime().plusMinutes(minutes);
        return from(newTime);
    }

    /**
     * Returns a new Time with the specified number of hours added, wrapping round midnight.
     *
     * @param hours the hours to add (can be negative)
     * @return a new Time instance
     */
    public Time plusHours(int hours) {
        LocalTime newTime = toLocalTime().plusHours(hours);
        return from(newTime);
    }

    /**
     * Returns a new Time with a different hour (12-hour format).
     *
     * @param hour the new hour (1-12)
     * @return a new Time instance
     * @throws ValueObjectException if {@code hour} is outside 1-12
     */
    public Time withHour(int hour) {
        return of(hour, this.minute, this.meridiem);
    }

    /**
     * Returns a new Time with a different minute.
     *
     * @param minute the new minute (0-59)
     * @return a new Time instance
     * @throws ValueObjectException if {@code minute} is outside 0-59
     */
    public Time withMinute(int minute) {
        return of(this.hour, minute, this.meridiem);
    }

    /**
     * Returns a new Time with a different meridiem.
     *
     * @param meridiem the new meridiem
     * @return a new Time instance
     * @throws ValueObjectException if {@code meridiem} is null
     */
    public Time withMeridiem(Meridiem meridiem) {
        return of(this.hour, this.minute, meridiem);
    }

    /**
     * Calculates minutes between this time and another time on the same day; it does not wrap round midnight.
     *
     * @param other the other time
     * @return minutes difference (positive if other is after this), from -1439 to 1439
     * @throws ValueObjectException if {@code other} is null
     */
    public int minutesUntil(Time other) {
        requireNonNull(other);
        int thisMinutes = hour24() * 60 + minute;
        int otherMinutes = other.hour24() * 60 + other.minute;
        return otherMinutes - thisMinutes;
    }

    /**
     * Checks if this time is before another time.
     *
     * @param other the other time
     * @return true if this time is before the other
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isBefore(Time other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks if this time is after another time.
     *
     * @param other the other time
     * @return true if this time is after the other
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isAfter(Time other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if this time is between two other times, both included. When {@code end} is before {@code start}
     * the range wraps across midnight: {@code 10:00 PM} to {@code 2:00 AM} holds {@code 11:00 PM} and
     * {@code 1:00 AM} but not noon.
     *
     * @param start the start time
     * @param end   the end time
     * @return true if this time is in the range from start to end
     * @throws ValueObjectException if {@code start} or {@code end} is null
     */
    public boolean isBetween(Time start, Time end) {
        requireNonNull(start);
        requireNonNull(end);
        if (!end.isBefore(start)) {
            return !isBefore(start) && !isAfter(end);
        }
        return !isBefore(start) || !isAfter(end);
    }

    // ==================== PRIVATE HELPERS ====================

    private static void requireNonNull(@Nullable Time other) {
        if (other == null) throw new ValueObjectException("A Time is required");
    }

    private static void validateHour12(int hour) {
        if (hour < 1 || hour > 12) {
            throw new ValueObjectException(
                    "Hour must be between 1 and 12 for 12-hour format, got: " + hour);
        }
    }

    private static void validateHour24(int hour) {
        if (hour < 0 || hour > 23) {
            throw new ValueObjectException(
                    "Hour must be between 0 and 23 for 24-hour format, got: " + hour);
        }
    }

    private static void validateMinute(int minute) {
        if (minute < 0 || minute > 59) {
            throw new ValueObjectException(
                    "Minute must be between 0 and 59, got: " + minute);
        }
    }

    private static int convertTo12Hour(int hour24) {
        if (hour24 == 0) {
            return 12; // Midnight
        } else if (hour24 <= 12) {
            return hour24;
        } else {
            return hour24 - 12;
        }
    }

    private static int convertTo24Hour(int hour12, Meridiem meridiem) {
        if (meridiem.isAm()) {
            return hour12 == 12 ? 0 : hour12;
        } else {
            return hour12 == 12 ? 12 : hour12 + 12;
        }
    }

    // ==================== OBJECT METHODS ====================

    /**
     * Orders times from midnight ({@code 12:00 AM}) to {@code 11:59 PM}.
     *
     * @param other the time to compare to
     * @return negative, zero or positive as this time is earlier than, the same as or later than {@code other}
     * @throws ValueObjectException if {@code other} is null
     */
    @Override
    public int compareTo(Time other) {
        requireNonNull(other);
        int thisTotal = hour24() * 60 + minute;
        int otherTotal = other.hour24() * 60 + other.minute;
        return Integer.compare(thisTotal, otherTotal);
    }

    /**
     * Writes the time as {@link #to12HourFormat()} does.
     *
     * @return e.g. {@code "9:05 AM"}
     */
    @Override
    public String toString() {
        return to12HourFormat();
    }

    /**
     * Parses a time like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link Time}, or empty when it is not one; never throws
     */
    public static Optional<Time> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is a time that {@link #parse(String)} accepts.
     *
     * @param text any text, or null
     * @return whether it is a {@link Time} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
