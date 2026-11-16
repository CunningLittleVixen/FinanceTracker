package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.CategoryNotFoundException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.service.BudgetService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BudgetCommandsTest {

    private final CommandFixture fixture = new CommandFixture();
    private final BudgetService service = new BudgetService();
    private final SetBudgetCommand set = new SetBudgetCommand(service);
    private final RemoveBudgetCommand remove = new RemoveBudgetCommand(service);

    @BeforeEach
    void setUp() {
        fixture.wallet.addCategory(new Category("Еда"));
    }

    @Test
    void setBudgetCreatesBudgetAndConfirms() {
        fixture.run(set, "set-budget Еда 4000");

        assertEquals(new BigDecimal("4000.00"), fixture.wallet.getBudget("Еда").orElseThrow().limit());
        assertTrue(fixture.output().contains("установлен"));
        assertTrue(fixture.output().contains("4,000.00"));
    }

    @Test
    void setBudgetAcceptsCommaAsDecimalSeparator() {
        fixture.run(set, "set-budget Еда 4000,50");

        assertEquals(new BigDecimal("4000.50"), fixture.wallet.getBudget("Еда").orElseThrow().limit());
    }

    @Test
    void setBudgetAgainReportsOldAndNewLimit() {
        fixture.run(set, "set-budget Еда 4000");
        fixture.run(set, "set-budget Еда 5000");

        assertTrue(fixture.output().contains("изменён"));
        assertTrue(fixture.output().contains("было 4,000.00, стало 5,000.00"));
    }

    @Test
    void invalidAmountIsRejectedAndNothingChanges() {
        assertThrows(ValidationException.class, () -> fixture.run(set, "set-budget Еда abc"));

        assertTrue(fixture.wallet.getBudget("Еда").isEmpty());
    }

    @Test
    void zeroLimitIsRejected() {
        assertThrows(ValidationException.class, () -> fixture.run(set, "set-budget Еда 0"));
    }

    @Test
    void unknownCategoryIsReported() {
        assertThrows(CategoryNotFoundException.class, () -> fixture.run(set, "set-budget Кино 100"));
    }

    @Test
    void removeBudgetDeletesItAndConfirms() {
        fixture.run(set, "set-budget Еда 4000");

        fixture.run(remove, "remove-budget Еда");

        assertTrue(fixture.wallet.getBudget("Еда").isEmpty());
        assertTrue(fixture.output().contains("удалён"));
    }

    @Test
    void removeMissingBudgetIsRejected() {
        assertThrows(ValidationException.class, () -> fixture.run(remove, "remove-budget Еда"));
    }

    @Test
    void bothCommandsAreMarkedForSaving() {
        assertTrue(set.changesData());
        assertTrue(remove.changesData());
    }
}
