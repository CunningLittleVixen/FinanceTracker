package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.service.BudgetStatus;
import io.github.cunninglittlevixen.financetracker.core.service.Notification;
import io.github.cunninglittlevixen.financetracker.core.service.OperationFilter;
import io.github.cunninglittlevixen.financetracker.core.service.Summary;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class ReportFormatter {

    private ReportFormatter() {
    }

    static String format(
            String owner, OperationFilter filter, Summary summary, List<Notification> notifications) {
        List<String> lines = new ArrayList<>();
        lines.add("Отчёт: " + owner);
        if (!filter.categories().isEmpty()) {
            lines.add("Категории: " + String.join(", ", filter.categories()));
        }
        periodLine(filter).ifPresent(lines::add);
        lines.add("");

        if (summary.empty()) {
            lines.add("Операций по выбранным условиям не найдено.");
        } else {
            lines.add("Общий доход: " + MoneyFormatter.format(summary.totalIncome()));
            addCategoryTable(lines, "Доходы по категориям:", summary.incomeByCategory());
            lines.add("");
            lines.add("Общие расходы: " + MoneyFormatter.format(summary.totalExpense()));
            addCategoryTable(lines, "Расходы по категориям:", summary.expenseByCategory());
        }

        if (!summary.budgets().isEmpty()) {
            lines.add("");
            lines.add("Бюджет по категориям:");
            lines.add(indent(budgetTable(summary.budgets())));
        }
        if (!notifications.isEmpty()) {
            lines.add("");
            lines.add("Оповещения (по всему кошельку):");
            for (Notification notification : notifications) {
                lines.add("  [!] " + notification.message());
            }
        }
        return String.join("\n", lines);
    }

    private static java.util.Optional<String> periodLine(OperationFilter filter) {
        if (filter.from() != null && filter.to() != null) {
            return java.util.Optional.of("Период: с " + filter.from() + " по " + filter.to());
        }
        if (filter.from() != null) {
            return java.util.Optional.of("Период: с " + filter.from());
        }
        if (filter.to() != null) {
            return java.util.Optional.of("Период: по " + filter.to());
        }
        return java.util.Optional.empty();
    }

    private static void addCategoryTable(
            List<String> lines, String title, Map<String, BigDecimal> amounts) {
        if (amounts.isEmpty()) {
            return;
        }
        List<List<String>> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : amounts.entrySet()) {
            rows.add(List.of(entry.getKey(), MoneyFormatter.format(entry.getValue())));
        }
        lines.add(title);
        lines.add(indent(TableFormatter.format(List.of("Категория", "Сумма"), rows, Set.of(1))));
    }

    private static String budgetTable(List<BudgetStatus> budgets) {
        List<List<String>> rows = new ArrayList<>();
        for (BudgetStatus status : budgets) {
            rows.add(List.of(status.category(), MoneyFormatter.format(status.limit()),
                    MoneyFormatter.format(status.spent()),
                    MoneyFormatter.format(status.remaining())));
        }
        return TableFormatter.format(
                List.of("Категория", "Бюджет", "Потрачено", "Остаток"), rows, Set.of(1, 2, 3));
    }

    private static String indent(String text) {
        return text.lines().map(line -> "  " + line).collect(Collectors.joining("\n"));
    }
}
