package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.exception.CategoryNotFoundException;
import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;
import java.util.Optional;

public class CommandLoop {

    private final Terminal terminal;
    private final Session session;
    private final CommandRegistry registry;
    private final WalletRepository wallets;

    public CommandLoop(
            Terminal terminal, Session session, CommandRegistry registry, WalletRepository wallets) {
        this.terminal = terminal;
        this.session = session;
        this.registry = registry;
        this.wallets = wallets;
    }

    public void run() {
        terminal.println("FinanceTracker: учёт личных финансов. Введите help, чтобы увидеть команды.");
        try {
            while (session.isRunning()) {
                String text = terminal.readLine(prompt());
                if (text == null) {
                    break;
                }
                handle(text);
            }
        } catch (InputEndedException e) {
            // ввод закончился посреди вопроса, выходим как при обычном конце ввода
        }
        shutdown();
    }

    private void handle(String text) {
        try {
            CommandLine line = CommandLine.parse(text);
            if (line.isEmpty()) {
                return;
            }
            Optional<Command> found = registry.find(line.name());
            if (found.isEmpty()) {
                terminal.println("Неизвестная команда «" + line.name()
                        + "». Введите help, чтобы увидеть список.");
                return;
            }
            Command command = found.get();
            if (command.requiresLogin() && !session.isLoggedIn()) {
                terminal.println("Эта команда доступна после входа. Выполните login или register.");
                return;
            }
            command.execute(line, session, terminal);
            if (command.changesData() && session.isLoggedIn()) {
                wallets.save(session.wallet());
            }
        } catch (InputEndedException e) {
            throw e;
        } catch (CategoryNotFoundException e) {
            terminal.println("Ошибка: " + e.getMessage());
            terminal.println("Создать категорию можно командой: add-category \""
                    + e.getCategoryName() + "\"");
        } catch (ValidationException e) {
            terminal.println("Ошибка: " + e.getMessage());
        } catch (StorageException e) {
            terminal.println("Ошибка хранилища: " + e.getMessage());
        } catch (RuntimeException e) {
            terminal.println("Непредвиденная ошибка: " + e);
        }
    }

    private String prompt() {
        return session.isLoggedIn() ? session.user().login() + "> " : "> ";
    }

    private void shutdown() {
        try {
            if (session.isLoggedIn()) {
                wallets.save(session.wallet());
            }
        } catch (StorageException e) {
            terminal.println("Ошибка хранилища: " + e.getMessage());
        }
        terminal.println("До свидания!");
    }
}
