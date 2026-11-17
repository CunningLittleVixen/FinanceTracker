package io.github.cunninglittlevixen.financetracker.core.service;

import java.util.List;

public record ImportResult(List<String> createdCategories, int budgets, int operations) {

    public ImportResult {
        createdCategories = List.copyOf(createdCategories);
    }

    public boolean isEmpty() {
        return createdCategories.isEmpty() && budgets == 0 && operations == 0;
    }
}
