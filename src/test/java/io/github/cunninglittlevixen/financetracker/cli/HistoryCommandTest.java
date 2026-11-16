package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.service.StatisticsService;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HistoryCommandTest {

    @TempDir
    Path tempDir;

    private final CommandFixture fixture = new CommandFixture();
    private final HistoryCommand command = new HistoryCommand(new StatisticsService());

    @BeforeEach
    void setUp() {
        SampleData.fillAssignmentExample(fixture.wallet);
    }

    @Test
    void listsOperationsSortedByDateWithTotalCount() {
        fixture.run(command, "history");

        String out = fixture.output();
        assertTrue(out.indexOf("2020-01-20") < out.indexOf("2020-02-05"));
        assertTrue(out.contains("Всего операций: 8."));
    }

    @Test
    void showsTypeAndFormattedAmount() {
        fixture.run(command, "history");

        assertTrue(fixture.output().contains("Расход"));
        assertTrue(fixture.output().contains("Доход"));
        assertTrue(fixture.output().contains("3,000.00"));
    }

    @Test
    void showsTransferCounterparty() {
        fixture.wallet.addCategory(new Category("Перевод"));
        fixture.wallet.addOperation(new Operation(OperationType.EXPENSE, "Перевод",
                new BigDecimal("100"), LocalDate.of(2020, 3, 1), "", "boris"));
        fixture.wallet.addOperation(new Operation(OperationType.INCOME, "Перевод",
                new BigDecimal("50"), LocalDate.of(2020, 3, 2), "", "vera"));

        fixture.run(command, "history --from 2020-03-01");

        assertTrue(fixture.output().contains("кому: boris"));
        assertTrue(fixture.output().contains("от: vera"));
    }

    @Test
    void filtersByCategoryAndPeriod() {
        fixture.run(command, "history --categories Еда --from 2020-02-01");

        assertTrue(fixture.output().contains("2020-02-05"));
        assertFalse(fixture.output().contains("2020-01-05"));
    }

    @Test
    void emptyResultSaysSo() {
        fixture.run(command, "history --from 2021-01-01");

        assertTrue(fixture.output().contains("Операций не найдено."));
    }

    @Test
    void outOptionWritesTableToFile() throws IOException {
        Path file = tempDir.resolve("h.txt");

        fixture.run(command, "history --out \"" + file + "\"");

        assertTrue(Files.readString(file).contains("Дата"));
    }

    @Test
    void unknownCategoryIsRejected() {
        assertThrows(ValidationException.class,
                () -> fixture.run(command, "history --categories Кино"));
    }
}
