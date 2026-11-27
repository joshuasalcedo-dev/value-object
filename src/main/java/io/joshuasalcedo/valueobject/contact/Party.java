package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.identifier.TaxId;

import java.util.List;
import java.util.Optional;

/**
 * Someone on a bill, invoice or receipt: the issuer (seller, merchant, biller) or the customer, with whatever
 * contact details were found next to the name.
 *
 * @param name    the name, e.g. {@code "Northwind Cloud"}
 * @param address the postal address, if one was found
 * @param email   the email address, if one was found
 * @param phone   the phone number, if one was found
 * @param website the website, if one was found
 * @param taxId   a tax or VAT number, if one was found
 * @param lines   the lines the party was read from, as printed
 * @author JoshuaSalcedo
 */
public record Party(String name, Optional<Address> address, Optional<Email> email, Optional<PhoneNumber> phone,
                    Optional<Link> website, Optional<TaxId> taxId, List<String> lines) implements ValueObject {

    /**
     * Creates a party, stripping the name; {@code null} optionals become empty and {@code null} lines an empty
     * list.
     *
     * @throws ValueObjectException if the name is {@code null} or blank
     */
    public Party {
        if (name == null || name.isBlank()) throw new ValueObjectException("A party needs a name");
        name = name.strip();
        address = address == null ? Optional.empty() : address;
        email = email == null ? Optional.empty() : email;
        phone = phone == null ? Optional.empty() : phone;
        website = website == null ? Optional.empty() : website;
        taxId = taxId == null ? Optional.empty() : taxId;
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    /**
     * Creates a party with only a name; its lines are just the name.
     *
     * @param name the name
     * @return a party with only a name
     * @throws ValueObjectException if the name is {@code null} or blank
     */
    public static Party of(String name) {
        if (name == null || name.isBlank()) throw new ValueObjectException("A party needs a name");
        return new Party(name, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                List.of(name));
    }

    /**
     * Reads the name as a person's name: when it {@link FullName#looksLikePerson(String) looks like one}, as
     * {@link FullName#parse(String)} reads it.
     *
     * @return the name as a person's name (first, middle, last), when it reads as one rather than a business's
     *         ({@code "Ms. Andrea Lim Bautista"} yes, {@code "Skyline Telecom, Inc."} no)
     */
    public Optional<FullName> person() {
        return FullName.looksLikePerson(name) ? FullName.tryParse(name) : Optional.empty();
    }

    /** {@return whether the name reads as a person's rather than a business's} */
    public boolean isPerson() {
        return person().isPresent();
    }

    @Override
    public String toString() {
        return name + address.map(a -> ", " + a).orElse("") + email.map(e -> " <" + e + ">").orElse("");
    }
}
