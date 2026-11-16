package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.infra.InMemoryUserRepository;
import io.github.cunninglittlevixen.financetracker.infra.Pbkdf2PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AuthServiceTest {

    private InMemoryUserRepository repository;
    private AuthService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        service = new AuthService(repository, new Pbkdf2PasswordHasher(1000));
    }

    @Test
    void registerStoresUserWithHashedPassword() {
        User user = service.register("anna", "secret1");

        assertEquals(user, repository.findByLogin("anna").orElseThrow());
        assertTrue(user.passwordHash().startsWith("pbkdf2$"));
        assertFalse(user.passwordHash().contains("secret1"));
    }

    @Test
    void registerTrimsAndLowercasesLogin() {
        assertEquals("anna", service.register("  Anna ", "secret1").login());
    }

    @Test
    void duplicateLoginIgnoringCaseFails() {
        service.register("anna", "secret1");

        ValidationException e = assertThrows(ValidationException.class,
                () -> service.register("ANNA", "other-pass"));

        assertTrue(e.getMessage().contains("занят"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ab", "anna!", "анна", "has space"})
    void invalidLoginFails(String login) {
        assertThrows(ValidationException.class, () -> service.register(login, "secret1"));
    }

    @Test
    void tooLongLoginFails() {
        assertThrows(ValidationException.class,
                () -> service.register("a".repeat(21), "secret1"));
    }

    @Test
    void boundaryLoginLengthsAreAccepted() {
        assertDoesNotThrow(() -> service.register("abc", "secret1"));
        assertDoesNotThrow(() -> service.register("a".repeat(20), "secret1"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345"})
    void shortPasswordFails(String password) {
        assertThrows(ValidationException.class, () -> service.register("anna", password));
    }

    @Test
    void loginWithCorrectPasswordIgnoresLoginCase() {
        service.register("anna", "secret1");

        assertEquals("anna", service.login("anna", "secret1").login());
        assertEquals("anna", service.login("ANNA", "secret1").login());
    }

    @Test
    void wrongPasswordAndUnknownLoginGiveSameMessage() {
        service.register("anna", "secret1");

        ValidationException wrongPassword = assertThrows(ValidationException.class,
                () -> service.login("anna", "wrong-pass"));
        ValidationException unknownLogin = assertThrows(ValidationException.class,
                () -> service.login("nobody", "secret1"));

        assertEquals("Неверный логин или пароль.", wrongPassword.getMessage());
        assertEquals(wrongPassword.getMessage(), unknownLogin.getMessage());
    }

    @Test
    void blankLoginAttemptFailsWithSameMessage() {
        assertThrows(ValidationException.class, () -> service.login(null, "secret1"));
        assertThrows(ValidationException.class, () -> service.login(" ", "secret1"));
    }

    @Test
    void severalUsersLogInIndependently() {
        service.register("anna", "secret1");
        service.register("boris", "secret2");

        assertEquals("anna", service.login("anna", "secret1").login());
        assertEquals("boris", service.login("boris", "secret2").login());
        assertThrows(ValidationException.class, () -> service.login("boris", "secret1"));
    }
}
