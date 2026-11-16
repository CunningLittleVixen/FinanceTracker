package io.github.cunninglittlevixen.financetracker.core.service;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Money;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.repository.UserRepository;
import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransferService {

    public static final String TRANSFER_CATEGORY = "Перевод";

    private final UserRepository users;
    private final WalletRepository wallets;

    public TransferService(UserRepository users, WalletRepository wallets) {
        this.users = users;
        this.wallets = wallets;
    }

    public Operation transfer(Wallet sender, String recipientLogin, BigDecimal amount,
                              LocalDate date, String description) {
        String recipient = User.normalizeLogin(recipientLogin);
        if (recipient.isEmpty()) {
            throw new ValidationException("Логин получателя не указан.");
        }
        if (recipient.equalsIgnoreCase(sender.getOwner())) {
            throw new ValidationException("Нельзя переводить деньги самому себе.");
        }
        if (users.findByLogin(recipient).isEmpty()) {
            throw new ValidationException("Пользователь «" + recipient + "» не найден.");
        }
        BigDecimal money = Money.positive(amount);
        if (sender.getBalance().compareTo(money) < 0) {
            throw new ValidationException("Недостаточно средств: на балансе "
                    + sender.getBalance().toPlainString() + ", а для перевода нужно "
                    + money.toPlainString() + ".");
        }

        Operation expense = new Operation(
                OperationType.EXPENSE, TRANSFER_CATEGORY, money, date, description, recipient);
        Operation income = new Operation(
                OperationType.INCOME, TRANSFER_CATEGORY, money, date, description, sender.getOwner());

        Wallet target = wallets.findByOwner(recipient).orElseGet(() -> new Wallet(recipient));
        ensureTransferCategory(target);
        target.addOperation(income);
        wallets.save(target);

        ensureTransferCategory(sender);
        sender.addOperation(expense);
        return expense;
    }

    private static void ensureTransferCategory(Wallet wallet) {
        if (!wallet.hasCategory(TRANSFER_CATEGORY)) {
            wallet.addCategory(new Category(TRANSFER_CATEGORY));
        }
    }
}
