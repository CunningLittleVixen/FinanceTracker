package io.github.cunninglittlevixen.financetracker.core.service;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StatisticsService {

    public List<Operation> select(Wallet wallet, OperationFilter filter) {
        Set<String> keys = resolveKeys(wallet, filter.categories());
        List<Operation> result = new ArrayList<>();
        for (Operation operation : wallet.getOperations()) {
            if (matches(operation, keys, filter)) {
                result.add(operation);
            }
        }
        return result;
    }

    public BigDecimal totalIncome(Wallet wallet, OperationFilter filter) {
        return total(wallet, filter, OperationType.INCOME);
    }

    public BigDecimal totalExpense(Wallet wallet, OperationFilter filter) {
        return total(wallet, filter, OperationType.EXPENSE);
    }

    private BigDecimal total(Wallet wallet, OperationFilter filter, OperationType type) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (Operation operation : select(wallet, filter)) {
            if (operation.type() == type) {
                sum = sum.add(operation.amount());
            }
        }
        return sum;
    }

    private Set<String> resolveKeys(Wallet wallet, List<String> names) {
        Set<String> keys = new HashSet<>();
        List<String> unknown = new ArrayList<>();
        for (String name : names) {
            if (wallet.hasCategory(name)) {
                keys.add(new Category(name).key());
            } else {
                unknown.add(name.trim());
            }
        }
        if (!unknown.isEmpty()) {
            throw new ValidationException("Категории не найдены: " + String.join(", ", unknown)
                    + ". " + availableCategories(wallet));
        }
        return keys;
    }

    private String availableCategories(Wallet wallet) {
        if (wallet.getCategories().isEmpty()) {
            return "Категорий пока нет.";
        }
        return "Доступные категории: " + wallet.getCategories().stream()
                .map(Category::name)
                .collect(Collectors.joining(", ")) + ".";
    }

    private boolean matches(Operation operation, Set<String> keys, OperationFilter filter) {
        boolean categoryMatches = keys.isEmpty()
                || keys.contains(new Category(operation.category()).key());
        boolean afterStart = filter.from() == null || !operation.date().isBefore(filter.from());
        boolean beforeEnd = filter.to() == null || !operation.date().isAfter(filter.to());
        return categoryMatches && afterStart && beforeEnd;
    }
}