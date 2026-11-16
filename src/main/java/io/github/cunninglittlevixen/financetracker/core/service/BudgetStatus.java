package io.github.cunninglittlevixen.financetracker.core.service;

import java.math.BigDecimal;

public record BudgetStatus(String category, BigDecimal limit, BigDecimal spent, BigDecimal remaining) {
}
