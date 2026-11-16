package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.service.Notification;
import io.github.cunninglittlevixen.financetracker.core.service.NotificationService;
import io.github.cunninglittlevixen.financetracker.core.service.OperationFilter;
import io.github.cunninglittlevixen.financetracker.core.service.StatisticsService;
import io.github.cunninglittlevixen.financetracker.core.service.Summary;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReportFormatterTest {

    private final StatisticsService statistics = new StatisticsService();
    private final NotificationService notifications = new NotificationService(statistics);
    private final Wallet wallet = new Wallet("anna");

    @BeforeEach
    void setUp() {
        SampleData.fillAssignmentExample(wallet);
    }

    private String report(OperationFilter filter, boolean withAlerts) {
        Summary summary = statistics.summarize(wallet, filter);
        List<Notification> alerts = withAlerts ? notifications.currentState(wallet) : List.of();
        return ReportFormatter.format("anna", filter, summary, alerts);
    }

    @Test
    void showsTotalsAndCategoryTables() {
        String text = report(OperationFilter.all(), false);

        assertTrue(text.contains("Отчёт: anna"));
        assertTrue(text.contains("Общий доход: 63,000.00"));
        assertTrue(text.contains("Общие расходы: 8,300.00"));
        assertTrue(text.contains("Зарплата"));
        assertTrue(text.contains("60,000.00"));
        assertTrue(text.contains("Бонус"));
        assertTrue(text.contains("Такси"));
        assertTrue(text.contains("1,500.00"));
    }

    @Test
    void showsBudgetTableWithRemainingLimit() {
        String text = report(OperationFilter.all(), false);

        assertTrue(text.contains("Бюджет по категориям:"));
        assertTrue(text.contains("Остаток"));
        assertTrue(text.contains("3,200.00"));
        assertTrue(text.contains("-500.00"));
    }

    @Test
    void headerShowsSelectedCategoriesAndPeriod() {
        OperationFilter filter = new OperationFilter(
                List.of("Еда"), LocalDate.of(2020, 2, 1), LocalDate.of(2020, 2, 28));

        String text = report(filter, false);

        assertTrue(text.contains("Категории: Еда"));
        assertTrue(text.contains("Период: с 2020-02-01 по 2020-02-28"));
    }

    @Test
    void headerShowsOpenEndedPeriods() {
        assertTrue(report(new OperationFilter(null, LocalDate.of(2020, 2, 15), null), false)
                .contains("Период: с 2020-02-15"));
        assertTrue(report(new OperationFilter(null, null, LocalDate.of(2020, 1, 31)), false)
                .contains("Период: по 2020-01-31"));
    }

    @Test
    void emptySelectionSaysSoButKeepsBudgets() {
        String text = report(new OperationFilter(null, LocalDate.of(2021, 1, 1), null), false);

        assertTrue(text.contains("Операций по выбранным условиям не найдено."));
        assertTrue(text.contains("Бюджет по категориям:"));
        assertFalse(text.contains("Общий доход"));
    }

    @Test
    void alertsAreListedWhenPresent() {
        String text = report(OperationFilter.all(), true);

        assertTrue(text.contains("Оповещения"));
        assertTrue(text.contains("Превышен бюджет"));
    }

    @Test
    void noAlertsSectionWhenThereAreNone() {
        assertFalse(report(OperationFilter.all(), false).contains("Оповещения"));
    }
}