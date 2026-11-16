package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.service.AuthService;
import io.github.cunninglittlevixen.financetracker.infra.InMemoryUserRepository;
import io.github.cunninglittlevixen.financetracker.infra.Pbkdf2PasswordHasher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class RegisterCommandTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final RecordingWalletRepository wallets = new RecordingWalletRepository();
    private final Session session = new Session();
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final RegisterCommand command = new RegisterCommand(
            new AuthService(users, new Pbkdf2PasswordHasher(1000)), wallets);

    private Terminal terminal(String input) {
        return Terminal.forStreams(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), bytes);
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void register(String input) {
        command.execute(CommandLine.parse("register"), session, terminal(input));
    }

    @Test
    void registerCreatesUserAndLogsIn() {
        register("Anna\nsecret1\n");

        assertTrue(session.isLoggedIn());
        assertEquals("anna", session.user().login());
        assertEquals("anna", session.wallet().getOwner());
        assertTrue(users.findByLogin("anna").isPresent());
        assertTrue(output().contains("Вы вошли как anna"));
        assertTrue(output().contains("add-category"));
    }

    @Test
    void invalidLoginIsRejectedAndSessionStaysClosed() {
        assertThrows(ValidationException.class, () -> register("ab\nsecret1\n"));

        assertFalse(session.isLoggedIn());
    }

    @Test
    void registerWhenAlreadyLoggedInFailsWithoutAskingQuestions() {
        session.login(new User("boris", "h"), new Wallet("boris"));

        ValidationException e = assertThrows(ValidationException.class, () -> register("anna\nsecret1\n"));

        assertTrue(e.getMessage().contains("logout"));
        assertFalse(output().contains("Логин:"));
    }

    @Test
    void registerRejectsExtraArguments() {
        assertThrows(ValidationException.class, () -> command.execute(
                CommandLine.parse("register extra"), session, terminal("")));
    }

    @Test
    void inputEndingBeforePasswordRegistersNobody() {
        assertThrows(InputEndedException.class, () -> register("anna\n"));

        assertFalse(session.isLoggedIn());
        assertTrue(users.findByLogin("anna").isEmpty());
    }
}
