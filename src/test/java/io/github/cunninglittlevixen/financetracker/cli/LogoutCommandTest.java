package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogoutCommandTest {

    private final RecordingWalletRepository wallets = new RecordingWalletRepository();
    private final Session session = new Session();
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final Terminal terminal =
            Terminal.forStreams(new ByteArrayInputStream(new byte[0]), bytes);
    private final LogoutCommand command = new LogoutCommand(wallets);

    @BeforeEach
    void setUp() {
        session.login(new User("anna", "h"), new Wallet("anna"));
    }

    @Test
    void logoutSavesWalletAndClosesSession() {
        command.execute(CommandLine.parse("logout"), session, terminal);

        assertEquals(1, wallets.saved.size());
        assertFalse(session.isLoggedIn());
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Вы вышли"));
    }

    @Test
    void failedSaveKeepsSessionOpen() {
        wallets.failure = new StorageException("диск полон");

        assertThrows(StorageException.class,
                () -> command.execute(CommandLine.parse("logout"), session, terminal));

        assertTrue(session.isLoggedIn());
    }

    @Test
    void logoutRequiresLogin() {
        assertTrue(command.requiresLogin());
    }
}
