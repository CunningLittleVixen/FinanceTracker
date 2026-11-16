package io.github.cunninglittlevixen.financetracker.core.service;

import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal WARNING_PERCENT = BigDecimal.valueOf(80);

    private final StatisticsService statistics;

    public NotificationService(StatisticsService statistics) {
        this.statistics = statistics;
    }

    public List<Notification> afterOperation(Wallet wallet, Operation operation) {
        Summary summary = statistics.summarize(wallet, OperationFilter.all());
        List<Notification> result = new ArrayList<>();
        if (operation.type() == OperationType.EXPENSE) {
            String key = new Category(operation.category()).key();
            for (BudgetStatus status : summary.budgets()) {
                if (new Category(status.category()).key().equals(key)) {
                    budgetNotification(status).ifPresent(result::add);
                }
            }
        }
        result.addAll(balanceNotifications(wallet, summary));
        return result;
    }

    public List<Notification> currentState(Wallet wallet) {
        Summary summary = statistics.summarize(wallet, OperationFilter.all());
        List<Notification> result = new ArrayList<>();
        for (BudgetStatus status : summary.budgets()) {
            budgetNotification(status).ifPresent(result::add);
        }
        result.addAll(balanceNotifications(wallet, summary));
        return result;
    }

    private Optional<Notification> budgetNotification(BudgetStatus status) {
        if (status.spent().compareTo(status.limit()) > 0) {
            return Optional.of(new Notification(NotificationType.BUDGET_EXCEEDED,
                    "Превышен бюджет «" + status.category() + "»: потрачено "
                            + status.spent().toPlainString() + " из "
                            + status.limit().toPlainString() + ", перерасход "
                            + status.remaining().negate().toPlainString() + "."));
        }
        BigDecimal spentScaled = status.spent().multiply(HUNDRED);
        if (spentScaled.compareTo(status.limit().multiply(WARNING_PERCENT)) >= 0) {
            String percent = spentScaled.divide(status.limit(), 0, RoundingMode.DOWN).toPlainString();
            return Optional.of(new Notification(NotificationType.BUDGET_NEAR_LIMIT,
                    "Внимание: использовано " + percent + "% бюджета «" + status.category()
                            + "». Осталось " + status.remaining().toPlainString() + "."));
        }
        return Optional.empty();
    }

    private List<Notification> balanceNotifications(Wallet wallet, Summary summary) {
        List<Notification> result = new ArrayList<>();
        if (summary.totalExpense().compareTo(summary.totalIncome()) > 0) {
            result.add(new Notification(NotificationType.EXPENSES_EXCEED_INCOME,
                    "Расходы (" + summary.totalExpense().toPlainString()
                            + ") превысили доходы (" + summary.totalIncome().toPlainString() + ")."));
        }
        if (!wallet.getOperations().isEmpty() && wallet.getBalance().signum() == 0) {
            result.add(new Notification(NotificationType.ZERO_BALANCE, "Баланс равен нулю."));
        }
        return result;
    }
}
