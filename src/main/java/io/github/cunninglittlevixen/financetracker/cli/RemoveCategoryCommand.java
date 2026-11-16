package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.service.CategoryService;
import java.util.Set;

public class RemoveCategoryCommand implements Command {

    private final CategoryService categories;

    public RemoveCategoryCommand(CategoryService categories) {
        this.categories = categories;
    }

    @Override
    public String name() {
        return "remove-category";
    }

    @Override
    public String usage() {
        return "remove-category <название>";
    }

    @Override
    public String description() {
        return "Удалить категорию без операций (её бюджет удаляется вместе с ней)";
    }

    @Override
    public String example() {
        return "remove-category Такси";
    }

    @Override
    public boolean changesData() {
        return true;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(1, 1, Set.of(), usage());
        String name = line.args().get(0);
        categories.removeCategory(session.wallet(), name);
        terminal.println("Категория «" + name.trim() + "» удалена.");
    }
}
