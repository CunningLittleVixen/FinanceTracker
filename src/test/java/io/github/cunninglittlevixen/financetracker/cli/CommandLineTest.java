package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CommandLineTest {

    @Test
    void parsesNameAndArguments() {
        CommandLine line = CommandLine.parse("add-expense Еда 300");

        assertEquals("add-expense", line.name());
        assertEquals(List.of("Еда", "300"), line.args());
        assertTrue(line.options().isEmpty());
    }

    @Test
    void commandNameIgnoresCaseButArgumentsKeepIt() {
        CommandLine line = CommandLine.parse("ADD-Expense Еда");

        assertEquals("add-expense", line.name());
        assertEquals(List.of("Еда"), line.args());
    }

    @Test
    void quotesGroupWordsIntoOneArgument() {
        CommandLine line = CommandLine.parse("add-expense \"Коммунальные услуги\" 3000 \"за ноябрь\"");

        assertEquals(List.of("Коммунальные услуги", "3000", "за ноябрь"), line.args());
    }

    @Test
    void extraSpacesAreIgnored() {
        CommandLine line = CommandLine.parse("   help    ");

        assertEquals("help", line.name());
        assertTrue(line.args().isEmpty());
    }

    @Test
    void parsesOptionsAndLowercasesTheirNames() {
        CommandLine line = CommandLine.parse("report --FROM 2026-11-01 --categories \"Еда,Такси\"");

        assertEquals(Map.of("from", "2026-11-01", "categories", "Еда,Такси"), line.options());
        assertTrue(line.args().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankLineIsEmptyCommand(String text) {
        assertTrue(CommandLine.parse(text).isEmpty());
    }

    @Test
    void unclosedQuoteIsRejected() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> CommandLine.parse("add-category \"Еда"));

        assertTrue(e.getMessage().contains("кавычк"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"report --from", "report --from --to 2026-01-01"})
    void optionWithoutValueIsRejected(String text) {
        assertThrows(ValidationException.class, () -> CommandLine.parse(text));
    }

    @Test
    void duplicateOptionIsRejected() {
        assertThrows(ValidationException.class,
                () -> CommandLine.parse("report --from 2026-01-01 --from 2026-02-01"));
    }

    @Test
    void negativeNumberIsArgumentNotOption() {
        assertEquals(List.of("Еда", "-5"), CommandLine.parse("add-income Еда -5").args());
    }

    @Test
    void emptyQuotedArgumentIsKept() {
        assertEquals(List.of("Еда", ""), CommandLine.parse("rename-category Еда \"\"").args());
    }

    @Test
    void checkRejectsTooFewArguments() {
        CommandLine line = CommandLine.parse("add-category");

        ValidationException e = assertThrows(ValidationException.class,
                () -> line.check(1, 1, Set.of(), "add-category <название>"));

        assertTrue(e.getMessage().contains("add-category <название>"));
    }

    @Test
    void checkRejectsTooManyArguments() {
        CommandLine line = CommandLine.parse("add-category Еда Такси");

        assertThrows(ValidationException.class,
                () -> line.check(1, 1, Set.of(), "add-category <название>"));
    }

    @Test
    void checkRejectsUnknownOptionAndListsAllowedOnes() {
        CommandLine line = CommandLine.parse("report --foo x");

        ValidationException e = assertThrows(ValidationException.class,
                () -> line.check(0, 0, Set.of("from", "to"), "report"));

        assertTrue(e.getMessage().contains("--foo"));
        assertTrue(e.getMessage().contains("--from"));
    }

    @Test
    void checkAcceptsValidShape() {
        CommandLine line = CommandLine.parse("report --from 2026-11-01");

        assertDoesNotThrow(() -> line.check(0, 0, Set.of("from", "to"), "report"));
    }

    @Test
    void optionDateParsesIsoDate() {
        CommandLine line = CommandLine.parse("report --from 2026-11-30");

        assertEquals(Optional.of(LocalDate.of(2026, 11, 30)), line.optionDate("from"));
    }

    @Test
    void missingOptionDateIsEmpty() {
        assertTrue(CommandLine.parse("report").optionDate("from").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"01.11.2026", "2026-02-30"})
    void invalidOptionDateIsRejectedWithFormatHint(String value) {
        CommandLine line = CommandLine.parse("report --from " + value);

        ValidationException e = assertThrows(ValidationException.class,
                () -> line.optionDate("from"));

        assertTrue(e.getMessage().contains("--from"));
        assertTrue(e.getMessage().contains("ГГГГ-ММ-ДД"));
    }
}
