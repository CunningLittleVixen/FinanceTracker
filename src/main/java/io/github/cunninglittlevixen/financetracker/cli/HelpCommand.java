package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.util.List;
import java.util.Set;

public class HelpCommand implements Command {

    private final CommandRegistry registry;

    public HelpCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String usage() {
        return "help [команда]";
    }

    @Override
    public String description() {
        return "Показать список команд или справку по одной команде";
    }

    @Override
    public String example() {
        return "help add-expense";
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(0, 1, Set.of(), usage());
        if (line.args().isEmpty()) {
            printList(session, terminal);
        } else {
            printDetails(line.args().get(0).toLowerCase(), terminal);
        }
    }

    private void printList(Session session, Terminal terminal) {
        List<List<String>> rows = registry.all().stream()
                .filter(command -> !command.requiresLogin() || session.isLoggedIn())
                .map(command -> List.of(command.usage(), command.description()))
                .toList();
        terminal.println(TableFormatter.format(List.of("Команда", "Описание"), rows, Set.of()));
        terminal.println("");
        terminal.println("Названия из нескольких слов берите в кавычки, например: "
                + "add-category \"Коммунальные услуги\".");
        terminal.println("Подробности и пример: help <команда>.");
    }

    private void printDetails(String commandName, Terminal terminal) {
        Command command = registry.find(commandName).orElseThrow(() -> new ValidationException(
                "Неизвестная команда «" + commandName + "». Введите help, чтобы увидеть список."));
        terminal.println("Использование: " + command.usage());
        terminal.println("Описание: " + command.description());
        terminal.println("Пример: " + command.example());
    }
}
