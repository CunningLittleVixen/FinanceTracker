package io.github.cunninglittlevixen.financetracker.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.CategoryNotFoundException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class WalletTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet("anna");
        wallet.addCategory(new Category("Еда"));
        wallet.addCategory(new Category("Зарплата"));
    }

    private static Operation op(OperationType type, String category, String amount) {
        return new Operation(type, category, new BigDecimal(amount), LocalDate.now(), null, null);
    }

    @Test
    void newWalletIsEmpty() {
        Wallet empty = new Wallet("anna");

        assertEquals("anna", empty.getOwner());
        assertEquals(new BigDecimal("0.00"), empty.getBalance());
        assertTrue(empty.getOperations().isEmpty());
        assertTrue(empty.getCategories().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsBlankOwner(String owner) {
        assertThrows(ValidationException.class, () -> new Wallet(owner));
    }

    @Test
    void findsCategoryIgnoringCase() {
        assertTrue(wallet.hasCategory("еда"));
        assertFalse(wallet.hasCategory("Такси"));
        assertFalse(wallet.hasCategory(" "));
    }

    @Test
    void rejectsDuplicateCategory() {
        assertThrows(ValidationException.class, () -> wallet.addCategory(new Category("ЕДА")));
    }

    @Test
    void addsOperationWithKnownCategory() {
        wallet.addOperation(op(OperationType.EXPENSE, "Еда", "300"));

        assertEquals(1, wallet.getOperations().size());
    }

    @Test
    void rejectsOperationWithUnknownCategory() {
        CategoryNotFoundException e = assertThrows(CategoryNotFoundException.class,
                () -> wallet.addOperation(op(OperationType.EXPENSE, "Такси", "100")));

        assertEquals("Такси", e.getCategoryName());
        assertTrue(e.getMessage().contains("Еда"));
        assertTrue(wallet.getOperations().isEmpty());
    }

    @Test
    void balanceIsIncomeMinusExpense() {
        wallet.addOperation(op(OperationType.INCOME, "Зарплата", "1000"));
        wallet.addOperation(op(OperationType.EXPENSE, "Еда", "300"));

        assertEquals(new BigDecimal("700.00"), wallet.getBalance());
    }

    @Test
    void balanceCanBeNegative() {
        wallet.addOperation(op(OperationType.EXPENSE, "Еда", "500"));

        assertEquals(new BigDecimal("-500.00"), wallet.getBalance());
    }

    @Test
    void setBudgetReplacesPreviousOne() {
        wallet.setBudget(new Budget("Еда", new BigDecimal("4000")));
        wallet.setBudget(new Budget("еда", new BigDecimal("5000")));

        assertEquals(new BigDecimal("5000.00"), wallet.getBudget("Еда").orElseThrow().limit());
        assertEquals(1, wallet.getBudgets().size());
    }

    @Test
    void rejectsBudgetForUnknownCategory() {
        assertThrows(CategoryNotFoundException.class,
                () -> wallet.setBudget(new Budget("Такси", BigDecimal.TEN)));
    }

    @Test
    void missingBudgetIsEmpty() {
        assertTrue(wallet.getBudget("Еда").isEmpty());
    }

    @Test
    void returnedCollectionsAreReadOnly() {
        assertThrows(UnsupportedOperationException.class,
                () -> wallet.getOperations().add(op(OperationType.EXPENSE, "Еда", "1")));
        assertThrows(UnsupportedOperationException.class,
                () -> wallet.getCategories().add(new Category("Такси")));
        assertThrows(UnsupportedOperationException.class,
                () -> wallet.getBudgets().add(new Budget("Еда", BigDecimal.TEN)));
    }
}
