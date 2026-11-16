package io.github.cunninglittlevixen.financetracker.infra.json;

import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WalletData(
        String owner,
        List<String> categories,
        List<BudgetData> budgets,
        List<OperationData> operations) {

    public record BudgetData(String category, BigDecimal limit) {
    }

    public record OperationData(
            OperationType type,
            String category,
            BigDecimal amount,
            LocalDate date,
            String description,
            String counterparty) {
    }
}
