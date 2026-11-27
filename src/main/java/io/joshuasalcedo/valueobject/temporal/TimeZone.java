package io.joshuasalcedo.valueobject.temporal;

import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;

/**
 * A selection of IANA time zones (zones in Africa and Argentina, and some major cities elsewhere), each with its
 * zone id, a display name and a fixed offset: the one in force outside summer time (for zones that Java's rules
 * model with negative daylight saving time, such as {@code AFRICA_CASABLANCA}, this is the usual offset rather
 * than the rules' "standard" one). The list is a subset of Java's
 * {@link ZoneId}s, not all of them.
 *
 * <p>Offsets are written {@code "GMT+8:00"} / {@code "GMT-5:00"} / {@code "GMT-0:30"}: a sign, hours without a
 * leading zero, then minutes. {@link #offset()} is a fixed value stored with the constant; {@link #offsetAt(Instant)}
 * and {@link #getCurrentOffset()} ask the zone rules for the offset at a moment, so they follow daylight saving
 * time.
 *
 * @author JoshuaSalcedo
 */
public enum TimeZone implements ValueObject {

    /** Africa/Abidjan. */
    AFRICA_ABIDJAN("Africa/Abidjan", "Abidjan Time", "GMT+0:00"),
    /** Africa/Accra. */
    AFRICA_ACCRA("Africa/Accra", "Accra Time", "GMT+0:00"),
    /** Africa/Addis_Ababa. */
    AFRICA_ADDIS_ABABA("Africa/Addis_Ababa", "Addis Ababa Time", "GMT+3:00"),
    /** Africa/Algiers. */
    AFRICA_ALGIERS("Africa/Algiers", "Central European Time", "GMT+1:00"),
    /** Africa/Asmara. */
    AFRICA_ASMARA("Africa/Asmara", "Asmara Time", "GMT+3:00"),
    /** Africa/Asmera. */
    AFRICA_ASMERA("Africa/Asmera", "Asmara Time", "GMT+3:00"),
    /** Africa/Bamako. */
    AFRICA_BAMAKO("Africa/Bamako", "Bamako Time", "GMT+0:00"),
    /** Africa/Bangui. */
    AFRICA_BANGUI("Africa/Bangui", "West Africa Time", "GMT+1:00"),
    /** Africa/Banjul. */
    AFRICA_BANJUL("Africa/Banjul", "Banjul Time", "GMT+0:00"),
    /** Africa/Bissau. */
    AFRICA_BISSAU("Africa/Bissau", "Bissau Time", "GMT+0:00"),
    /** Africa/Blantyre. */
    AFRICA_BLANTYRE("Africa/Blantyre", "Blantyre Time", "GMT+2:00"),
    /** Africa/Brazzaville. */
    AFRICA_BRAZZAVILLE("Africa/Brazzaville", "West Africa Time", "GMT+1:00"),
    /** Africa/Bujumbura. */
    AFRICA_BUJUMBURA("Africa/Bujumbura", "Bujumbura Time", "GMT+2:00"),
    /** Africa/Cairo. */
    AFRICA_CAIRO("Africa/Cairo", "Eastern European Time", "GMT+2:00"),
    /** Africa/Casablanca. */
    AFRICA_CASABLANCA("Africa/Casablanca", "Casablanca Time", "GMT+1:00"),
    /** Africa/Ceuta. */
    AFRICA_CEUTA("Africa/Ceuta", "Central European Time", "GMT+1:00"),
    /** Africa/Conakry. */
    AFRICA_CONAKRY("Africa/Conakry", "Conakry Time", "GMT+0:00"),
    /** Africa/Dakar. */
    AFRICA_DAKAR("Africa/Dakar", "Dakar Time", "GMT+0:00"),
    /** Africa/Dar_es_Salaam. */
    AFRICA_DAR_ES_SALAAM("Africa/Dar_es_Salaam", "Dar es Salaam Time", "GMT+3:00"),
    /** Africa/Djibouti. */
    AFRICA_DJIBOUTI("Africa/Djibouti", "Djibouti Time", "GMT+3:00"),
    /** Africa/Douala. */
    AFRICA_DOUALA("Africa/Douala", "West Africa Time", "GMT+1:00"),
    /** Africa/El_Aaiun. */
    AFRICA_EL_AAIUN("Africa/El_Aaiun", "El Aaiun Time", "GMT+1:00"),
    /** Africa/Freetown. */
    AFRICA_FREETOWN("Africa/Freetown", "Freetown Time", "GMT+0:00"),
    /** Africa/Gaborone. */
    AFRICA_GABORONE("Africa/Gaborone", "Gaborone Time", "GMT+2:00"),
    /** Africa/Harare. */
    AFRICA_HARARE("Africa/Harare", "Harare Time", "GMT+2:00"),
    /** Africa/Johannesburg. */
    AFRICA_JOHANNESBURG("Africa/Johannesburg", "Johannesburg Time", "GMT+2:00"),
    /** Africa/Juba. */
    AFRICA_JUBA("Africa/Juba", "Juba Time", "GMT+2:00"),
    /** Africa/Kampala. */
    AFRICA_KAMPALA("Africa/Kampala", "Kampala Time", "GMT+3:00"),
    /** Africa/Khartoum. */
    AFRICA_KHARTOUM("Africa/Khartoum", "Khartoum Time", "GMT+2:00"),
    /** Africa/Kigali. */
    AFRICA_KIGALI("Africa/Kigali", "Kigali Time", "GMT+2:00"),
    /** Africa/Kinshasa. */
    AFRICA_KINSHASA("Africa/Kinshasa", "West Africa Time", "GMT+1:00"),
    /** Africa/Lagos. */
    AFRICA_LAGOS("Africa/Lagos", "West Africa Time", "GMT+1:00"),
    /** Africa/Libreville. */
    AFRICA_LIBREVILLE("Africa/Libreville", "West Africa Time", "GMT+1:00"),
    /** Africa/Lome. */
    AFRICA_LOME("Africa/Lome", "Lome Time", "GMT+0:00"),
    /** Africa/Luanda. */
    AFRICA_LUANDA("Africa/Luanda", "West Africa Time", "GMT+1:00"),
    /** Africa/Lubumbashi. */
    AFRICA_LUBUMBASHI("Africa/Lubumbashi", "Lubumbashi Time", "GMT+2:00"),
    /** Africa/Lusaka. */
    AFRICA_LUSAKA("Africa/Lusaka", "Lusaka Time", "GMT+2:00"),
    /** Africa/Malabo. */
    AFRICA_MALABO("Africa/Malabo", "West Africa Time", "GMT+1:00"),
    /** Africa/Maputo. */
    AFRICA_MAPUTO("Africa/Maputo", "Maputo Time", "GMT+2:00"),
    /** Africa/Maseru. */
    AFRICA_MASERU("Africa/Maseru", "Maseru Time", "GMT+2:00"),
    /** Africa/Mbabane. */
    AFRICA_MBABANE("Africa/Mbabane", "Mbabane Time", "GMT+2:00"),
    /** Africa/Mogadishu. */
    AFRICA_MOGADISHU("Africa/Mogadishu", "Mogadishu Time", "GMT+3:00"),
    /** Africa/Monrovia. */
    AFRICA_MONROVIA("Africa/Monrovia", "Monrovia Time", "GMT+0:00"),
    /** Africa/Nairobi. */
    AFRICA_NAIROBI("Africa/Nairobi", "Nairobi Time", "GMT+3:00"),
    /** Africa/Ndjamena. */
    AFRICA_NDJAMENA("Africa/Ndjamena", "West Africa Time", "GMT+1:00"),
    /** Africa/Niamey. */
    AFRICA_NIAMEY("Africa/Niamey", "West Africa Time", "GMT+1:00"),
    /** Africa/Nouakchott. */
    AFRICA_NOUAKCHOTT("Africa/Nouakchott", "Nouakchott Time", "GMT+0:00"),
    /** Africa/Ouagadougou. */
    AFRICA_OUAGADOUGOU("Africa/Ouagadougou", "Ouagadougou Time", "GMT+0:00"),
    /** Africa/Porto-Novo. */
    AFRICA_PORTO_NOVO("Africa/Porto-Novo", "West Africa Time", "GMT+1:00"),
    /** Africa/Sao_Tome. */
    AFRICA_SAO_TOME("Africa/Sao_Tome", "São Tomé Time", "GMT+0:00"),
    /** Africa/Timbuktu. */
    AFRICA_TIMBUKTU("Africa/Timbuktu", "Bamako Time", "GMT+0:00"),
    /** Africa/Tripoli. */
    AFRICA_TRIPOLI("Africa/Tripoli", "Eastern European Time", "GMT+2:00"),
    /** Africa/Tunis. */
    AFRICA_TUNIS("Africa/Tunis", "Central European Time", "GMT+1:00"),
    /** Africa/Windhoek. */
    AFRICA_WINDHOEK("Africa/Windhoek", "Windhoek Time", "GMT+2:00"),
    /** America/Adak. */
    AMERICA_ADAK("America/Adak", "Hawaii-Aleutian Time", "GMT-10:00"),
    /** America/Anchorage. */
    AMERICA_ANCHORAGE("America/Anchorage", "Alaska Time", "GMT-9:00"),
    /** America/Anguilla. */
    AMERICA_ANGUILLA("America/Anguilla", "Atlantic Time", "GMT-4:00"),
    /** America/Antigua. */
    AMERICA_ANTIGUA("America/Antigua", "Atlantic Time", "GMT-4:00"),
    /** America/Araguaina. */
    AMERICA_ARAGUAINA("America/Araguaina", "Brasilia Time", "GMT-3:00"),
    /** America/Argentina/Buenos_Aires. */
    AMERICA_ARGENTINA_BUENOS_AIRES("America/Argentina/Buenos_Aires", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/Catamarca. */
    AMERICA_ARGENTINA_CATAMARCA("America/Argentina/Catamarca", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/ComodRivadavia. */
    AMERICA_ARGENTINA_COMODRIVADAVIA("America/Argentina/ComodRivadavia", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/Cordoba. */
    AMERICA_ARGENTINA_CORDOBA("America/Argentina/Cordoba", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/Jujuy. */
    AMERICA_ARGENTINA_JUJUY("America/Argentina/Jujuy", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/La_Rioja. */
    AMERICA_ARGENTINA_LA_RIOJA("America/Argentina/La_Rioja", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/Mendoza. */
    AMERICA_ARGENTINA_MENDOZA("America/Argentina/Mendoza", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/Rio_Gallegos. */
    AMERICA_ARGENTINA_RIO_GALLEGOS("America/Argentina/Rio_Gallegos", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/Salta. */
    AMERICA_ARGENTINA_SALTA("America/Argentina/Salta", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/San_Juan. */
    AMERICA_ARGENTINA_SAN_JUAN("America/Argentina/San_Juan", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/San_Luis. */
    AMERICA_ARGENTINA_SAN_LUIS("America/Argentina/San_Luis", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/Tucuman. */
    AMERICA_ARGENTINA_TUCUMAN("America/Argentina/Tucuman", "Argentina Time", "GMT-3:00"),
    /** America/Argentina/Ushuaia. */
    AMERICA_ARGENTINA_USHUAIA("America/Argentina/Ushuaia", "Argentina Time", "GMT-3:00"),
    /** America/New_York. */
    AMERICA_NEW_YORK("America/New_York", "Eastern Time", "GMT-5:00"),
    /** America/Chicago. */
    AMERICA_CHICAGO("America/Chicago", "Central Time", "GMT-6:00"),
    /** America/Denver. */
    AMERICA_DENVER("America/Denver", "Mountain Time", "GMT-7:00"),
    /** America/Los_Angeles. */
    AMERICA_LOS_ANGELES("America/Los_Angeles", "Pacific Time", "GMT-8:00"),
    /** Asia/Tokyo. */
    ASIA_TOKYO("Asia/Tokyo", "Japan Time", "GMT+9:00"),
    /** Asia/Shanghai. */
    ASIA_SHANGHAI("Asia/Shanghai", "China Time", "GMT+8:00"),
    /** Asia/Manila. */
    ASIA_MANILA("Asia/Manila", "Philippine Time", "GMT+8:00"),
    /** Asia/Singapore. */
    ASIA_SINGAPORE("Asia/Singapore", "Singapore Time", "GMT+8:00"),
    /** Europe/London. */
    EUROPE_LONDON("Europe/London", "London Time", "GMT+0:00"),
    /** Europe/Paris. */
    EUROPE_PARIS("Europe/Paris", "Central European Time", "GMT+1:00"),
    /** Europe/Berlin. */
    EUROPE_BERLIN("Europe/Berlin", "Central European Time", "GMT+1:00"),
    /** Australia/Sydney. */
    AUSTRALIA_SYDNEY("Australia/Sydney", "Australian Eastern Time", "GMT+10:00"),
    /** Pacific/Auckland. */
    PACIFIC_AUCKLAND("Pacific/Auckland", "New Zealand Time", "GMT+12:00"),
    /** Coordinated Universal Time ({@code UTC}). */
    UTC("UTC", "Coordinated Universal Time", "GMT+0:00");

    // Note: Full timezone list truncated for brevity - in production, include all 604 timezones

    private final String zoneId;
    private final String displayName;
    private final String offset;
    private final ZoneOffset standardOffset;

    TimeZone(String zoneId, String displayName, String offset) {
        this.zoneId = zoneId;
        this.displayName = displayName;
        this.offset = offset;
        String[] hm = offset.substring(4).split(":");
        int sign = offset.charAt(3) == '-' ? -1 : 1;
        this.standardOffset = ZoneOffset.ofHoursMinutes(sign * Integer.parseInt(hm[0]), sign * Integer.parseInt(hm[1]));
    }

    /**
     * The IANA zone id.
     *
     * @return e.g. {@code "Asia/Manila"}
     */
    public String zoneId() {
        return zoneId;
    }

    /**
     * A name for people.
     *
     * @return e.g. {@code "Philippine Time"}
     */
    public String displayName() {
        return displayName;
    }

    /**
     * The offset stored with the constant; it does not change with daylight saving time.
     *
     * @return e.g. {@code "GMT+8:00"}
     */
    public String offset() {
        return offset;
    }

    /**
     * The stored offset as a Java offset; it does not change with daylight saving time.
     *
     * @return the {@link ZoneOffset} that {@link #offset()} writes, e.g. {@code +08:00}
     */
    public ZoneOffset standardOffset() {
        return standardOffset;
    }

    /**
     * The zone's offset from UTC at a given moment, from the zone rules (so it follows daylight saving time).
     *
     * @param instant the moment
     * @return the offset in force at that moment
     * @throws ValueObjectException if {@code instant} is null
     */
    public ZoneOffset offsetAt(Instant instant) {
        if (instant == null) throw new ValueObjectException("An instant is required");
        return toZoneId().getRules().getOffset(instant);
    }

    /**
     * Gets the current UTC offset (accounts for DST), from the zone rules at this moment, written as
     * {@link #formatOffset(ZoneOffset)} does.
     *
     * @return e.g. {@code "GMT+8:00"} or {@code "GMT-4:00"}
     */
    public String getCurrentOffset() {
        return formatOffset(offsetAt(Instant.now()));
    }

    /**
     * Writes an offset the way this class does: {@code "GMT"}, a sign, the hours without a leading zero, a colon
     * and two digits of minutes. The sign belongs to the whole offset, so minus half an hour is
     * {@code "GMT-0:30"}; any seconds are dropped.
     *
     * @param offset the offset
     * @return e.g. {@code "GMT+8:00"}, {@code "GMT+5:45"} or {@code "GMT-0:30"}
     * @throws ValueObjectException if {@code offset} is null
     */
    public static String formatOffset(ZoneOffset offset) {
        if (offset == null) throw new ValueObjectException("An offset is required");
        int totalSeconds = offset.getTotalSeconds();
        char sign = totalSeconds < 0 ? '-' : '+';
        int abs = Math.abs(totalSeconds);
        return String.format(java.util.Locale.ROOT, "GMT%c%d:%02d", sign, abs / 3600, (abs % 3600) / 60);
    }

    /**
     * The Java zone for this time zone.
     *
     * @return the {@link ZoneId} of {@link #zoneId()}
     */
    public ZoneId toZoneId() {
        return ZoneId.of(this.zoneId);
    }

    /**
     * Finds the constant for a zone id, or for a constant name; the match is exact (case and spacing count).
     *
     * @param zoneId a zone id such as {@code "Australia/Sydney"}, or a constant name such as
     *               {@code "AUSTRALIA_SYDNEY"}
     * @return the matching constant
     * @throws ValueObjectException if no constant matches
     */
    public static TimeZone fromZoneId(String zoneId) {
        for (TimeZone tz : values()) {
            // Match by zoneId (e.g., "Australia/Sydney")
            if (tz.zoneId.equals(zoneId)) {
                return tz;
            }
            // Also match by enum name (e.g., "AUSTRALIA_SYDNEY") for backward compatibility
            if (tz.name().equals(zoneId)) {
                return tz;
            }
        }
        throw new ValueObjectException("Unknown timezone: " + zoneId);
    }

    /**
     * Returns a formatted string like {@code "(GMT+8:00) Asia/Manila - Philippine Time"}, with the current offset.
     *
     * @return the current offset, zone id and display name
     */
    public String getFormattedDisplay() {
        return String.format("(%s) %s - %s", getCurrentOffset(), zoneId, displayName);
    }

    /**
     * Writes the zone as {@link #getFormattedDisplay()} does.
     *
     * @return e.g. {@code "(GMT+8:00) Asia/Manila - Philippine Time"}
     */
    @Override
    public String toString() {
        return getFormattedDisplay();
    }

    /**
     * Compares timezones by zone ID alphabetically.
     *
     * @param other the TimeZone to compare to
     * @return negative if this comes before, zero if equal, positive if after
     * @throws ValueObjectException if {@code other} is null
     */
    public int compareByZoneId(TimeZone other) {
        if (other == null) throw new ValueObjectException("A TimeZone to compare to is required");
        return this.zoneId.compareTo(other.zoneId);
    }

    /**
     * Compares timezones by their stored, standard offsets ({@link #standardOffset()}), west to east: a zone
     * further behind UTC sorts first, so {@code GMT-5:00} comes before {@code GMT+9:00}, which comes before
     * {@code GMT+10:00}.
     *
     * @param other the TimeZone to compare to
     * @return negative, zero or positive as this standard offset is behind, equal to or ahead of the other's
     * @throws ValueObjectException if {@code other} is null
     */
    public int compareByOffset(TimeZone other) {
        if (other == null) throw new ValueObjectException("A TimeZone to compare to is required");
        return Integer.compare(this.standardOffset.getTotalSeconds(), other.standardOffset.getTotalSeconds());
    }

    /**
     * Compares timezones by their offsets at a given moment ({@link #offsetAt(Instant)}), west to east, so
     * daylight saving time is taken into account.
     *
     * @param other   the TimeZone to compare to
     * @param instant the moment at which to compare the offsets
     * @return negative, zero or positive as this zone's offset is behind, equal to or ahead of the other's then
     * @throws ValueObjectException if an argument is null
     */
    public int compareByOffset(TimeZone other, Instant instant) {
        if (other == null) throw new ValueObjectException("A TimeZone to compare to is required");
        return Integer.compare(offsetAt(instant).getTotalSeconds(), other.offsetAt(instant).getTotalSeconds());
    }

    /**
     * A comparator that orders zones by their offsets at a given moment, west to east, as
     * {@link #compareByOffset(TimeZone, Instant)} does.
     *
     * @param instant the moment at which to compare the offsets
     * @return the comparator
     * @throws ValueObjectException if {@code instant} is null
     */
    public static Comparator<TimeZone> byOffsetAt(Instant instant) {
        if (instant == null) throw new ValueObjectException("An instant is required");
        return (a, b) -> a.compareByOffset(b, instant);
    }

    /**
     * Reads a zone id or constant name, as {@link #fromZoneId(String)} does, after stripping blanks around it.
     *
     * @param text a zone id, e.g. {@code "Asia/Manila"}
     * @return it
     * @throws ValueObjectException if it is not one
     */
    public static TimeZone parse(String text) {
        if (text == null) throw new ValueObjectException("A TimeZone is required");
        return fromZoneId(text.strip());
    }

    /**
     * Reads a zone id or constant name like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link TimeZone}, or empty when it is not one; never throws
     */
    public static Optional<TimeZone> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is a zone id or constant name that {@link #parse(String)} accepts.
     *
     * @param text any text, or null
     * @return whether it is a {@link TimeZone} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
