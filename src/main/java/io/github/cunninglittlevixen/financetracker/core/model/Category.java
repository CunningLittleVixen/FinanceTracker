package io.github.cunninglittlevixen.financetracker.core.model;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.util.Locale;

public record Category(String name) {

    public Category {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название категории не указано.");
        }
        name = name.trim();
    }

    public String key() {
        return name.toLowerCase(Locale.ROOT);
    }
}
