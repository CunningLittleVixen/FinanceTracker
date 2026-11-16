package io.github.cunninglittlevixen.financetracker.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandRegistry {

    private final List<Command> commands = new ArrayList<>();

    public void register(Command command) {
        if (find(command.name()).isPresent()) {
            throw new IllegalArgumentException("Команда уже зарегистрирована: " + command.name());
        }
        commands.add(command);
    }

    public Optional<Command> find(String name) {
        return commands.stream().filter(command -> command.name().equals(name)).findFirst();
    }

    public List<Command> all() {
        return List.copyOf(commands);
    }
}
