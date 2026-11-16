package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.CategoryNotFoundException;
import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandLoopTest {

    private final RecordingWalletRepository repository = new RecordingWalletRepository();
    private final Session session = new Session();

    private String run(String input, Command... extraCommands) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Terminal terminal = Terminal.forStreams(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), bytes);
        CommandRegistry registry = new CommandRegistry();
        registry.register(new ExitCommand());
        registry.register(new HelpCommand(registry));
        for (Command command : extraCommands) {
            registry.register(command);
        }
        new CommandLoop(terminal, session, registry, repository).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void logIn() {
        session.login(new User("anna", "h"), new Wallet("anna"));
    }

    @Test
    void exitStopsLoopAndSaysGoodbye() {
        String out = run("exit\n");

        assertTrue(out.contains("До свидания!"));
        assertTrue(out.contains("> "));
    }

    @Test
    void blankLinesAreIgnored() {
        assertFalse(run("\n   \nexit\n").contains("Неизвестная"));
    }

    @Test
    void unknownCommandSuggestsHelp() {
        String out = run("foo\nexit\n");

        assertTrue(out.contains("Неизвестная команда «foo»"));
        assertTrue(out.contains("help"));
    }

    @Test
    void endOfInputEndsLoopGracefully() {
        assertTrue(run("").contains("До свидания!"));
    }

    @Test
    void commandRequiringLoginIsBlockedWhenLoggedOut() {
        TestCommand secret = new TestCommand("secret", true, false, line -> { });

        String out = run("secret\nexit\n", secret);

        assertTrue(secret.calls.isEmpty());
        assertTrue(out.contains("login"));
    }

    @Test
    void validationErrorIsShownAndLoopContinues() {
        TestCommand bad = new TestCommand("bad", false, false, line -> {
            throw new ValidationException("Плохая сумма");
        });
        TestCommand ok = TestCommand.simple("ok");

        String out = run("bad\nok\nexit\n", bad, ok);

        assertTrue(out.contains("Ошибка: Плохая сумма"));
        assertEquals(1, ok.calls.size());
    }

    @Test
    void missingCategoryShowsHintWithCommand() {
        TestCommand command = new TestCommand("spend", false, false, line -> {
            throw new CategoryNotFoundException("Такси", "Категория «Такси» не найдена.");
        });

        String out = run("spend\nexit\n", command);

        assertTrue(out.contains("add-category \"Такси\""));
    }

    @Test
    void unexpectedErrorDoesNotCrashLoop() {
        TestCommand command = new TestCommand("boom", false, false, line -> {
            throw new IllegalStateException("boom");
        });

        String out = run("boom\nexit\n", command);

        assertTrue(out.contains("Непредвиденная ошибка"));
        assertTrue(out.contains("boom"));
        assertTrue(out.contains("До свидания!"));
    }

    @Test
    void storageErrorIsShownWithPrefix() {
        TestCommand command = new TestCommand("disk", false, false, line -> {
            throw new StorageException("диск");
        });

        assertTrue(run("disk\nexit\n", command).contains("Ошибка хранилища: диск"));
    }

    @Test
    void quotedArgumentsReachCommandAsOneValue() {
        TestCommand echo = TestCommand.simple("echo");

        run("echo \"Коммунальные услуги\" 5\nexit\n", echo);

        assertEquals(List.of("Коммунальные услуги", "5"), echo.calls.get(0).args());
    }

    @Test
    void commandThatChangesDataSavesWalletAndExitSavesAgain() {
        logIn();
        TestCommand change = new TestCommand("change", true, true, line -> { });

        run("change\nexit\n", change);

        assertEquals(2, repository.saved.size());
    }

    @Test
    void readOnlyCommandDoesNotSaveBeforeExit() {
        logIn();
        TestCommand view = new TestCommand("view", true, false, line -> { });

        run("view\nexit\n", view);

        assertEquals(1, repository.saved.size());
    }

    @Test
    void exitSavesLoggedInWallet() {
        logIn();

        run("exit\n");

        assertEquals(1, repository.saved.size());
    }

    @Test
    void exitWhenLoggedOutSavesNothing() {
        run("exit\n");

        assertTrue(repository.saved.isEmpty());
    }

    @Test
    void failedSaveIsReportedAndLoopContinues() {
        logIn();
        repository.failure = new StorageException("диск полон");
        TestCommand change = new TestCommand("change", true, true, line -> { });

        String out = run("change\nexit\n", change);

        assertTrue(out.contains("Ошибка хранилища: диск полон"));
        assertEquals(1, change.calls.size());
        assertTrue(out.contains("До свидания!"));
    }

    @Test
    void promptShowsLoginWhenLoggedIn() {
        logIn();

        assertTrue(run("exit\n").contains("anna> "));
    }
}
