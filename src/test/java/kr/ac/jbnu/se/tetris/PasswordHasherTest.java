package kr.ac.jbnu.se.tetris;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void hashedPasswordMatchesOnlyTheSamePassword() {
        String stored = PasswordHasher.hash("Passw0rd!");
        assertTrue(PasswordHasher.isHashed(stored));
        assertTrue(PasswordHasher.matches("Passw0rd!", stored));
        assertFalse(PasswordHasher.matches("passw0rd!", stored));
    }

    @Test
    void sameInputGivesDifferentHashesBecauseOfSalt() {
        assertNotEquals(PasswordHasher.hash("12345678"), PasswordHasher.hash("12345678"));
    }

    @Test
    void hashContainsNoCommaSoUsersFileFormatSurvives() {
        assertFalse(PasswordHasher.hash("a,b,c,d,e,f,g,h").contains(","));
    }

    @Test
    void legacyPlainTextAccountsStillLogIn() {
        assertFalse(PasswordHasher.isHashed("12345678"));
        assertTrue(PasswordHasher.matches("12345678", "12345678"));
        assertFalse(PasswordHasher.matches("other", "12345678"));
    }

    @Test
    void malformedHashDoesNotMatchOrThrow() {
        assertFalse(PasswordHasher.matches("x", "pbkdf2$abc"));
        assertFalse(PasswordHasher.matches("x", "pbkdf2$1$!!$!!"));
        assertFalse(PasswordHasher.matches(null, "pbkdf2$1$a$b"));
    }

    @Test
    void rejectsNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
    }
}
