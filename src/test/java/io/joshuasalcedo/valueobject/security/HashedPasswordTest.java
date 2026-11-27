package io.joshuasalcedo.valueobject.security;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class HashedPasswordTest {

    static final String BCRYPT = "$2b$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
    static final String ARGON2 = "$argon2id$v=19$m=65536,t=3,p=4$c29tZXNhbHQ$RdescudvJCsgt3ub+b+dWRWJTmaaJObG";
    static final String SCRYPT = "$scrypt$ln=16,r=8,p=1$aM15713r3Xsvxbi31lqr1Q$nFNh2CVHVjNldFVKDHDlm4CbdRSCdEBsjjJxD+iCs5E";
    static final String PBKDF2 = "$pbkdf2-sha256$29000$N2bMOSdESAkhxJTS2nuPEQ$a7zUt1ZBbR8Y7CslRjmoeD5mEGgibUt/hIjZyq.tTMk";
    static final String SHA512 = "$6$saltsalt$svn8UoSVapNtMuq1ukKS4tPQd8iKwSMHWjl/O817G3uBnIFNjnQJuesI68u4OTLiBFdcbYEdFCoEOfaS35inz1";
    static final String SHA256 = "$5$rounds=5000$saltstring$5B8vYYiY.CVt1RlTTf8KbXBH3hsxY/GNooZF8wCrxaq";

    @ParameterizedTest(name = "{1}")
    @CsvSource(delimiter = '|', value = {
            "$2b$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW | bcrypt-2b",
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy | bcrypt-2a",
            "$2y$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy | bcrypt-2y",
            "$argon2id$v=19$m=65536,t=3,p=4$c29tZXNhbHQ$RdescudvJCsgt3ub+b+dWRWJTmaaJObG | argon2id",
            "$argon2i$v=19$m=4096,t=3,p=1$c29tZXNhbHQ$iWh06vD8Fy27wf9npn6FXWiCX4K6pW6Ue1Bnzz07Z8A | argon2i",
            "$argon2d$m=4096,t=3,p=1$c29tZXNhbHQ$iWh06vD8Fy27wf9npn6FXWiCX4K6pW6Ue1Bnzz07Z8A | argon2d",
            "$scrypt$ln=16,r=8,p=1$aM15713r3Xsvxbi31lqr1Q$nFNh2CVHVjNldFVKDHDlm4CbdRSCdEBsjjJxD+iCs5E | scrypt",
            "$pbkdf2-sha256$29000$N2bMOSdESAkhxJTS2nuPEQ$a7zUt1ZBbR8Y7CslRjmoeD5mEGgibUt/hIjZyq.tTMk | pbkdf2",
            "$6$saltsalt$svn8UoSVapNtMuq1ukKS4tPQd8iKwSMHWjl/O817G3uBnIFNjnQJuesI68u4OTLiBFdcbYEdFCoEOfaS35inz1 | sha512-crypt",
            "$5$rounds=5000$saltstring$5B8vYYiY.CVt1RlTTf8KbXBH3hsxY/GNooZF8wCrxaq | sha256-crypt",
    })
    void acceptsRecognisedFormats(String hash, String algorithm) {
        HashedPassword h = HashedPassword.parse("  " + hash + "  ");
        assertEquals(hash, h.hash());
        assertEquals(algorithm, h.algorithm());
        assertTrue(HashedPassword.isValid(hash));
        assertTrue(HashedPassword.isValidHash(hash));
        assertEquals("HashedPassword[" + algorithm + "]", h.toString());
        assertFalse(h.toString().contains(hash));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "hunter2", "correct horse battery staple", "P@ssw0rd!",
            "5f4dcc3b5aa765d61d8327deb882cf99", "cGFzc3dvcmRwYXNzd29yZHBhc3N3b3JkcGFzcw==",
            "$2b$12$tooshort", "$argon2id$nothing", "$1$salt$hash", "$foo$bar", "$5$salt$short", "$scrypt$"})
    void rejectsPlainPasswordsAndUnknownFormats(String text) {
        assertThrows(ValueObjectException.class, () -> new HashedPassword(text));
        assertThrows(ValueObjectException.class, () -> HashedPassword.parse(text));
        assertThrows(ValueObjectException.class, () -> HashedPassword.of(text));
        assertTrue(HashedPassword.tryParse(text).isEmpty());
        assertFalse(HashedPassword.isValid(text));
        assertFalse(HashedPassword.isValidHash(text));
    }

    @Test
    void errorMessagesNeverEchoThePassword() {
        ValueObjectException e = assertThrows(ValueObjectException.class, () -> HashedPassword.of("hunter2-secret"));
        assertFalse(e.getMessage().contains("hunter2"));
    }

    @Test
    void kindsAndMasking() {
        assertTrue(HashedPassword.of(BCRYPT).isBcrypt());
        assertTrue(HashedPassword.of(ARGON2).isArgon2());
        assertTrue(HashedPassword.of(SCRYPT).isScrypt());
        assertTrue(HashedPassword.of(PBKDF2).isPbkdf2());
        assertFalse(HashedPassword.of(SHA512).isBcrypt());
        assertEquals("$2b$12$...(53 chars)", HashedPassword.of(BCRYPT).masked());
        assertEquals(BCRYPT.length(), HashedPassword.of(BCRYPT).length());
        assertEquals(HashedPassword.of(BCRYPT), HashedPassword.of(" " + BCRYPT));
        assertEquals(HashedPassword.of(BCRYPT).hashCode(), HashedPassword.of(BCRYPT).hashCode());
        assertNotEquals(HashedPassword.of(BCRYPT), HashedPassword.of(SHA256));
    }
}
