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
import io.github.cunninglittlevixen.financetracker.core.service.NotificationService;
import io.github.cunninglittlevixen.financetracker.core.service.OperationService;
import io.github.cunninglittlevixen.financetracker.core.service.StatisticsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AddOperationCommandTest {

    private final CommandFixture fixture = new CommandFixture();
    private final OperationService service = new OperationService();
    private final NotificationService notifications = new NotificationService(new StatisticsService());
    private final AddOperationCommand income =
            new AddOperationCommand(OperationType.INCOME, service, notifications);
    private final AddOperationCommand expense =
            new AddOperationCommand(OperationType.EXPENSE, service, notifications);

    @BeforeEach
    void setUp() {
        fixture.wallet.addCategory(new Category("Еда"));
        fixture.wallet.addCategory(new Category("Зарплата"));
    }

    @Test
    void addExpenseStoresOperationAndShowsBalance() {
        fixture.run(expense, "add-expense Еда 300");

        Operation op = fixture.wallet.getOperations().get(0);
        assertEquals(OperationType.EXPENSE, op.type());
        assertEquals(new BigDecimal("300.00"), op.amount());
        assertEquals(LocalDate.now(), op.date());
        assertTrue(fixture.output().contains("Расход добавлен"));
        assertTrue(fixture.output().contains("-300.00"));
    }

    @Test
    void addIncomeStoresDescription() {
        fixture.run(income, "add-income Зарплата 20000 \"аванс\"");

        assertEquals("аванс", fixture.wallet.getOperations().get(0).description());
        assertTrue(fixture.output().contains("Доход добавлен"));
        assertTrue(fixture.output().contains("20,000.00"));
    }

    @Test
    void dateOptionSetsOperationDate() {
        fixture.run(expense, "add-expense Еда 100 --date 2020-01-05");

        assertEquals(LocalDate.of(2020, 1, 5), fixture.wallet.getOperations().get(0).date());
    }

    @Test
    void futureDateIsRejected() {
        String tomorrow = LocalDate.now().plusDays(1).toString();

        assertThrows(ValidationException.class,
                () -> fixture.run(expense, "add-expense Еда 100 --date " + tomorrow));

        assertTrue(fixture.wallet.getOperations().isEmpty());
    }

    @Test
    void wrongDateFormatShowsFormatHint() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(expense, "add-expense Еда 100 --date 05.01.2020"));

        assertTrue(e.getMessage().contains("ГГГГ-ММ-ДД"));
    }

    @Test
    void unknownCategoryIsReportedAndNothingIsAdded() {
        assertThrows(CategoryNotFoundException.class,
                () -> fixture.run(expense, "add-expense Кино 100"));

        assertTrue(fixture.wallet.getOperations().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0"})
    void invalidAmountIsRejected(String amount) {
        assertThrows(ValidationException.class,
                () -> fixture.run(expense, "add-expense Еда " + amount));

        assertTrue(fixture.wallet.getOperations().isEmpty());
    }

    @Test
    void missingAmountShowsUsage() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(expense, "add-expense Еда"));

        assertTrue(e.getMessage().contains("add-expense <категория> <сумма>"));
    }

    @Test
    void unknownOptionIsRejected() {
        assertThrows(ValidationException.class,
                () -> fixture.run(expense, "add-expense Еда 100 --foo bar"));
    }

    @Test
    void exceededBudgetAlertIsShownAfterExpense() {
        fixture.wallet.setBudget(new Budget("Еда", new BigDecimal("1000")));
        fixture.run(income, "add-income Зарплата 5000");

        fixture.run(expense, "add-expense Еда 1500");

        assertTrue(fixture.output().contains("Превышен бюджет"));
    }

    @Test
    void bothCommandsAreMarkedForSaving() {
        assertTrue(income.changesData());
        assertTrue(expense.changesData());
    }

    @Test
    void commandsHaveDifferentNames() {
        assertEquals("add-income", income.name());
        assertEquals("add-expense", expense.name());
        assertFalse(income.usage().equals(expense.usage()));
    }
}
