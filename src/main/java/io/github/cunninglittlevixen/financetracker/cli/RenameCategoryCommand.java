package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.service.CategoryService;
import java.util.Set;

public class RenameCategoryCommand implements Command {

    private final CategoryService categories;

    public RenameCategoryCommand(CategoryService categories) {
        this.categories = categories;
    }

    @Override
    public String name() {
        return "rename-category";
    }

    @Override
    public String usage() {
        return "rename-category <старое название> <новое название>";
    }

    @Override
    public String description() {
        return "Переименовать категорию вместе с её операциями и бюджетом";
    }

    @Override
    public String example() {
        return "rename-category Еда Продукты";
    }

    @Override
    public boolean changesData() {
        return true;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(2, 2, Set.of(), usage());
        String oldName = line.args().get(0);
        String newName = line.args().get(1).trim();
        categories.renameCategory(session.wallet(), oldName, newName);
        terminal.println("Категория «" + oldName.trim() + "» переименована в «" + newName
                + "». Операции и бюджет перенесены.");
    }
}
