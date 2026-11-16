package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.service.CategoryService;
import java.util.Set;

public class AddCategoryCommand implements Command {

    private final CategoryService categories;

    public AddCategoryCommand(CategoryService categories) {
        this.categories = categories;
    }

    @Override
    public String name() {
        return "add-category";
    }

    @Override
    public String usage() {
        return "add-category <название>";
    }

    @Override
    public String description() {
        return "Создать категорию (название из нескольких слов берите в кавычки)";
    }

    @Override
    public String example() {
        return "add-category \"Коммунальные услуги\"";
    }

    @Override
    public boolean changesData() {
        return true;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(1, 1, Set.of(), usage());
        Category category = categories.addCategory(session.wallet(), line.args().get(0));
        terminal.println("Категория «" + category.name() + "» создана.");
    }
}
