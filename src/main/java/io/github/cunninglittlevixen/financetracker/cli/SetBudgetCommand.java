package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Money;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.service.BudgetService;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public class SetBudgetCommand implements Command {

    private final BudgetService budgets;

    public SetBudgetCommand(BudgetService budgets) {
        this.budgets = budgets;
    }

    @Override
    public String name() {
        return "set-budget";
    }

    @Override
    public String usage() {
        return "set-budget <категория> <лимит>";
    }

    @Override
    public String description() {
        return "Установить или изменить бюджет категории";
    }

    @Override
    public String example() {
        return "set-budget Еда 4000";
    }

    @Override
    public boolean changesData() {
        return true;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(2, 2, Set.of(), usage());
        Wallet wallet = session.wallet();
        String category = line.args().get(0);
        BigDecimal limit = Money.parse(line.args().get(1));
        Optional<Budget> previous = wallet.getBudget(category);

        Budget budget = budgets.setBudget(wallet, category, limit);

        String newLimit = MoneyFormatter.format(budget.limit());
        if (previous.isPresent()) {
            terminal.println("Бюджет категории «" + budget.category() + "» изменён: было "
                    + MoneyFormatter.format(previous.get().limit()) + ", стало " + newLimit + ".");
        } else {
            terminal.println("Бюджет категории «" + budget.category() + "» установлен: "
                    + newLimit + ".");
        }
    }
}
