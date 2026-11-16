package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TableFormatterTest {

    @Test
    void alignsColumnsAndRightAlignsNumbers() {
        String table = TableFormatter.format(
                List.of("Категория", "Сумма"),
                List.of(List.of("Еда", "800.00"), List.of("Развлечения", "3,000.00")),
                Set.of(1));

        String expected = String.join("\n",
                "Категория" + " ".repeat(3) + "|" + " ".repeat(4) + "Сумма",
                "-".repeat(12) + "+" + "-".repeat(9),
                "Еда" + " ".repeat(9) + "|" + " ".repeat(3) + "800.00",
                "Развлечения" + " " + "|" + " " + "3,000.00");
        assertEquals(expected, table);
    }

    @Test
    void leftAlignedLastColumnHasNoTrailingSpaces() {
        String table = TableFormatter.format(
                List.of("Категория", "Бюджет", "Статус"),
                List.of(List.of("Еда", "4,000.00", "ok")),
                Set.of(1));

        for (String line : table.split("\n")) {
            assertFalse(line.endsWith(" "));
        }
    }

    @Test
    void tableWithoutRowsHasHeaderAndSeparator() {
        String table = TableFormatter.format(List.of("Имя", "Сумма"), List.of(), Set.of());

        String[] lines = table.split("\n");
        assertEquals(2, lines.length);
        assertEquals("-".repeat(4) + "+" + "-".repeat(6), lines[1]);
    }

    @Test
    void rowWithWrongNumberOfCellsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> TableFormatter.format(
                List.of("А", "Б"), List.of(List.of("только одна")), Set.of()));
    }

    @Test
    void allRowsHaveSameLength() {
        String table = TableFormatter.format(
                List.of("Категория", "Сумма"),
                List.of(List.of("Еда", "800.00"), List.of("Развлечения", "3,000.00")),
                Set.of(1));

        long distinctLengths = table.lines().map(String::length).distinct().count();
        assertTrue(distinctLengths == 1);
    }
}