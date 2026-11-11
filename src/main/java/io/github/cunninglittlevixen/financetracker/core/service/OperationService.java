package io.github.cunninglittlevixen.financetracker.core.service;

import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;

public class OperationService {

    public Operation addIncome(
            Wallet wallet, String category, BigDecimal amount, LocalDate date, String description) {
        return add(wallet, OperationType.INCOME, category, amount, date, description);
    }

    public Operation addExpense(
            Wallet wallet, String category, BigDecimal amount, LocalDate date, String description) {
        return add(wallet, OperationType.EXPENSE, category, amount, date, description);
    }

    private Operation add(
            Wallet wallet,
            OperationType type,
            String category,
            BigDecimal amount,
            LocalDate date,
            String description) {
        Operation operation = new Operation(type, category, amount, date, description, null);
        wallet.addOperation(operation);
        return operation;
    }
}