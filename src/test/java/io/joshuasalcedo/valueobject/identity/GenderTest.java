package io.joshuasalcedo.valueobject.identity;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenderTest {

    @ParameterizedTest
    @CsvSource({"F, FEMALE", "' f', FEMALE", "'female ', FEMALE", "M, MALE", "' NB ', NON_BINARY",
            "Non-Binary, NON_BINARY", "N, PREFER_NOT_TO_SAY", "o, OTHER"})
    void parsesCodesAndNamesTrimmed(String text, Gender expected) {
        assertEquals(expected, Gender.parse(text));
        assertTrue(Gender.isValid(text));
        assertEquals(expected, Gender.tryParse(text).orElseThrow());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "U", "unknown", "x"})
    void rejectsUnknown(String text) {
        assertThrows(ValueObjectException.class, () -> Gender.parse(text));
        assertTrue(Gender.tryParse(text).isEmpty());
        assertFalse(Gender.isValid(text));
    }

    @Test
    void accessors() {
        assertEquals("NB", Gender.NON_BINARY.code());
        assertEquals("Female", Gender.FEMALE.displayName());
        assertEquals("Female", Gender.FEMALE.toString());
        assertTrue(Gender.UNKNOWN.isUnknown());
        assertEquals(Gender.FEMALE, Gender.fromCode("f").orElseThrow());
        assertEquals(Gender.MALE, Gender.fromName("male").orElseThrow());
        assertTrue(Gender.fromCode(null).isEmpty());
        assertTrue(Gender.FEMALE.compareByName(Gender.MALE) < 0);
    }
}
