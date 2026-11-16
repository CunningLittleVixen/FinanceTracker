package io.github.cunninglittlevixen.financetracker.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class TestCommand implements Command {

    final List<CommandLine> calls = new ArrayList<>();

    private final String name;
    private final boolean requiresLogin;
    private final boolean changesData;
    private final Consumer<CommandLine> action;

    TestCommand(String name, boolean requiresLogin, boolean changesData,
                Consumer<CommandLine> action) {
        this.name = name;
        this.requiresLogin = requiresLogin;
        this.changesData = changesData;
        this.action = action;
    }

    static TestCommand simple(String name) {
        return new TestCommand(name, false, false, line -> { });
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String usage() {
        return name + " <арг>";
    }

    @Override
    public String description() {
        return "Тестовая команда";
    }

    @Override
    public String example() {
        return name + " x";
    }

    @Override
    public boolean requiresLogin() {
        return requiresLogin;
    }

    @Override
    public boolean changesData() {
        return changesData;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        calls.add(line);
        action.accept(line);
    }
}
