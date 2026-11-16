package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NotificationServiceTest {

    private NotificationService service;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        service = new NotificationService(new StatisticsService());
        wallet = new Wallet("anna");
        for (String name : new String[] {
                "Еда", "Развлечения", "Коммунальные услуги", "Такси", "Зарплата"}) {
            wallet.addCategory(new Category(name));
        }
        wallet.setBudget(new Budget("Еда", new BigDecimal("4000")));
        wallet.setBudget(new Budget("Развлечения", new BigDecimal("3000")));
        wallet.setBudget(new Budget("Коммунальные услуги", new BigDecimal("2500")));
    }

    private Operation add(OperationType type, String category, String amount) {
        Operation operation = new Operation(
                type, category, new BigDecimal(amount), LocalDate.of(2020, 1, 1), null, null);
        wallet.addOperation(operation);
        return operation;
    }

    private void fundWallet() {
        add(OperationType.INCOME, "Зарплата", "100000");
    }

    private static List<NotificationType> types(List<Notification> notifications) {
        return notifications.stream().map(Notification::type).toList();
    }

    @Test
    void exceededBudgetIsReportedAfterExpense() {
        fundWallet();
        Operation op = add(OperationType.EXPENSE, "Коммунальные услуги", "3000");

        List<Notification> notifications = service.afterOperation(wallet, op);

        assertEquals(List.of(NotificationType.BUDGET_EXCEEDED), types(notifications));
        assertTrue(notifications.get(0).message().contains("Коммунальные услуги"));
        assertTrue(notifications.get(0).message().contains("500.00"));
    }

    @Test
    void nearLimitMessageShowsPercentAndRemaining() {
        fundWallet();
        Operation op = add(OperationType.EXPENSE, "Еда", "3400");

        List<Notification> notifications = service.afterOperation(wallet, op);

        assertEquals(List.of(NotificationType.BUDGET_NEAR_LIMIT), types(notifications));
        assertTrue(notifications.get(0).message().contains("85%"));
        assertTrue(notifications.get(0).message().contains("600.00"));
    }

    @ParameterizedTest
    @CsvSource({"3200, true", "3159, false", "4000, true"})
    void warningThresholdIsEightyPercentInclusive(String amount, boolean expectWarning) {
        fundWallet();
        Operation op = add(OperationType.EXPENSE, "Еда", amount);

        List<Notification> notifications = service.afterOperation(wallet, op);

        assertEquals(expectWarning ? 1 : 0, notifications.size());
    }

    @Test
    void incomeDoesNotRepeatBudgetWarnings() {
        fundWallet();
        add(OperationType.EXPENSE, "Коммунальные услуги", "3000");
        Operation income = add(OperationType.INCOME, "Зарплата", "100");

        assertTrue(service.afterOperation(wallet, income).isEmpty());
    }

    @Test
    void onlyBudgetOfOperationCategoryIsReportedAfterExpense() {
        fundWallet();
        add(OperationType.EXPENSE, "Коммунальные услуги", "3000");
        Operation op = add(OperationType.EXPENSE, "Еда", "10");

        assertTrue(service.afterOperation(wallet, op).isEmpty());
    }

    @Test
    void categoryWithoutBudgetGivesNoBudgetWarning() {
        fundWallet();
        Operation op = add(OperationType.EXPENSE, "Такси", "99999");

        assertTrue(service.afterOperation(wallet, op).isEmpty());
    }

    @Test
    void expensesExceedingIncomeAreReported() {
        add(OperationType.INCOME, "Зарплата", "1000");
        Operation op = add(OperationType.EXPENSE, "Еда", "1500");

        assertEquals(List.of(NotificationType.EXPENSES_EXCEED_INCOME),
                types(service.afterOperation(wallet, op)));
    }

    @Test
    void zeroBalanceIsReportedWhenExpensesEqualIncome() {
        add(OperationType.INCOME, "Зарплата", "1000");
        Operation op = add(OperationType.EXPENSE, "Еда", "1000");

        assertEquals(List.of(NotificationType.ZERO_BALANCE),
                types(service.afterOperation(wallet, op)));
    }

    @Test
    void emptyWalletHasNoNotifications() {
        assertTrue(service.currentState(wallet).isEmpty());
    }

    @Test
    void currentStateForAssignmentExample() {
        add(OperationType.EXPENSE, "Еда", "300");
        add(OperationType.EXPENSE, "Еда", "500");
        add(OperationType.EXPENSE, "Развлечения", "3000");
        add(OperationType.EXPENSE, "Коммунальные услуги", "3000");
        add(OperationType.EXPENSE, "Такси", "1500");
        add(OperationType.INCOME, "Зарплата", "20000");
        add(OperationType.INCOME, "Зарплата", "40000");
        add(OperationType.INCOME, "Зарплата", "3000");

        assertEquals(
                List.of(NotificationType.BUDGET_NEAR_LIMIT, NotificationType.BUDGET_EXCEEDED),
                types(service.currentState(wallet)));
    }
}
