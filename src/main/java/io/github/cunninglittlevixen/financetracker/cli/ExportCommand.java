package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.service.FormatRegistry;
import io.github.cunninglittlevixen.financetracker.core.service.WalletFormat;
import java.nio.file.Path;
import java.util.Set;

public class ExportCommand implements Command {

    private final FormatRegistry formats;

    public ExportCommand(FormatRegistry formats) {
        this.formats = formats;
    }

    @Override
    public String name() {
        return "export";
    }

    @Override
    public String usage() {
        return "export <файл.json|файл.csv>";
    }

    @Override
    public String description() {
        return "Сохранить данные в файл: .json — полный снимок кошелька, .csv — только операции. "
                + "Существующий файл не перезаписывается";
    }

    @Override
    public String example() {
        return "export backup.json";
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(1, 1, Set.of(), usage());
        Wallet wallet = session.wallet();
        Path file = DataFiles.path(line.args().get(0));
        WalletFormat format = formats.forFile(file.getFileName().toString());
        DataFiles.write(format, wallet, file);
        terminal.println("Данные сохранены в файл " + file + " (операций: "
                + wallet.getOperations().size() + ").");
    }
}
