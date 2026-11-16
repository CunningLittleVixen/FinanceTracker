package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Money;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.service.NotificationService;
import io.github.cunninglittlevixen.financetracker.core.service.TransferService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class TransferCommand implements Command {

    private final TransferService transfers;
    private final NotificationService notifications;

    public TransferCommand(TransferService transfers, NotificationService notifications) {
        this.transfers = transfers;
        this.notifications = notifications;
    }

    @Override
    public String name() {
        return "transfer";
    }

    @Override
    public String usage() {
        return "transfer <логин> <сумма> [комментарий] [--date ГГГГ-ММ-ДД]";
    }

    @Override
    public String description() {
        return "Перевести деньги другому пользователю (расход у вас, доход у него)";
    }

    @Override
    public String example() {
        return "transfer boris 500 \"за обед\"";
    }

    @Override
    public boolean changesData() {
        return true;
    }

    @Override
    public void execute(CommandLine line, Session session, Terminal terminal) {
        line.check(2, 3, Set.of("date"), usage());
        Wallet wallet = session.wallet();
        BigDecimal amount = Money.parse(line.args().get(1));
        String description = line.args().size() > 2 ? line.args().get(2) : "";
        LocalDate date = line.optionDate("date").orElseGet(LocalDate::now);

        Operation operation = transfers.transfer(wallet, line.args().get(0), amount, date, description);

        terminal.println("Переведено " + MoneyFormatter.format(operation.amount())
                + " пользователю " + operation.counterparty() + ". Баланс: "
                + MoneyFormatter.format(wallet.getBalance()) + ".");
        NotificationPrinter.show(terminal, notifications.afterOperation(wallet, operation));
    }
}
