package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.service.BudgetService;
import java.util.Set;

public class RemoveBudgetCommand implements Command {

    private final BudgetService budgets;

    public RemoveBudgetCommand(BudgetService budgets) {
        this.budgets = budgets;
    }

    @Override
    public String name() {
        return "remove-budget";
    }

    @Override
    public String usage() {
        return "remove-budget <категория>";
    }

    @Override
    public String description() {
        return "Удалить бюджет категории (сама категория остаётся)";
    }

    @Override
    public String example() {
        return "remove-budget Еда";
    }

    @Override
    public boolean changesData() {
        return true;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(1, 1, Set.of(), usage());
        String category = line.args().get(0);
        budgets.removeBudget(session.wallet(), category);
        terminal.println("Бюджет категории «" + category.trim() + "» удалён.");
    }
}
