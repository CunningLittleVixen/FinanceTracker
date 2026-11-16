package io.github.cunninglittlevixen.financetracker.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TableFormatter {

    private TableFormatter() {
    }

    public static String format(
            List<String> headers, List<List<String>> rows, Set<Integer> rightAligned) {
        int columns = headers.size();
        int[] widths = new int[columns];
        for (int i = 0; i < columns; i++) {
            widths[i] = headers.get(i).length();
        }
        for (List<String> row : rows) {
            if (row.size() != columns) {
                throw new IllegalArgumentException(
                        "Число ячеек в строке не совпадает с числом столбцов.");
            }
            for (int i = 0; i < columns; i++) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }
        List<String> lines = new ArrayList<>();
        lines.add(formatRow(headers, widths, rightAligned));
        lines.add(separator(widths));
        for (List<String> row : rows) {
            lines.add(formatRow(row, widths, rightAligned));
        }
        return String.join("\n", lines);
    }

    private static String formatRow(List<String> cells, int[] widths, Set<Integer> rightAligned) {
        List<String> padded = new ArrayList<>();
        for (int i = 0; i < widths.length; i++) {
            padded.add(pad(cells.get(i), widths[i], rightAligned.contains(i)));
        }
        return String.join(" | ", padded).stripTrailing();
    }

    private static String pad(String text, int width, boolean alignRight) {
        String spaces = " ".repeat(width - text.length());
        return alignRight ? spaces + text : text + spaces;
    }

    private static String separator(int[] widths) {
        List<String> parts = new ArrayList<>();
        for (int width : widths) {
            parts.add("-".repeat(width));
        }
        return String.join("-+-", parts);
    }
}
