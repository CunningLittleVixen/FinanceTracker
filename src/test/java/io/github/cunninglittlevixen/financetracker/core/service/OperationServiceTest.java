package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.CategoryNotFoundException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OperationServiceTest {

    private static final LocalDate TODAY = LocalDate.now();

    private OperationService service;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        service = new OperationService();
        wallet = new Wallet("anna");
        for (String name : new String[] {
                "Еда", "Развлечения", "Коммунальные услуги", "Такси", "Зарплата", "Бонус"}) {
            wallet.addCategory(new Category(name));
        }
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    @Test
    void addIncomeStoresIncomeOperation() {
        Operation op = service.addIncome(wallet, "Зарплата", money("20000"), TODAY, "аванс");

        assertEquals(OperationType.INCOME, op.type());
        assertEquals("аванс", op.description());
        assertEquals(1, wallet.getOperations().size());
    }

    @Test
    void addExpenseStoresExpenseOperation() {
        Operation op = service.addExpense(wallet, "Еда", money("300"), TODAY, null);

        assertEquals(OperationType.EXPENSE, op.type());
        assertEquals(op, wallet.getOperations().get(0));
    }

    @Test
    void unknownCategoryLeavesWalletUnchanged() {
        assertThrows(CategoryNotFoundException.class,
                () -> service.addExpense(wallet, "Кино", money("100"), TODAY, null));

        assertTrue(wallet.getOperations().isEmpty());
    }

    @Test
    void zeroAmountLeavesWalletUnchanged() {
        assertThrows(ValidationException.class,
                () -> service.addExpense(wallet, "Еда", money("0"), TODAY, null));

        assertTrue(wallet.getOperations().isEmpty());
    }

    @Test
    void futureDateLeavesWalletUnchanged() {
        assertThrows(ValidationException.class,
                () -> service.addIncome(wallet, "Бонус", money("100"), TODAY.plusDays(1), null));

        assertTrue(wallet.getOperations().isEmpty());
    }

    @Test
    void exampleFromAssignmentGivesExpectedBalance() {
        service.addExpense(wallet, "Еда", money("300"), TODAY, null);
        service.addExpense(wallet, "Еда", money("500"), TODAY, null);
        service.addExpense(wallet, "Развлечения", money("3000"), TODAY, null);
        service.addExpense(wallet, "Коммунальные услуги", money("3000"), TODAY, null);
        service.addExpense(wallet, "Такси", money("1500"), TODAY, null);
        service.addIncome(wallet, "Зарплата", money("20000"), TODAY, null);
        service.addIncome(wallet, "Зарплата", money("40000"), TODAY, null);
        service.addIncome(wallet, "Бонус", money("3000"), TODAY, null);

        assertEquals(8, wallet.getOperations().size());
        assertEquals(new BigDecimal("54700.00"), wallet.getBalance());
    }
}
