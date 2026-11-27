package io.joshuasalcedo.valueobject.social;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TelegramAccountTest {

    @ParameterizedTest
    @ValueSource(strings = {"andrea_lim", "@andrea_lim", "https://t.me/andrea_lim", "HTTPS://T.ME/andrea_lim/",
            "tg://resolve?domain=andrea_lim", "  @andrea_lim  ", "https://t.me/andrea_lim?start=1"})
    void readsEveryForm(String text) {
        TelegramAccount t = TelegramAccount.parse(text);
        assertEquals("andrea_lim", t.username());
        assertEquals("@andrea_lim", t.withAtSign());
        assertEquals("https://t.me/andrea_lim", t.toUrl());
        assertEquals("tg://resolve?domain=andrea_lim", t.toDeepLink());
        assertTrue(TelegramAccount.isValid(text));
        assertEquals(t, TelegramAccount.of(text));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "abcd", "1abcde", "abcde_", "ab__cd", "abc-def", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "@", "andréa"})
    void badInputIsAValueObjectException(String text) {
        assertThrows(ValueObjectException.class, () -> new TelegramAccount(text));
        assertThrows(ValueObjectException.class, () -> TelegramAccount.of(text));
        assertThrows(ValueObjectException.class, () -> TelegramAccount.parse(text));
        assertTrue(TelegramAccount.tryParse(text).isEmpty());
        assertFalse(TelegramAccount.isValid(text));
    }

    @Test
    void equalityIgnoresCase() {
        TelegramAccount a = TelegramAccount.of("Andrea_Lim");
        TelegramAccount b = TelegramAccount.of("andrea_lim");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.equalsIgnoreCase(b));
        assertFalse(a.equalsIgnoreCase(null));
        assertEquals("Andrea_Lim", a.toString());
        assertEquals("andrea_lim", a.normalized());
    }
}
