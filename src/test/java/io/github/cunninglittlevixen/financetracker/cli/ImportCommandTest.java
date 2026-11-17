package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.service.FormatRegistry;
import io.github.cunninglittlevixen.financetracker.core.service.ImportService;
import io.github.cunninglittlevixen.financetracker.infra.csv.CsvWalletFormat;
import io.github.cunninglittlevixen.financetracker.infra.json.JsonMapperFactory;
import io.github.cunninglittlevixen.financetracker.infra.json.JsonWalletFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImportCommandTest {

    private static final String HEADER = "date,type,category,amount,description,counterparty\n";

    @TempDir
    Path tempDir;

    private final CommandFixture fixture = new CommandFixture();
    private final JsonWalletFormat json = new JsonWalletFormat(JsonMapperFactory.create());
    private final ImportCommand importer = new ImportCommand(
            new FormatRegistry(List.of(new CsvWalletFormat(), json)), new ImportService());

    private String commandFor(Path file) {
        return "import \"" + file + "\"";
    }

    @Test
    void importCsvAddsOperationsAndCreatesCategories() throws IOException {
        Path file = tempDir.resolve("ops.csv");
        Files.writeString(file, HEADER
                + "2020-01-05,EXPENSE,Еда,300,обед,\n"
                + "2020-01-06,INCOME,Зарплата,1000,,\n");

        fixture.run(importer, commandFor(file));

        assertEquals(2, fixture.wallet.getOperations().size());
        assertTrue(fixture.wallet.hasCategory("Еда"));
        assertTrue(fixture.output().contains("добавлено операций — 2"));
        assertTrue(fixture.output().contains("Созданы категории: Еда, Зарплата."));
    }

    @Test
    void importJsonSnapshotRestoresBudgetsAndOperations() throws IOException {
        Wallet source = new Wallet("anna");
        source.addCategory(new Category("Еда"));
        source.setBudget(new Budget("Еда", new BigDecimal("4000")));
        source.addOperation(new Operation(OperationType.EXPENSE, "Еда", new BigDecimal("300"),
                LocalDate.of(2020, 1, 5), null, null));
        StringWriter text = new StringWriter();
        json.write(source, text);
        Path file = tempDir.resolve("backup.json");
        Files.writeString(file, text.toString());

        fixture.run(importer, commandFor(file));

        assertTrue(fixture.wallet.getBudget("Еда").isPresent());
        assertEquals(1, fixture.wallet.getOperations().size());
    }

    @Test
    void invalidFileLeavesWalletUnchanged() throws IOException {
        Path file = tempDir.resolve("ops.csv");
        Files.writeString(file, HEADER
                + "2020-01-05,EXPENSE,Еда,300,,\n"
                + "bad,EXPENSE,Еда,1,,\n");

        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(importer, commandFor(file)));

        assertTrue(e.getMessage().contains("Строка 3"));
        assertTrue(fixture.wallet.getOperations().isEmpty());
        assertTrue(fixture.wallet.getCategories().isEmpty());
    }

    @Test
    void missingFileIsReported() {
        Path file = tempDir.resolve("missing.csv");

        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(importer, commandFor(file)));

        assertTrue(e.getMessage().contains("Файл не найден"));
    }

    @Test
    void unknownExtensionIsRejected() {
        Path file = tempDir.resolve("data.txt");

        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(importer, commandFor(file)));

        assertTrue(e.getMessage().contains("Неизвестный формат"));
    }

    @Test
    void importIsMarkedAsDataChanging() {
        assertTrue(importer.changesData());
    }
}
