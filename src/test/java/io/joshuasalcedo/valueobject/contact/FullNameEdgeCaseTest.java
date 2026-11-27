package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FullNameEdgeCaseTest {

    @ParameterizedTest
    @ValueSource(strings = {"Bautista,", "Bautista, ", ",", " , "})
    void aCommaWithNoGivenNameIsAValueObjectException(String text) {
        assertThrows(ValueObjectException.class, () -> FullName.parse(text));
        assertTrue(FullName.tryParse(text).isEmpty());
        assertFalse(FullName.isValid(text));
    }

    @Test
    void aLeadingCommaLeavesNoSurname() {
        FullName n = FullName.parse(", Andrea Lim");
        assertEquals("Andrea", n.firstName());
        assertEquals("Lim", n.middleName());
        assertNull(n.lastName());
    }

    @Test
    void oneWordNamesAreValidAndTryParsed() {
        assertEquals("Cher", FullName.parse("Cher").firstName());
        assertTrue(FullName.isValid("Cher"));
        assertEquals(FullName.parse("Cher"), FullName.tryParse(" cher ").orElseThrow());
        assertFalse(FullName.looksLikePerson("Cher"), "looksLikePerson stays stricter");
    }

    @Test
    void tryParseAcceptsWhatParseAccepts() {
        for (String s : List.of("Cher", "Juan dela Cruz", "Bautista, Andrea Lim", "Ms. Andrea Lim Bautista",
                "Engr. Jose P. Rizal Jr.", "ANNA MARIA DE LA PAZ SANTOS GARCIA")) {
            FullName parsed = FullName.parse(s);
            assertEquals(parsed, FullName.tryParse(s).orElseThrow(), s);
            assertTrue(FullName.isValid(s), s);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "Northwind Cloud", "Skyline Telecom, Inc.", "R2 D2", "a@b.com"})
    void rejectsWhatIsNotAPersonsName(String text) {
        assertThrows(ValueObjectException.class, () -> FullName.parse(text));
        assertTrue(FullName.tryParse(text).isEmpty());
        assertFalse(FullName.isValid(text));
    }

    @Test
    void constructorRejectsNullFirstNameWithValueObjectException() {
        assertThrows(ValueObjectException.class, () -> new FullName(null, null, "Cruz"));
        assertThrows(ValueObjectException.class, () -> new FullName("  ", null, null));
        assertThrows(ValueObjectException.class, () -> new FullName("J0hn", null, null));
        assertThrows(ValueObjectException.class, () -> new FullName("A".repeat(51), null, null));
    }

    @Test
    void constructorNormalizesWhitespace() {
        FullName n = new FullName("  Andrea  ", "  ", " Lim   Bautista ");
        assertEquals("Andrea", n.firstName());
        assertNull(n.middleName());
        assertEquals("Lim Bautista", n.lastName());
    }

    @Test
    void formatsAndOrders() {
        FullName n = FullName.parse("Ms. Andrea Lim Bautista");
        assertEquals("Andrea Lim Bautista", n.fullName());
        assertEquals("Bautista, Andrea Lim", n.formalName());
        assertEquals("ALB", n.initials());
        assertEquals("Cher", FullName.parse("Cher").formalName());
        assertTrue(FullName.parse("Ana Cruz").compareTo(FullName.parse("Ben Dizon")) < 0);
        assertTrue(FullName.parse("Ana Cruz").compareTo(FullName.parse("Cher")) < 0, "missing surname last");
    }
}
