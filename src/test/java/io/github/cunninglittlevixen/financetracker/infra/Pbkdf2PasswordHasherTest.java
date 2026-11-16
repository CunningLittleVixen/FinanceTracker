package io.github.cunninglittlevixen.financetracker.infra;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class Pbkdf2PasswordHasherTest {

    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher(1000);

    @Test
    void correctPasswordVerifies() {
        assertTrue(hasher.verify("secret1", hasher.hash("secret1")));
    }

    @Test
    void wrongPasswordFails() {
        assertFalse(hasher.verify("secret2", hasher.hash("secret1")));
    }

    @Test
    void samePasswordGivesDifferentHashes() {
        assertNotEquals(hasher.hash("secret1"), hasher.hash("secret1"));
    }

    @Test
    void hashContainsParametersButNotPassword() {
        String hash = hasher.hash("secret1");

        assertTrue(hash.startsWith("pbkdf2$1000$"));
        assertFalse(hash.contains("secret1"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc", "pbkdf2$x$y$z", "pbkdf2$1000$!!!$!!!", "pbkdf2$0$AAAA$AAAA"})
    void corruptedHashDoesNotVerify(String stored) {
        assertFalse(hasher.verify("secret1", stored));
    }

    @Test
    void hashFromAnotherIterationCountStillVerifies() {
        String hash = hasher.hash("secret1");

        assertTrue(new Pbkdf2PasswordHasher(2000).verify("secret1", hash));
    }

    @Test
    void rejectsNonPositiveIterations() {
        assertThrows(IllegalArgumentException.class, () -> new Pbkdf2PasswordHasher(0));
    }
}
