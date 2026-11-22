package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.ValueObject;


import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum Language implements ValueObject {

    ARABIC("ar", "Arabic", "العربية"),
    BENGALI("bn", "Bangla", "বাংলা"),
    CHINESE("zh", "Chinese", "中文"),
    DANISH("da", "Danish", "dansk"),
    DUTCH("nl", "Dutch", "Nederlands"),
    ENGLISH("en", "English", "English"),
    FINNISH("fi", "Finnish", "suomi"),
    FRENCH("fr", "French", "français"),
    GERMAN("de", "German", "Deutsch"),
    GREEK("el", "Greek", "Ελληνικά"),
    HEBREW("he", "Hebrew", "עברית"),
    HINDI("hi", "Hindi", "हिन्दी"),
    INDONESIAN("id", "Indonesian", "Indonesia"),
    ITALIAN("it", "Italian", "italiano"),
    JAPANESE("ja", "Japanese", "日本語"),
    KOREAN("ko", "Korean", "한국어"),
    MALAY("ms", "Malay", "Melayu"),
    NORWEGIAN("no", "Norwegian", "norsk"),
    PERSIAN("fa", "Persian", "فارسی"),
    POLISH("pl", "Polish", "polski"),
    PORTUGUESE("pt", "Portuguese", "português"),
    ROMANIAN("ro", "Romanian", "română"),
    RUSSIAN("ru", "Russian", "русский"),
    SPANISH("es", "Spanish", "español"),
    SWEDISH("sv", "Swedish", "svenska"),
    TAGALOG("tl", "Tagalog", "Tagalog"),
    THAI("th", "Thai", "ไทย"),
    TURKISH("tr", "Turkish", "Türkçe"),
    UKRAINIAN("uk", "Ukrainian", "українська"),
    VIETNAMESE("vi", "Vietnamese", "Tiếng Việt"),

    UNKNOWN("xx", "Unknown", "Unknown");

    /**
     * @return whether this is {@link #UNKNOWN}
     */
    public boolean isNull() {
        return this == UNKNOWN;
    }

    // Note: Full language list truncated for brevity - in production, include all languages

    private final String code;
    private final String displayName;
    private final String nativeName;

    Language(String code, String displayName, String nativeName) {
        this.code = code;
        this.displayName = displayName;
        this.nativeName = nativeName;
    }


    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public String nativeName() {
        return nativeName;
    }

    public static Optional<Language> fromCode(String code) {
        return Arrays.stream(values())
                .filter(language -> language.code.equalsIgnoreCase(code))
                .findFirst();
    }

    public static Optional<Language> fromName(String name) {
        return Arrays.stream(values())
                .filter(language -> language.displayName.equalsIgnoreCase(name))
                .findFirst();
    }



    public static Optional<Language> fromLocale(Locale locale) {
        return fromCode(locale.getLanguage());
    }


    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Compares languages by display name alphabetically.
     *
     * @param other the Language to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByName(Language other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }

    /**
     * Compares languages by ISO code alphabetically.
     *
     * @param other the Language to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByCode(Language other) {
        return this.code.compareTo(other.code);
    }

    /**
     * Returns the null object representation of Language.
     *
     * @return the UNKNOWN language constant
     */
    public static Language nullValue() {
        return UNKNOWN;
    }
}
