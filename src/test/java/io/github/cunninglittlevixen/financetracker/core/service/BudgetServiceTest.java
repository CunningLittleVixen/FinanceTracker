package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.CategoryNotFoundException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BudgetServiceTest {

    private BudgetService service;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        service = new BudgetService();
        wallet = new Wallet("anna");
        wallet.addCategory(new Category("Еда"));
    }

    @Test
    void setBudgetStoresLimitWithTwoDecimals() {
        Budget budget = service.setBudget(wallet, "Еда", new BigDecimal("4000"));

        assertEquals(new BigDecimal("4000.00"), budget.limit());
        assertEquals(budget, wallet.getBudget("Еда").orElseThrow());
    }

    @Test
    void setBudgetAgainEditsLimit() {
        service.setBudget(wallet, "Еда", new BigDecimal("4000"));
        service.setBudget(wallet, "еда", new BigDecimal("5000"));

        assertEquals(1, wallet.getBudgets().size());
        assertEquals(new BigDecimal("5000.00"), wallet.getBudget("Еда").orElseThrow().limit());
    }

    @Test
    void setBudgetForUnknownCategoryFails() {
        assertThrows(CategoryNotFoundException.class,
                () -> service.setBudget(wallet, "Кино", new BigDecimal("100")));
    }

    @Test
    void invalidLimitKeepsPreviousBudget() {
        service.setBudget(wallet, "Еда", new BigDecimal("4000"));

        assertThrows(ValidationException.class,
                () -> service.setBudget(wallet, "Еда", BigDecimal.ZERO));

        assertEquals(new BigDecimal("4000.00"), wallet.getBudget("Еда").orElseThrow().limit());
    }

    @Test
    void removeBudgetDeletesIt() {
        service.setBudget(wallet, "Еда", new BigDecimal("4000"));

        service.removeBudget(wallet, "Еда");

        assertTrue(wallet.getBudget("Еда").isEmpty());
    }
}
