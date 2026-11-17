package io.github.cunninglittlevixen.financetracker.infra.csv;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Money;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.model.WalletContent;
import io.github.cunninglittlevixen.financetracker.core.service.WalletFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvWalletFormat implements WalletFormat {

    private static final List<String> HEADER =
            List.of("date", "type", "category", "amount", "description", "counterparty");

    private record Row(int line, List<String> fields) {
    }

    @Override
    public String extension() {
        return "csv";
    }

    @Override
    public void write(Wallet wallet, Writer out) throws IOException {
        out.write(String.join(",", HEADER) + "\n");
        for (Operation operation : wallet.getOperations()) {
            String counterparty = operation.counterparty() == null ? "" : operation.counterparty();
            out.write(String.join(",",
                    operation.date().toString(),
                    operation.type().name(),
                    escape(operation.category()),
                    operation.amount().toPlainString(),
                    escape(operation.description()),
                    escape(counterparty)) + "\n");
        }
    }

    @Override
    public WalletContent read(Reader in) throws IOException {
        String text = new BufferedReader(in).lines().collect(Collectors.joining("\n"));
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        List<Row> rows = parse(text).stream().filter(row -> !isBlank(row)).toList();
        if (rows.isEmpty()) {
            throw new ValidationException(
                    "Файл пуст: первой строкой должен быть заголовок " + String.join(",", HEADER) + ".");
        }
        checkHeader(rows.get(0));

        List<Operation> operations = new ArrayList<>();
        Map<String, Category> categories = new LinkedHashMap<>();
        for (Row row : rows.subList(1, rows.size())) {
            Operation operation = toOperation(row);
            operations.add(operation);
            Category category = new Category(operation.category());
            categories.putIfAbsent(category.key(), category);
        }
        return new WalletContent(List.copyOf(categories.values()), List.of(), operations);
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")
                || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static List<Row> parse(String text) {
        List<Row> rows = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int line = 1;
        int rowStart = 1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    if (c == '\n') {
                        line++;
                    }
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else if (c == '\n') {
                fields.add(field.toString());
                field.setLength(0);
                rows.add(new Row(rowStart, fields));
                fields = new ArrayList<>();
                line++;
                rowStart = line;
            } else {
                field.append(c);
            }
        }
        if (inQuotes) {
            throw new ValidationException("Не закрыта кавычка в строке " + rowStart + ".");
        }
        if (field.length() > 0 || !fields.isEmpty()) {
            fields.add(field.toString());
            rows.add(new Row(rowStart, fields));
        }
        return rows;
    }

    private static boolean isBlank(Row row) {
        return row.fields().size() == 1 && row.fields().get(0).isBlank();
    }

    private static void checkHeader(Row row) {
        List<String> actual = row.fields().stream()
                .map(name -> name.trim().toLowerCase(Locale.ROOT))
                .toList();
        if (!actual.equals(HEADER)) {
            throw new ValidationException(
                    "Первая строка должна быть заголовком: " + String.join(",", HEADER) + ".");
        }
    }

    private static Operation toOperation(Row row) {
        List<String> f = row.fields();
        if (f.size() != HEADER.size()) {
            throw error(row, "ожидается " + HEADER.size() + " полей, найдено " + f.size() + ".");
        }
        try {
            return new Operation(parseType(f.get(1)), f.get(2), Money.parse(f.get(3)),
                    parseDate(f.get(0)), f.get(4), f.get(5));
        } catch (ValidationException e) {
            throw error(row, e.getMessage());
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    "дата должна быть в формате ГГГГ-ММ-ДД, получено: " + text);
        }
    }

    private static OperationType parseType(String text) {
        try {
            return OperationType.valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("тип должен быть INCOME или EXPENSE, получено: " + text);
        }
    }

    private static ValidationException error(Row row, String message) {
        return new ValidationException("Строка " + row.line() + ": " + message);
    }
}
