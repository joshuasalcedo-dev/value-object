package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FullNameTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', nullValues = "-", value = {
            "Ms. Andrea Lim Bautista    | Andrea | Lim       | Bautista",
            "Juan dela Cruz             | Juan   | -         | dela Cruz",
            "Maria de los Santos Reyes  | Maria  | de los Santos | Reyes",
            "Engr. Jose P. Rizal Jr.    | Jose   | P         | Rizal",
            "'Bautista, Andrea Lim'     | Andrea | Lim       | Bautista",
            "HARRIS DANIEL J            | Harris | Daniel    | J",
            "Cher                       | Cher   | -         | -",
    })
    void parsesPrintedNames(String text, String first, String middle, String last) {
        FullName n = FullName.parse(text);
        assertEquals(first, n.firstName());
        assertEquals(middle, n.middleName());
        assertEquals(last, n.lastName());
    }

    @Test
    void tellsPeopleFromBusinesses() {
        assertTrue(FullName.looksLikePerson("Ms. Andrea Lim Bautista"));
        assertTrue(FullName.looksLikePerson("Maria Santos Dizon"));
        assertFalse(FullName.looksLikePerson("Skyline Telecom, Inc."));
        assertFalse(FullName.looksLikePerson("Brightpath Media LLC"));
        assertFalse(FullName.looksLikePerson("BEAN & LEAF CAFE"));
        assertFalse(FullName.looksLikePerson("Unit 5B Palm Residences"));
        assertTrue(FullName.tryParse("Northwind Cloud").isEmpty());
        assertThrows(ValueObjectException.class, () -> FullName.parse(" "));
    }
}
