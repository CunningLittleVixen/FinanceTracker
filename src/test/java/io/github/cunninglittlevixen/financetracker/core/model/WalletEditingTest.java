package io.github.cunninglittlevixen.financetracker.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.CategoryNotFoundException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WalletEditingTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet("anna");
        wallet.addCategory(new Category("Еда"));
        wallet.addCategory(new Category("Такси"));
        wallet.addCategory(new Category("Зарплата"));
    }

    private static Operation expense(String category, String amount) {
        return new Operation(OperationType.EXPENSE, category, new BigDecimal(amount),
                LocalDate.now(), null, null);
    }

    private List<String> categoryNames() {
        return wallet.getCategories().stream().map(Category::name).toList();
    }

    @Test
    void renameUpdatesCategoryOperationsAndBudget() {
        wallet.addOperation(expense("Еда", "300"));
        wallet.addOperation(expense("Еда", "500"));
        wallet.setBudget(new Budget("Еда", new BigDecimal("4000")));

        wallet.renameCategory("Еда", "Продукты");

        assertTrue(wallet.hasCategory("Продукты"));
        assertFalse(wallet.hasCategory("Еда"));
        assertTrue(wallet.getOperations().stream().allMatch(op -> op.category().equals("Продукты")));
        assertEquals("Продукты", wallet.getBudget("Продукты").orElseThrow().category());
        assertEquals(new BigDecimal("4000.00"), wallet.getBudget("Продукты").orElseThrow().limit());
        assertEquals(new BigDecimal("-800.00"), wallet.getBalance());
    }

    @Test
    void renameKeepsCategoryOrder() {
        wallet.renameCategory("Такси", "Транспорт");

        assertEquals(List.of("Еда", "Транспорт", "Зарплата"), categoryNames());
    }

    @Test
    void renameToExistingNameFailsAndChangesNothing() {
        wallet.addOperation(expense("Еда", "300"));

        assertThrows(ValidationException.class, () -> wallet.renameCategory("Еда", "такси"));

        assertTrue(wallet.hasCategory("Еда"));
        assertEquals("Еда", wallet.getOperations().get(0).category());
    }

    @Test
    void renameChangingOnlyCaseIsAllowed() {
        wallet.renameCategory("Еда", "ЕДА");

        assertEquals(List.of("ЕДА", "Такси", "Зарплата"), categoryNames());
    }

    @Test
    void renameUnknownCategoryFails() {
        assertThrows(CategoryNotFoundException.class, () -> wallet.renameCategory("Кино", "Театр"));
    }

    @Test
    void renameToBlankNameFailsAndChangesNothing() {
        assertThrows(ValidationException.class, () -> wallet.renameCategory("Еда", " "));

        assertEquals(List.of("Еда", "Такси", "Зарплата"), categoryNames());
    }

    @Test
    void removeCategoryWithoutOperationsAlsoRemovesBudget() {
        wallet.setBudget(new Budget("Такси", new BigDecimal("1000")));

        wallet.removeCategory("такси");

        assertFalse(wallet.hasCategory("Такси"));
        assertTrue(wallet.getBudgets().isEmpty());
    }

    @Test
    void removeCategoryWithOperationsFails() {
        wallet.addOperation(expense("Еда", "300"));

        ValidationException e = assertThrows(ValidationException.class,
                () -> wallet.removeCategory("Еда"));

        assertTrue(e.getMessage().contains("1"));
        assertTrue(wallet.hasCategory("Еда"));
    }

    @Test
    void removeUnknownCategoryFails() {
        assertThrows(CategoryNotFoundException.class, () -> wallet.removeCategory("Кино"));
    }

    @Test
    void removeBudgetWorksOnceAndThenFails() {
        wallet.setBudget(new Budget("Еда", new BigDecimal("4000")));

        wallet.removeBudget("Еда");

        assertTrue(wallet.getBudget("Еда").isEmpty());
        assertThrows(ValidationException.class, () -> wallet.removeBudget("Еда"));
    }
}
