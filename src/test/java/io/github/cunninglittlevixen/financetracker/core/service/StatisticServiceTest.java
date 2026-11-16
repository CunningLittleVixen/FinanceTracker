package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatisticsServiceTest {

    private StatisticsService service;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        service = new StatisticsService();
        wallet = new Wallet("anna");
        for (String name : new String[] {
                "Еда", "Развлечения", "Коммунальные услуги", "Такси", "Зарплата", "Бонус"}) {
            wallet.addCategory(new Category(name));
        }
        add(OperationType.EXPENSE, "Еда", "300", d(1, 5));
        add(OperationType.EXPENSE, "Еда", "500", d(2, 5));
        add(OperationType.EXPENSE, "Развлечения", "3000", d(1, 20));
        add(OperationType.EXPENSE, "Коммунальные услуги", "3000", d(2, 10));
        add(OperationType.EXPENSE, "Такси", "1500", d(2, 20));
        add(OperationType.INCOME, "Зарплата", "20000", d(1, 1));
        add(OperationType.INCOME, "Зарплата", "40000", d(2, 1));
        add(OperationType.INCOME, "Бонус", "3000", d(2, 15));
    }

    private void add(OperationType type, String category, String amount, LocalDate date) {
        wallet.addOperation(new Operation(type, category, new BigDecimal(amount), date, null, null));
    }

    private static LocalDate d(int month, int day) {
        return LocalDate.of(2020, month, day);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    @Test
    void totalsForAllOperationsMatchAssignmentExample() {
        assertEquals(money("63000.00"), service.totalIncome(wallet, OperationFilter.all()));
        assertEquals(money("8300.00"), service.totalExpense(wallet, OperationFilter.all()));
        assertEquals(8, service.select(wallet, OperationFilter.all()).size());
    }

    @Test
    void totalsForSingleCategory() {
        OperationFilter filter = new OperationFilter(List.of("Еда"), null, null);

        assertEquals(money("800.00"), service.totalExpense(wallet, filter));
        assertEquals(money("0.00"), service.totalIncome(wallet, filter));
    }

    @Test
    void totalsForSeveralCategoriesIgnoreCase() {
        OperationFilter filter = new OperationFilter(List.of("еда", "Зарплата"), null, null);

        assertEquals(money("800.00"), service.totalExpense(wallet, filter));
        assertEquals(money("60000.00"), service.totalIncome(wallet, filter));
    }

    @Test
    void periodBoundsAreInclusive() {
        OperationFilter filter = new OperationFilter(null, d(2, 1), d(2, 28));

        assertEquals(money("5000.00"), service.totalExpense(wallet, filter));
        assertEquals(money("43000.00"), service.totalIncome(wallet, filter));
    }

    @Test
    void periodWithOnlyStart() {
        OperationFilter filter = new OperationFilter(null, d(2, 15), null);

        assertEquals(money("1500.00"), service.totalExpense(wallet, filter));
        assertEquals(money("3000.00"), service.totalIncome(wallet, filter));
    }

    @Test
    void periodWithOnlyEnd() {
        OperationFilter filter = new OperationFilter(null, null, d(1, 31));

        assertEquals(money("3300.00"), service.totalExpense(wallet, filter));
        assertEquals(money("20000.00"), service.totalIncome(wallet, filter));
    }

    @Test
    void periodWithoutOperationsGivesEmptySelectionAndZeroTotals() {
        OperationFilter filter = new OperationFilter(null, LocalDate.of(2021, 1, 1), null);

        assertTrue(service.select(wallet, filter).isEmpty());
        assertEquals(money("0.00"), service.totalExpense(wallet, filter));
        assertEquals(money("0.00"), service.totalIncome(wallet, filter));
    }

    @Test
    void unknownCategoriesAreAllListedInError() {
        OperationFilter filter = new OperationFilter(List.of("Кино", "Еда", "Театр"), null, null);

        ValidationException e = assertThrows(ValidationException.class,
                () -> service.totalExpense(wallet, filter));

        assertTrue(e.getMessage().contains("Кино"));
        assertTrue(e.getMessage().contains("Театр"));
    }
}
