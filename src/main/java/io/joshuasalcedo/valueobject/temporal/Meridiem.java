package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;

public enum Meridiem implements ValueObject {
    AM("AM", "Ante Meridiem", 0, 11),
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

    public static Meridiem fromHour(int hour24) {
        if (hour24 < 0 || hour24 > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        return hour24 < 12 ? AM : PM;
    }

    public static Meridiem fromLocalTime(java.time.LocalTime time) {
        return fromHour(time.getHour());
    }

    public static Meridiem now() {
        return fromLocalTime(java.time.LocalTime.now());
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getFullName() {
        return fullName;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public boolean isAm() {
        return this == AM;
    }

    public boolean isPm() {
        return this == PM;
    }

    @Override
    public String toString() {
        return abbreviation;
    }
}