package io.github.cunninglittlevixen.financetracker.core.service;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StatisticsService {

    public List<Operation> select(Wallet wallet, OperationFilter filter) {
        return filterOperations(wallet, filter, resolveKeys(wallet, filter.categories()));
    }

    public BigDecimal totalIncome(Wallet wallet, OperationFilter filter) {
        return sum(select(wallet, filter), OperationType.INCOME, null);
    }

    public BigDecimal totalExpense(Wallet wallet, OperationFilter filter) {
        return sum(select(wallet, filter), OperationType.EXPENSE, null);
    }

    // NEW
    public Summary summarize(Wallet wallet, OperationFilter filter) {
        Set<String> keys = resolveKeys(wallet, filter.categories());
        List<Operation> selected = filterOperations(wallet, filter, keys);

        Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();
        List<BudgetStatus> statuses = new ArrayList<>();

        for (Category category : wallet.getCategories()) {
            String name = category.name();
            BigDecimal income = sum(selected, OperationType.INCOME, category.key());
            BigDecimal expense = sum(selected, OperationType.EXPENSE, category.key());
            if (income.signum() > 0) {
                incomeByCategory.put(name, income);
            }
            if (expense.signum() > 0) {
                expenseByCategory.put(name, expense);
            }
            if (keys.isEmpty() || keys.contains(category.key())) {
                wallet.getBudget(name).ifPresent(budget -> statuses.add(new BudgetStatus(
                        name, budget.limit(), expense, budget.limit().subtract(expense))));
            }
        }

        return new Summary(
                sum(selected, OperationType.INCOME, null),
                sum(selected, OperationType.EXPENSE, null),
                incomeByCategory,
                expenseByCategory,
                statuses,
                selected.isEmpty());
    }

    private List<Operation> filterOperations(
            Wallet wallet, OperationFilter filter, Set<String> keys) {
        List<Operation> result = new ArrayList<>();
        for (Operation operation : wallet.getOperations()) {
            if (matches(operation, keys, filter)) {
                result.add(operation);
            }
        }
        return result;
    }

    private BigDecimal sum(List<Operation> operations, OperationType type, String categoryKey) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (Operation operation : operations) {
            boolean categoryMatches = categoryKey == null
                    || new Category(operation.category()).key().equals(categoryKey);
            if (operation.type() == type && categoryMatches) {
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