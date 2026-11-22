package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Immutable value object representing a time in 12-hour format.
 * Supports conversion to/from 24-hour format and various string representations.
 */
public record Time(int hour, int minute, Meridiem meridiem) implements ValueObject, Comparable<Time> {

    // ==================== COMPACT CONSTRUCTOR ====================

    public Time {
        validateHour12(hour);
        validateMinute(minute);
        Objects.requireNonNull(meridiem, "Meridiem cannot be null");
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Creates a Time from 12-hour format components.
     *
     * @param hour     the hour (1-12)
     * @param minute   the minute (0-59)
     * @param meridiem AM or PM
     * @return a new Time instance
     * @throws IllegalArgumentException if hour or minute is out of range
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
     * @throws IllegalArgumentException if hour or minute is out of range
     */
    public static Time of24(int hour24, int minute) {
        validateHour24(hour24);
        validateMinute(minute);

        Meridiem meridiem = Meridiem.fromHour(hour24);
        int hour12 = convertTo12Hour(hour24);

        return new Time(hour12, minute, meridiem);
    }

    /**
     * Creates a Time from a LocalTime.
     *
     * @param localTime the LocalTime to convert
     * @return a new Time instance
     */
    public static Time from(LocalTime localTime) {
        Objects.requireNonNull(localTime, "LocalTime cannot be null");
        return of24(localTime.getHour(), localTime.getMinute());
    }

    /**
     * Creates a Time representing the current time.
     *
     * @return a new Time instance for now
     */
    public static Time now() {
        return from(LocalTime.now());
    }

    /**
     * Parses a time string in various formats.
     * Supported formats: "11:30 AM", "11:30AM", "23:30", "9:05 PM"
     *
     * @param timeString the string to parse
     * @return a new Time instance
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public static Time parse(String timeString) {
        Objects.requireNonNull(timeString, "Time string cannot be null");
        String normalized = timeString.trim().toUpperCase();

        // Try 12-hour format with AM/PM
        if (normalized.contains("AM") || normalized.contains("PM")) {
            return parse12HourFormat(normalized);
        }

        // Try 24-hour format
        return parse24HourFormat(normalized);
    }

    private static Time parse12HourFormat(String timeString) {
        boolean isPm = timeString.contains("PM");
        String timePart = timeString
                .replace("AM", "")
                .replace("PM", "")
                .trim();

        String[] parts = timePart.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid time format: " + timeString);
        }

        try {
            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());
            Meridiem meridiem = isPm ? Meridiem.PM : Meridiem.AM;
            return of(hour, minute, meridiem);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time format: " + timeString, e);
        }
    }

    private static Time parse24HourFormat(String timeString) {
        String[] parts = timeString.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid time format: " + timeString);
        }

        try {
            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());
            return of24(hour, minute);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time format: " + timeString, e);
        }
    }

    // ==================== DERIVED GETTERS ====================

    /**
     * @return the hour in 24-hour format (0-23)
     */
    public int hour24() {
        return convertTo24Hour(hour, meridiem);
    }

    // ==================== CONVERSION METHODS ====================

    /**
     * Converts to a LocalTime.
     *
     * @return the equivalent LocalTime
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
        return String.format("%02d:%02d", hour24(), minute);
    }

    /**
     * Returns the time in 12-hour format string (h:mm AM/PM).
     *
     * @return formatted string like "11:30 PM" or "9:05 AM"
     */
    public String to12HourFormat() {
        return String.format("%d:%02d %s", hour, minute, meridiem.getAbbreviation());
    }

    /**
     * Returns the time in 12-hour format with padded hour (hh:mm AM/PM).
     *
     * @return formatted string like "11:30 PM" or "09:05 AM"
     */
    public String to12HourFormatPadded() {
        return String.format("%02d:%02d %s", hour, minute, meridiem.getAbbreviation());
    }

    /**
     * Returns the time in ISO format (HH:mm:ss).
     *
     * @return formatted string like "23:30:00"
     */
    public String toIsoFormat() {
        return String.format("%02d:%02d:00", hour24(), minute);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * @return true if this time is in the morning (AM)
     */
    public boolean isMorning() {
        return meridiem.isAm();
    }

    /**
     * @return true if this time is in the afternoon/evening (PM)
     */
    public boolean isAfternoon() {
        return meridiem.isPm();
    }

    /**
     * @return true if this time is midnight (12:00 AM)
     */
    public boolean isMidnight() {
        return hour == 12 && minute == 0 && meridiem.isAm();
    }

    /**
     * @return true if this time is noon (12:00 PM)
     */
    public boolean isNoon() {
        return hour == 12 && minute == 0 && meridiem.isPm();
    }

    /**
     * Returns a new Time with the specified number of minutes added.
     *
     * @param minutes the minutes to add (can be negative)
     * @return a new Time instance
     */
    public Time plusMinutes(int minutes) {
        LocalTime newTime = toLocalTime().plusMinutes(minutes);
        return from(newTime);
    }

    /**
     * Returns a new Time with the specified number of hours added.
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
     */
    public Time withHour(int hour) {
        return of(hour, this.minute, this.meridiem);
    }

    /**
     * Returns a new Time with a different minute.
     *
     * @param minute the new minute (0-59)
     * @return a new Time instance
     */
    public Time withMinute(int minute) {
        return of(this.hour, minute, this.meridiem);
    }

    /**
     * Returns a new Time with a different meridiem.
     *
     * @param meridiem the new meridiem
     * @return a new Time instance
     */
    public Time withMeridiem(Meridiem meridiem) {
        return of(this.hour, this.minute, meridiem);
    }

    /**
     * Calculates minutes between this time and another time.
     *
     * @param other the other time
     * @return minutes difference (positive if other is after this)
     */
    public int minutesUntil(Time other) {
        int thisMinutes = hour24() * 60 + minute;
        int otherMinutes = other.hour24() * 60 + other.minute;
        return otherMinutes - thisMinutes;
    }

    /**
     * Checks if this time is before another time.
     *
     * @param other the other time
     * @return true if this time is before the other
     */
    public boolean isBefore(Time other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks if this time is after another time.
     *
     * @param other the other time
     * @return true if this time is after the other
     */
    public boolean isAfter(Time other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if this time is between two other times (inclusive).
     *
     * @param start the start time
     * @param end   the end time
     * @return true if this time is between start and end
     */
    public boolean isBetween(Time start, Time end) {
        return !isBefore(start) && !isAfter(end);
    }

    // ==================== PRIVATE HELPERS ====================

    private static void validateHour12(int hour) {
        if (hour < 1 || hour > 12) {
            throw new IllegalArgumentException(
                    "Hour must be between 1 and 12 for 12-hour format, got: " + hour);
        }
    }

    private static void validateHour24(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(
                    "Hour must be between 0 and 23 for 24-hour format, got: " + hour);
        }
    }

    private static void validateMinute(int minute) {
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(
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

    @Override
    public int compareTo(Time other) {
        int thisTotal = hour24() * 60 + minute;
        int otherTotal = other.hour24() * 60 + other.minute;
        return Integer.compare(thisTotal, otherTotal);
    }

    @Override
    public String toString() {
        return to12HourFormat();
    }
}
