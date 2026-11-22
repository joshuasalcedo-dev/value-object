package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.geo.Country;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tax identification number: a TIN, a VAT number, an EIN. Its country is read from its form where the form tells
 * it (a Philippine TIN {@code 123-456-789-000}, an EU VAT number {@code DE123456789}, a US EIN {@code 12-3456789}),
 * else it is {@link Country#UNKNOWN}.
 *
 * <pre>{@code
 * TaxId.parse("555-123-456-000");   // PHILIPPINES, tin
 * TaxId.parse("DE123456789");       // GERMANY, vat
 * TaxId.of("555012345", Country.UNITED_STATES);
 * }</pre>
 *
 * @param value   the number as printed, trimmed, letters upper case
 * @param country the country that issued it
 * @author JoshuaSalcedo
 */
public record TaxId(String value, Country country) implements ValueObject {

    private static final Pattern PH_TIN = Pattern.compile("^\\d{3}-?\\d{3}-?\\d{3}(-?\\d{3,5})?$");
    private static final Pattern US_EIN = Pattern.compile("^\\d{2}-\\d{7}$");
    private static final Pattern EU_VAT = Pattern.compile("^(AT|BE|BG|CY|CZ|DE|DK|EE|EL|ES|FI|FR|HR|HU|IE|IT|LT|LU|LV|MT|NL|PL|PT|RO|SE|SI|SK|XI|GB|CH|NO)\\s?((?=(?:[A-Z]*\\d){6})[0-9A-Z]{8,12})$");

    /**
     * @throws ValueObjectException if it is not 5-20 letters, digits and separators
     */
    public TaxId {
        if (value == null || value.isBlank()) throw new ValueObjectException("A tax id is required");
        value = value.strip().replaceAll("\\s+", " ").replaceAll("[.]$", "").toUpperCase(Locale.ROOT);
        if (country == null) country = Country.UNKNOWN;
        String core = value.replaceAll("[\\s./-]", "");
        if (!core.matches("[0-9A-Z]{5,20}") || !core.matches(".*\\d.*"))
            throw new ValueObjectException("Not a tax id: " + value);
    }

    /**
     * @param text a tax id as printed
     * @return it, with its country when the form tells it
     * @throws ValueObjectException if it is not a tax id
     */
    public static TaxId parse(String text) {
        if (text == null) throw new ValueObjectException("A tax id is required");
        String t = text.strip().replaceAll("[.]$", "").toUpperCase(Locale.ROOT);
        if (PH_TIN.matcher(t).matches() && t.contains("-")) return new TaxId(t, Country.PHILIPPINES);
        if (US_EIN.matcher(t).matches()) return new TaxId(t, Country.UNITED_STATES);
        Matcher eu = EU_VAT.matcher(t);
        if (eu.matches()) {
            String code = eu.group(1).equals("EL") ? "GR" : eu.group(1).equals("XI") ? "GB" : eu.group(1);
            Country c = Country.fromCode(code);
            return new TaxId(t.replace(" ", ""), c == null ? Country.UNKNOWN : c);
        }
        return new TaxId(t, Country.UNKNOWN);
    }

    /**
     * @param value   the number as printed
     * @param country the country that issued it
     * @return it
     */
    public static TaxId of(String value, Country country) {
        return new TaxId(value, country);
    }

    /**
     * @param text any text
     * @return whether it reads as a tax id
     */
    public static boolean isValid(@Nullable String text) {
        if (text == null) return false;
        try {
            parse(text);
            return true;
        } catch (ValueObjectException e) {
            return false;
        }
    }

    /** @return its letters and digits only */
    public String digits() {
        return value.replaceAll("[^0-9A-Z]", "");
    }

    /** @return whether it is an EU-style VAT number (country prefix) */
    public boolean isVat() {
        return EU_VAT.matcher(value).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}
