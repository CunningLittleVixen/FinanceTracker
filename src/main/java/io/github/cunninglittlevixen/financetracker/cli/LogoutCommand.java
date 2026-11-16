package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;
import java.util.Set;

public class LogoutCommand implements Command {

    private final WalletRepository wallets;

    public LogoutCommand(WalletRepository wallets) {
        this.wallets = wallets;
    }

    @Override
    public String name() {
        return "logout";
    }

    @Override
    public String usage() {
        return "logout";
    }

    @Override
    public String description() {
        return "Сохранить данные и выйти из учётной записи";
    }

    @Override
    public String example() {
        return "logout";
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(0, 0, Set.of(), usage());
        wallets.save(session.wallet());
        String login = session.user().login();
        session.logout();
        terminal.println("Вы вышли из системы. До встречи, " + login + "!");
    }
}
