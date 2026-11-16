package io.github.cunninglittlevixen.financetracker.core.service;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;

public record OperationFilter(List<String> categories, LocalDate from, LocalDate to) {

    public OperationFilter {
        categories = categories == null ? List.of() : List.copyOf(categories);
        if (categories.stream().anyMatch(name -> name == null || name.isBlank())) {
            throw new ValidationException("Название категории в выборке не указано.");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException(
                    "Начало периода (" + from + ") позже его конца (" + to + ").");
        }
    }

    public static OperationFilter all() {
        return new OperationFilter(null, null, null);
    }
}
