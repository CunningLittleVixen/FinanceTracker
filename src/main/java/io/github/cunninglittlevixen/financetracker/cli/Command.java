package io.github.cunninglittlevixen.financetracker.cli;

public interface Command {

    String name();

    String usage();

    String description();

    String example();

    default boolean requiresLogin() {
        return true;
    }

    default boolean changesData() {
        return false;
    }

    void execute(CommandLine line, Session session, Terminal terminal);
}
