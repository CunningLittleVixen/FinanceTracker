package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.service.OperationFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class ReportOptions {

    static final Set<String> ALLOWED = Set.of("categories", "from", "to", "out");

    private ReportOptions() {
    }

    static OperationFilter filter(CommandLine line) {
        List<String> categories = line.option("categories")
                .map(text -> Arrays.stream(text.split(",", -1)).map(String::trim).toList())
                .orElse(List.of());
        LocalDate from = line.optionDate("from").orElse(null);
        LocalDate to = line.optionDate("to").orElse(null);
        return new OperationFilter(categories, from, to);
    }

    static void deliver(CommandLine line, String text, Terminal terminal) {
        Optional<String> out = line.option("out");
        if (out.isEmpty()) {
            terminal.println(text);
            return;
        }
        Path path;
        try {
            path = Path.of(out.get());
        } catch (InvalidPathException e) {
            throw new ValidationException("Недопустимое имя файла: " + out.get());
        }
        try {
            Files.writeString(path, text + "\n", StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
        } catch (FileAlreadyExistsException e) {
            throw new ValidationException("Файл «" + path + "» уже существует. "
                    + "Укажите другое имя через --out.");
        } catch (IOException e) {
            throw new StorageException("Не удалось записать файл «" + path
                    + "». Проверьте, что папка существует.", e);
        }
        terminal.println("Сохранено в файл: " + path);
    }
}
