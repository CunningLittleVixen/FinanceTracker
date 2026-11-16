package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategoryServiceTest {

    private CategoryService service;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        service = new CategoryService();
        wallet = new Wallet("anna");
    }

    @Test
    void addCategoryStoresTrimmedCategory() {
        Category category = service.addCategory(wallet, "  Еда ");

        assertEquals("Еда", category.name());
        assertTrue(wallet.hasCategory("еда"));
    }

    @Test
    void addDuplicateCategoryFails() {
        service.addCategory(wallet, "Еда");

        assertThrows(ValidationException.class, () -> service.addCategory(wallet, "ЕДА"));
    }

    @Test
    void renameCategoryChangesName() {
        service.addCategory(wallet, "Еда");

        service.renameCategory(wallet, "Еда", "Продукты");

        assertTrue(wallet.hasCategory("Продукты"));
        assertFalse(wallet.hasCategory("Еда"));
    }

    @Test
    void removeCategoryWithOperationsFails() {
        service.addCategory(wallet, "Еда");
        new OperationService().addExpense(
                wallet, "Еда", new BigDecimal("100"), java.time.LocalDate.now(), null);

        assertThrows(ValidationException.class, () -> service.removeCategory(wallet, "Еда"));
        assertTrue(wallet.hasCategory("Еда"));
    }
}