package io.github.cunninglittlevixen.financetracker.core.model;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;

public record Operation(
        OperationType type,
        String category,
        BigDecimal amount,
        LocalDate date,
        String description,
        String counterparty) {

    public Operation {
        if (type == null) {
            throw new ValidationException("Не указан тип операции (доход или расход).");
        }
        if (category == null || category.isBlank()) {
            throw new ValidationException("Категория не указана.");
        }
        category = category.trim();
        amount = Money.positive(amount);
        if (date == null) {
            throw new ValidationException("Дата не указана.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата не может быть в будущем: " + date);
        }
        description = description == null ? "" : description.trim();
        counterparty = counterparty == null || counterparty.isBlank() ? null : counterparty.trim();
    }

    public boolean isTransfer() {
        return counterparty != null;
    }
}
