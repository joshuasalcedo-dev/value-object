package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.geo.Country;
import org.jspecify.annotations.Nullable;

/**
 * A bank's SWIFT / BIC code (ISO 9362): 4 letters for the bank, 2 for its country, 2 letters or digits for its
 * location, and an optional 3 for the branch ({@code "XXX"} is the head office).
 *
 * <pre>{@code
 * Bic bic = Bic.parse("BOPIPHMM");
 * bic.bankCode();   // "BOPI"
 * bic.country();    // PHILIPPINES
 * }</pre>
 *
 * <p>Read as printed: a code in lower case is not taken for one, so words are not mistaken for codes.
 *
 * @param value the code, 8 or 11 characters, upper case
 * @author JoshuaSalcedo
 */
public record Bic(String value) implements ValueObject, Comparable<Bic> {

    /**
     * @throws ValueObjectException if it is not a BIC with a known country
     */
    public Bic {
        if (value == null) throw new ValueObjectException("A BIC is required");
        value = value.strip().replace(" ", "");
        if (!value.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?"))
            throw new ValueObjectException("A BIC is 8 or 11 upper-case letters and digits: " + value);
        Country c = Country.fromCode(value.substring(4, 6));
        if (c == null || c == Country.UNKNOWN) throw new ValueObjectException("Not a country in a BIC: " + value.substring(4, 6));
    }

    /**
     * @param text the code, e.g. {@code "BOPIPHMM"} or {@code "DEUTDEFF500"}
     * @return it
     * @throws ValueObjectException if it is not a BIC
     */
    public static Bic parse(String text) {
        return new Bic(text);
    }

    /**
     * @param text any text
     * @return whether it is a BIC
     */
    public static boolean isValid(@Nullable String text) {
        if (text == null) return false;
        try {
            new Bic(text);
            return true;
        } catch (ValueObjectException e) {
            return false;
        }
    }

    /** @return the bank's code, e.g. {@code "DEUT"} */
    public String bankCode() {
        return value.substring(0, 4);
    }

    /** @return the country */
    public Country country() {
        return Country.fromCode(value.substring(4, 6));
    }

    /** @return the location code */
    public String locationCode() {
        return value.substring(6, 8);
    }

    /** @return the branch code, {@code "XXX"} for the head office or when none is given */
    public String branchCode() {
        return value.length() == 11 ? value.substring(8) : "XXX";
    }

    /** @return whether it names the head office */
    public boolean isHeadOffice() {
        return branchCode().equals("XXX");
    }

    @Override
    public int compareTo(Bic other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
