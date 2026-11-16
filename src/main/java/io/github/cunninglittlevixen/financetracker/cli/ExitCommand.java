package io.github.cunninglittlevixen.financetracker.cli;

import java.util.Set;

public class ExitCommand implements Command {

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public String usage() {
        return "exit";
    }

    @Override
    public String description() {
        return "Сохранить данные и выйти из программы";
    }

    @Override
    public String example() {
        return "exit";
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(0, 0, Set.of(), usage());
        session.stop();
    }
}
