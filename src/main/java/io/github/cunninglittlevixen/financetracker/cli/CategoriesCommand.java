package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CategoriesCommand implements Command {

    @Override
    public String name() {
        return "categories";
    }

    @Override
    public String usage() {
        return "categories";
    }

    @Override
    public String description() {
        return "Показать категории и их бюджеты";
    }

    @Override
    public String example() {
        return "categories";
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(0, 0, Set.of(), usage());
        Wallet wallet = session.wallet();
        if (wallet.getCategories().isEmpty()) {
            terminal.println("Категорий пока нет. Создайте первую: add-category Еда.");
            return;
        }
        List<List<String>> rows = new ArrayList<>();
        for (Category category : wallet.getCategories()) {
            String budget = wallet.getBudget(category.name())
                    .map(b -> MoneyFormatter.format(b.limit()))
                    .orElse("-");
            rows.add(List.of(category.name(), budget));
        }
        terminal.println(TableFormatter.format(List.of("Категория", "Бюджет"), rows, Set.of(1)));
    }
}
