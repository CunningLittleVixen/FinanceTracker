package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Money;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.service.NotificationService;
import io.github.cunninglittlevixen.financetracker.core.service.OperationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class AddOperationCommand implements Command {

    private final OperationType type;
    private final OperationService operations;
    private final NotificationService notifications;

    public AddOperationCommand(
            OperationType type, OperationService operations, NotificationService notifications) {
        this.type = type;
        this.operations = operations;
        this.notifications = notifications;
    }

    @Override
    public String name() {
        return isIncome() ? "add-income" : "add-expense";
    }

    @Override
    public String usage() {
        return name() + " <категория> <сумма> [описание] [--date ГГГГ-ММ-ДД]";
    }

    @Override
    public String description() {
        return isIncome()
                ? "Добавить доход (дата по умолчанию сегодня)"
                : "Добавить расход (дата по умолчанию сегодня)";
    }

    @Override
    public String example() {
        return isIncome()
                ? "add-income Зарплата 20000"
                : "add-expense \"Коммунальные услуги\" 3000 \"за ноябрь\" --date 2026-11-10";
    }

    @Override
    public boolean changesData() {
        return true;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(2, 3, Set.of("date"), usage());
        Wallet wallet = session.wallet();
        String category = line.args().get(0);
        BigDecimal amount = Money.parse(line.args().get(1));
        String description = line.args().size() > 2 ? line.args().get(2) : "";
        LocalDate date = line.optionDate("date").orElseGet(LocalDate::now);

        Operation operation = isIncome()
                ? operations.addIncome(wallet, category, amount, date, description)
                : operations.addExpense(wallet, category, amount, date, description);

        terminal.println((isIncome() ? "Доход" : "Расход") + " добавлен: " + operation.category()
                + ", " + MoneyFormatter.format(operation.amount()) + ", " + operation.date()
                + ". Баланс: " + MoneyFormatter.format(wallet.getBalance()) + ".");
        NotificationPrinter.show(terminal, notifications.afterOperation(wallet, operation));
    }

    private boolean isIncome() {
        return type == OperationType.INCOME;
    }
}
