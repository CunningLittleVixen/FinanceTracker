package io.github.cunninglittlevixen.financetracker.core.service;

import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.model.WalletContent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImportService {

    public ImportResult merge(Wallet wallet, WalletContent content) {
        Map<String, Category> missing = new LinkedHashMap<>();
        List<Category> wanted = new ArrayList<>(content.categories());
        for (Budget budget : content.budgets()) {
            wanted.add(new Category(budget.category()));
        }
        for (Operation operation : content.operations()) {
            wanted.add(new Category(operation.category()));
        }
        for (Category category : wanted) {
            if (!wallet.hasCategory(category.name())) {
                missing.putIfAbsent(category.key(), category);
            }
        }

        for (Category category : missing.values()) {
            wallet.addCategory(category);
        }
        for (Budget budget : content.budgets()) {
            wallet.setBudget(new Budget(canonicalName(wallet, budget.category()), budget.limit()));
        }
        for (Operation op : content.operations()) {
            wallet.addOperation(new Operation(op.type(), canonicalName(wallet, op.category()),
                    op.amount(), op.date(), op.description(), op.counterparty()));
        }

        List<String> created = missing.values().stream().map(Category::name).toList();
        return new ImportResult(created, content.budgets().size(), content.operations().size());
    }

    private static String canonicalName(Wallet wallet, String name) {
        String key = new Category(name).key();
        return wallet.getCategories().stream()
                .filter(category -> category.key().equals(key))
                .findFirst()
                .orElseThrow()
                .name();
    }
}
