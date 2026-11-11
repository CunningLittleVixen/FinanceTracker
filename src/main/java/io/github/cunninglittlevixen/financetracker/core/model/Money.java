package io.github.cunninglittlevixen.financetracker.core.model;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    private static final int SCALE = 2;

    private Money() {
    }

    public static BigDecimal parse(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Сумма не указана. Пример: 150.50");
        }
        String cleaned = text.trim().replace(',', '.');
        try {
            return positive(new BigDecimal(cleaned));
        } catch (NumberFormatException e) {
            throw new ValidationException(
                    "Сумма должна быть числом, например 150.50. Вы ввели: " + text.trim());
        }
    }

    public static BigDecimal positive(BigDecimal value) {
        if (value == null) {
            throw new ValidationException("Сумма не указана.");
        }
        BigDecimal rounded = value.setScale(SCALE, RoundingMode.HALF_EVEN);
        if (rounded.signum() <= 0) {
            throw new ValidationException("Сумма должна быть больше нуля.");
        }
        return rounded;
    }
}
