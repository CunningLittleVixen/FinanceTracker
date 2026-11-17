package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.WalletContent;
import io.github.cunninglittlevixen.financetracker.core.service.FormatRegistry;
import io.github.cunninglittlevixen.financetracker.core.service.ImportResult;
import io.github.cunninglittlevixen.financetracker.core.service.ImportService;
import io.github.cunninglittlevixen.financetracker.core.service.WalletFormat;
import java.nio.file.Path;
import java.util.Set;

public class ImportCommand implements Command {

    private final FormatRegistry formats;
    private final ImportService importService;

    public ImportCommand(FormatRegistry formats, ImportService importService) {
        this.formats = formats;
        this.importService = importService;
    }

    @Override
    public String name() {
        return "import";
    }

    @Override
    public String usage() {
        return "import <файл.json|файл.csv>";
    }

    @Override
    public String description() {
        return "Добавить данные из файла к текущему кошельку (недостающие категории создаются). "
                + "Повторный импорт того же файла создаст дубликаты";
    }

    @Override
    public String example() {
        return "import backup.json";
    }

    @Override
    public boolean changesData() {
        return true;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(1, 1, Set.of(), usage());
        Path file = DataFiles.path(line.args().get(0));
        WalletFormat format = formats.forFile(file.getFileName().toString());
        WalletContent content = DataFiles.read(format, file);
        ImportResult result = importService.merge(session.wallet(), content);
        if (result.isEmpty()) {
            terminal.println("Файл не содержит данных для импорта.");
            return;
        }
        terminal.println("Импорт завершён: добавлено операций — " + result.operations()
                + ", задано бюджетов — " + result.budgets() + ".");
        if (!result.createdCategories().isEmpty()) {
            terminal.println("Созданы категории: "
                    + String.join(", ", result.createdCategories()) + ".");
        }
    }
}
