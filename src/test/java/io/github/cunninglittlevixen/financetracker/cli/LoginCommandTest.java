package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.service.AuthService;
import io.github.cunninglittlevixen.financetracker.core.service.NotificationService;
import io.github.cunninglittlevixen.financetracker.core.service.StatisticsService;
import io.github.cunninglittlevixen.financetracker.infra.InMemoryUserRepository;
import io.github.cunninglittlevixen.financetracker.infra.Pbkdf2PasswordHasher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginCommandTest {

    private final RecordingWalletRepository wallets = new RecordingWalletRepository();
    private final Session session = new Session();
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private LoginCommand command;

    @BeforeEach
    void setUp() {
        AuthService auth = new AuthService(new InMemoryUserRepository(), new Pbkdf2PasswordHasher(1000));
        auth.register("anna", "secret1");
        command = new LoginCommand(auth, wallets, new NotificationService(new StatisticsService()));
    }

    private void login(String input) {
        Terminal terminal = Terminal.forStreams(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), bytes);
        command.execute(CommandLine.parse("login"), session, terminal);
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private static Operation op(OperationType type, String category, String amount) {
        return new Operation(type, category, new BigDecimal(amount), LocalDate.of(2020, 1, 1), null, null);
    }

    @Test
    void loginLoadsStoredWalletAndShowsBalance() {
        Wallet stored = new Wallet("anna");
        stored.addCategory(new Category("Зарплата"));
        stored.addOperation(op(OperationType.INCOME, "Зарплата", "1000"));
        wallets.stored.put("anna", stored);

        login("ANNA\nsecret1\n");

        assertSame(stored, session.wallet());
        assertEquals("anna", session.user().login());
        assertTrue(output().contains("1,000.00"));
    }

    @Test
    void loginWithoutWalletFileStartsEmptyWallet() {
        login("anna\nsecret1\n");

        assertTrue(session.wallet().getOperations().isEmpty());
        assertTrue(output().contains("add-category"));
    }

    @Test
    void wrongPasswordKeepsSessionClosed() {
        assertThrows(ValidationException.class, () -> login("anna\nwrong-pass\n"));

        assertFalse(session.isLoggedIn());
    }

    @Test
    void loginShowsAlertsAboutCurrentState() {
        Wallet stored = new Wallet("anna");
        stored.addCategory(new Category("Еда"));
        stored.addCategory(new Category("Зарплата"));
        stored.setBudget(new Budget("Еда", new BigDecimal("1000")));
        stored.addOperation(op(OperationType.INCOME, "Зарплата", "10000"));
        stored.addOperation(op(OperationType.EXPENSE, "Еда", "5000"));
        wallets.stored.put("anna", stored);

        login("anna\nsecret1\n");

        assertTrue(output().contains("Оповещения:"));
        assertTrue(output().contains("Превышен бюджет"));
    }

    @Test
    void loginWhenAlreadyLoggedInFails() {
        session.login(new User("boris", "h"), new Wallet("boris"));

        assertThrows(ValidationException.class, () -> login("anna\nsecret1\n"));
        assertEquals("boris", session.user().login());
    }

    @Test
    void corruptedWalletFileKeepsSessionClosed() {
        wallets.loadFailure = new StorageException("файл повреждён");

        assertThrows(StorageException.class, () -> login("anna\nsecret1\n"));

        assertFalse(session.isLoggedIn());
    }
}
