package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.CategoryNotFoundException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.service.CategoryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CategoryCommandsTest {

    private final CommandFixture fixture = new CommandFixture();
    private final CategoryService service = new CategoryService();
    private final AddCategoryCommand add = new AddCategoryCommand(service);
    private final RenameCategoryCommand rename = new RenameCategoryCommand(service);
    private final RemoveCategoryCommand remove = new RemoveCategoryCommand(service);
    private final CategoriesCommand list = new CategoriesCommand();

    @Test
    void addCategoryCreatesItAndConfirms() {
        fixture.run(add, "add-category Еда");

        assertTrue(fixture.wallet.hasCategory("Еда"));
        assertTrue(fixture.output().contains("«Еда» создана"));
    }

    @Test
    void quotedNameKeepsSpaces() {
        fixture.run(add, "add-category \"Коммунальные услуги\"");

        assertTrue(fixture.wallet.hasCategory("Коммунальные услуги"));
    }

    @Test
    void nameWithSpacesWithoutQuotesShowsUsage() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(add, "add-category Коммунальные услуги"));

        assertTrue(e.getMessage().contains("add-category <название>"));
    }

    @Test
    void duplicateCategoryIsRejected() {
        fixture.run(add, "add-category Еда");

        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(add, "add-category еда"));

        assertTrue(e.getMessage().contains("уже существует"));
    }

    @Test
    void renameMovesCategoryAndConfirms() {
        fixture.wallet.addCategory(new Category("Еда"));
        fixture.wallet.setBudget(new Budget("Еда", new BigDecimal("4000")));
        fixture.wallet.addOperation(new Operation(OperationType.EXPENSE, "Еда",
                new BigDecimal("300"), LocalDate.of(2020, 1, 1), null, null));

        fixture.run(rename, "rename-category Еда Продукты");

        assertTrue(fixture.wallet.hasCategory("Продукты"));
        assertEquals("Продукты", fixture.wallet.getOperations().get(0).category());
        assertTrue(fixture.wallet.getBudget("Продукты").isPresent());
        assertTrue(fixture.output().contains("переименована"));
    }

    @Test
    void removeDeletesCategoryAndConfirms() {
        fixture.wallet.addCategory(new Category("Такси"));

        fixture.run(remove, "remove-category Такси");

        assertFalse(fixture.wallet.hasCategory("Такси"));
        assertTrue(fixture.output().contains("удалена"));
    }

    @Test
    void removeCategoryWithOperationsIsRejected() {
        fixture.wallet.addCategory(new Category("Еда"));
        fixture.wallet.addOperation(new Operation(OperationType.EXPENSE, "Еда",
                new BigDecimal("300"), LocalDate.of(2020, 1, 1), null, null));

        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(remove, "remove-category Еда"));

        assertTrue(e.getMessage().contains("операций"));
        assertTrue(fixture.wallet.hasCategory("Еда"));
    }

    @Test
    void removeUnknownCategoryReportsMissingCategory() {
        assertThrows(CategoryNotFoundException.class,
                () -> fixture.run(remove, "remove-category Кино"));
    }

    @Test
    void listShowsCategoriesWithBudgets() {
        fixture.wallet.addCategory(new Category("Еда"));
        fixture.wallet.addCategory(new Category("Такси"));
        fixture.wallet.setBudget(new Budget("Еда", new BigDecimal("4000")));

        fixture.run(list, "categories");

        String out = fixture.output();
        assertTrue(out.contains("Категория"));
        assertTrue(out.contains("Еда"));
        assertTrue(out.contains("4,000.00"));
        assertTrue(out.contains("Такси"));
    }

    @Test
    void emptyListSuggestsAddCategory() {
        fixture.run(list, "categories");

        assertTrue(fixture.output().contains("add-category"));
    }

    @Test
    void onlyDataChangingCommandsAreMarkedForSaving() {
        assertTrue(add.changesData());
        assertTrue(rename.changesData());
        assertTrue(remove.changesData());
        assertFalse(list.changesData());
    }
}
