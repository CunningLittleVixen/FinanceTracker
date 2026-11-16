package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.service.OperationFilter;
import io.github.cunninglittlevixen.financetracker.core.service.StatisticsService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class HistoryCommand implements Command {

    private final StatisticsService statistics;

    public HistoryCommand(StatisticsService statistics) {
        this.statistics = statistics;
    }

    @Override
    public String name() {
        return "history";
    }

    @Override
    public String usage() {
        return "history [--categories \"Еда,Такси\"] [--from ГГГГ-ММ-ДД] [--to ГГГГ-ММ-ДД]"
                + " [--out файл]";
    }

    @Override
    public String description() {
        return "Список операций по датам; можно выбрать категории и период и сохранить в файл";
    }

    @Override
    public String example() {
        return "history --from 2026-11-01";
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(0, 0, ReportOptions.ALLOWED, usage());
        OperationFilter filter = ReportOptions.filter(line);
        List<Operation> operations = statistics.select(session.wallet(), filter).stream()
                .sorted(Comparator.comparing(Operation::date))
                .toList();
        ReportOptions.deliver(line, format(operations), terminal);
    }

    private static String format(List<Operation> operations) {
        if (operations.isEmpty()) {
            return "Операций не найдено.";
        }
        List<List<String>> rows = new ArrayList<>();
        for (Operation operation : operations) {
            rows.add(List.of(
                    operation.date().toString(),
                    operation.type() == OperationType.INCOME ? "Доход" : "Расход",
                    operation.category(),
                    MoneyFormatter.format(operation.amount()),
                    transferText(operation),
                    operation.description()));
        }
        String table = TableFormatter.format(
                List.of("Дата", "Тип", "Категория", "Сумма", "Перевод", "Описание"),
                rows, Set.of(3));
        return table + "\nВсего операций: " + operations.size() + ".";
    }

    private static String transferText(Operation operation) {
        if (!operation.isTransfer()) {
            return "";
        }
        return (operation.type() == OperationType.EXPENSE ? "кому: " : "от: ")
                + operation.counterparty();
    }
}
