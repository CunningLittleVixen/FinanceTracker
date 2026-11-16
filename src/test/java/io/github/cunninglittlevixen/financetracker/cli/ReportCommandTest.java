package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.service.NotificationService;
import io.github.cunninglittlevixen.financetracker.core.service.StatisticsService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportCommandTest {

    @TempDir
    Path tempDir;

    private final CommandFixture fixture = new CommandFixture();
    private final StatisticsService statistics = new StatisticsService();
    private final ReportCommand command =
            new ReportCommand(statistics, new NotificationService(statistics));

    @BeforeEach
    void setUp() {
        SampleData.fillAssignmentExample(fixture.wallet);
    }

    @Test
    void reportPrintsAssignmentExample() {
        fixture.run(command, "report");

        String out = fixture.output();
        assertTrue(out.contains("Общий доход: 63,000.00"));
        assertTrue(out.contains("Общие расходы: 8,300.00"));
        assertTrue(out.contains("Бюджет по категориям:"));
        assertTrue(out.contains("-500.00"));
    }

    @Test
    void reportForSelectedCategoriesAndPeriod() {
        fixture.run(command,
                "report --categories \"Еда,Такси\" --from 2020-02-01 --to 2020-02-28");

        String out = fixture.output();
        assertTrue(out.contains("Категории: Еда, Такси"));
        assertTrue(out.contains("Общие расходы: 2,000.00"));
    }

    @Test
    void unknownCategoriesAreAllListed() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(command, "report --categories \"Кино,Театр\""));

        assertTrue(e.getMessage().contains("Кино"));
        assertTrue(e.getMessage().contains("Театр"));
    }

    @Test
    void trailingCommaInCategoriesIsRejected() {
        assertThrows(ValidationException.class,
                () -> fixture.run(command, "report --categories \"Еда,\""));
    }

    @Test
    void reversedPeriodIsRejected() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(command, "report --from 2020-03-01 --to 2020-01-01"));

        assertTrue(e.getMessage().contains("позже"));
    }

    @Test
    void wrongDateFormatIsRejected() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(command, "report --from 01.02.2020"));

        assertTrue(e.getMessage().contains("ГГГГ-ММ-ДД"));
    }

    @Test
    void emptyPeriodSaysNothingFound() {
        fixture.run(command, "report --from 2021-01-01");

        assertTrue(fixture.output().contains("не найдено"));
    }

    @Test
    void outOptionWritesReportToFileInsteadOfScreen() throws IOException {
        Path file = tempDir.resolve("r.txt");

        fixture.run(command, "report --out \"" + file + "\"");

        assertTrue(Files.readString(file).contains("Общий доход: 63,000.00"));
        assertTrue(fixture.output().contains("Сохранено в файл"));
        assertFalse(fixture.output().contains("Общий доход"));
    }

    @Test
    void outOptionRefusesToOverwriteExistingFile() throws IOException {
        Path file = tempDir.resolve("r.txt");
        Files.writeString(file, "старое");

        ValidationException e = assertThrows(ValidationException.class,
                () -> fixture.run(command, "report --out \"" + file + "\""));

        assertTrue(e.getMessage().contains("уже существует"));
        assertEquals("старое", Files.readString(file));
    }

    @Test
    void outToMissingFolderGivesStorageError() {
        Path file = tempDir.resolve("no").resolve("such").resolve("r.txt");

        assertThrows(StorageException.class,
                () -> fixture.run(command, "report --out \"" + file + "\""));
    }

    @Test
    void unknownOptionIsRejected() {
        assertThrows(ValidationException.class, () -> fixture.run(command, "report --foo x"));
    }

    @Test
    void reportDoesNotChangeData() {
        assertFalse(command.changesData());
    }
}
