package io.github.cunninglittlevixen.financetracker.core.model;

import java.util.List;

public record WalletContent(
        List<Category> categories, List<Budget> budgets, List<Operation> operations) {

    public WalletContent {
        categories = categories == null ? List.of() : List.copyOf(categories);
        budgets = budgets == null ? List.of() : List.copyOf(budgets);
        operations = operations == null ? List.of() : List.copyOf(operations);
    }
}
