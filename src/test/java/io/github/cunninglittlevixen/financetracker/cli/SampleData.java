package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;

final class SampleData {

    private SampleData() {
    }

    static void fillAssignmentExample(Wallet wallet) {
        for (String name : new String[] {
                "Еда", "Развлечения", "Коммунальные услуги", "Такси", "Зарплата", "Бонус"}) {
            wallet.addCategory(new Category(name));
        }
        add(wallet, OperationType.EXPENSE, "Еда", "300", 1, 5);
        add(wallet, OperationType.EXPENSE, "Еда", "500", 2, 5);
        add(wallet, OperationType.EXPENSE, "Развлечения", "3000", 1, 20);
        add(wallet, OperationType.EXPENSE, "Коммунальные услуги", "3000", 2, 10);
        add(wallet, OperationType.EXPENSE, "Такси", "1500", 2, 20);
        add(wallet, OperationType.INCOME, "Зарплата", "20000", 1, 1);
        add(wallet, OperationType.INCOME, "Зарплата", "40000", 2, 1);
        add(wallet, OperationType.INCOME, "Бонус", "3000", 2, 15);
        wallet.setBudget(new Budget("Еда", new BigDecimal("4000")));
        wallet.setBudget(new Budget("Развлечения", new BigDecimal("3000")));
        wallet.setBudget(new Budget("Коммунальные услуги", new BigDecimal("2500")));
    }

    private static void add(
            Wallet wallet, OperationType type, String category, String amount, int month, int day) {
        wallet.addOperation(new Operation(type, category, new BigDecimal(amount),
                LocalDate.of(2020, month, day), null, null));
    }
}
