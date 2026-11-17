package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.model.WalletContent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImportServiceTest {

    private final ImportService service = new ImportService();
    private final Wallet wallet = new Wallet("anna");

    private static Operation op(String category, String amount) {
        return new Operation(OperationType.EXPENSE, category, new BigDecimal(amount),
                LocalDate.of(2020, 1, 5), null, null);
    }

    @Test
    void createsMissingCategoriesFromContentBudgetsAndOperationsInOrder() {
        WalletContent content = new WalletContent(
                List.of(new Category("Еда")),
                List.of(new Budget("Такси", new BigDecimal("1000"))),
                List.of(op("Кино", "10")));

        ImportResult result = service.merge(wallet, content);

        assertEquals(List.of("Еда", "Такси", "Кино"), result.createdCategories());
        assertEquals(1, result.budgets());
        assertEquals(1, result.operations());
        assertTrue(wallet.hasCategory("Такси"));
        assertTrue(wallet.getBudget("Такси").isPresent());
    }

    @Test
    void keepsExistingCategorySpelling() {
        wallet.addCategory(new Category("Еда"));

        ImportResult result = service.merge(wallet,
                new WalletContent(List.of(), List.of(), List.of(op("еда", "10"))));

        assertTrue(result.createdCategories().isEmpty());
        assertEquals("Еда", wallet.getOperations().get(0).category());
    }

    @Test
    void importedBudgetReplacesBudgetOfSameCategory() {
        wallet.addCategory(new Category("Еда"));
        wallet.setBudget(new Budget("Еда", new BigDecimal("1000")));

        service.merge(wallet, new WalletContent(List.of(),
                List.of(new Budget("Еда", new BigDecimal("2000"))), List.of()));

        assertEquals(new BigDecimal("2000.00"), wallet.getBudget("Еда").orElseThrow().limit());
    }

    @Test
    void appendsOperationsAndKeepsExistingOnes() {
        wallet.addCategory(new Category("Еда"));
        wallet.addOperation(op("Еда", "100"));

        ImportResult result = service.merge(wallet, new WalletContent(List.of(), List.of(),
                List.of(op("Еда", "10"), op("Еда", "20"))));

        assertEquals(3, wallet.getOperations().size());
        assertEquals(2, result.operations());
        assertEquals(new BigDecimal("-130.00"), wallet.getBalance());
    }

    @Test
    void emptyContentChangesNothing() {
        ImportResult result = service.merge(wallet, new WalletContent(null, null, null));

        assertTrue(result.isEmpty());
        assertTrue(wallet.getCategories().isEmpty());
    }

    @Test
    void importingSameContentTwiceDuplicatesOperations() {
        WalletContent content = new WalletContent(List.of(), List.of(), List.of(op("Еда", "10")));

        service.merge(wallet, content);
        service.merge(wallet, content);

        assertEquals(2, wallet.getOperations().size());
    }
}
