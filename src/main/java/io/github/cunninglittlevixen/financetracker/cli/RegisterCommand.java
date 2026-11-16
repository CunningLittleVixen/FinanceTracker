package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;
import io.github.cunninglittlevixen.financetracker.core.service.AuthService;
import java.util.Set;

public class RegisterCommand implements Command {

    private final AuthService auth;
    private final WalletRepository wallets;

    public RegisterCommand(AuthService auth, WalletRepository wallets) {
        this.auth = auth;
        this.wallets = wallets;
    }

    @Override
    public String name() {
        return "register";
    }

    @Override
    public String usage() {
        return "register";
    }

    @Override
    public String description() {
        return "Создать нового пользователя и сразу войти";
    }

    @Override
    public String example() {
        return "register";
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(0, 0, Set.of(), usage());
        session.requireLoggedOut();
        String login = terminal.ask("Логин: ");
        String password = terminal.ask("Пароль: ");
        User user = auth.register(login, password);
        SessionSupport.open(session, user, wallets);
        terminal.println("Регистрация прошла успешно. Вы вошли как " + user.login() + ".");
        terminal.println(
                "Начните с создания категорий, например: add-category Еда. Список команд: help.");
    }
}
