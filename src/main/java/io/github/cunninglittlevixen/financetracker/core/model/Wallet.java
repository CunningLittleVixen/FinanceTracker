package io.github.cunninglittlevixen.financetracker.core.model;

import io.github.cunninglittlevixen.financetracker.core.exception.CategoryNotFoundException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Wallet {

    private final String owner;
    private final List<Operation> operations = new ArrayList<>();
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private final Map<String, Budget> budgets = new LinkedHashMap<>();

    public Wallet(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new ValidationException("Логин владельца кошелька не указан.");
        }
        this.owner = owner.trim();
    }

    public String getOwner() {
        return owner;
    }

    public void addCategory(Category category) {
        if (categories.putIfAbsent(category.key(), category) != null) {
            throw new ValidationException("Категория «" + category.name() + "» уже существует.");
        }
    }

    public boolean hasCategory(String name) {
        return name != null && !name.isBlank() && categories.containsKey(keyOf(name));
    }

    public void renameCategory(String oldName, String newName) {
        requireCategory(oldName);
        Category renamed = new Category(newName);
        String oldKey = keyOf(oldName);
        if (!renamed.key().equals(oldKey) && categories.containsKey(renamed.key())) {
            throw new ValidationException("Категория «" + renamed.name() + "» уже существует.");
        }

        Map<String, Category> updated = new LinkedHashMap<>();
        for (Map.Entry<String, Category> entry : categories.entrySet()) {
            if (entry.getKey().equals(oldKey)) {
                updated.put(renamed.key(), renamed);
            } else {
                updated.put(entry.getKey(), entry.getValue());
            }
        }
        categories.clear();
        categories.putAll(updated);

        for (int i = 0; i < operations.size(); i++) {
            Operation op = operations.get(i);
            if (belongsTo(op, oldKey)) {
                operations.set(i, new Operation(op.type(), renamed.name(), op.amount(),
                        op.date(), op.description(), op.counterparty()));
            }
        }

        Budget budget = budgets.remove(oldKey);
        if (budget != null) {
            budgets.put(renamed.key(), new Budget(renamed.name(), budget.limit()));
        }
    }


    public void removeCategory(String name) {
        requireCategory(name);
        String key = keyOf(name);
        long count = operations.stream().filter(op -> belongsTo(op, key)).count();
        if (count > 0) {
            throw new ValidationException("Нельзя удалить категорию «" + categories.get(key).name()
                    + "»: в ней операций: " + count + ".");
        }
        categories.remove(key);
        budgets.remove(key);
    }

    public void addOperation(Operation operation) {
        requireCategory(operation.category());
        operations.add(operation);
    }

    public void setBudget(Budget budget) {
        requireCategory(budget.category());
        budgets.put(keyOf(budget.category()), budget);
    }

    public void removeBudget(String category) {
        requireCategory(category);
        if (budgets.remove(keyOf(category)) == null) {
            throw new ValidationException(
                    "Для категории «" + categories.get(keyOf(category)).name() + "» бюджет не задан.");
        }
    }

    public Optional<Budget> getBudget(String category) {
        return Optional.ofNullable(budgets.get(keyOf(category)));
    }

    public BigDecimal getBalance() {
        BigDecimal balance = BigDecimal.ZERO.setScale(2);
        for (Operation operation : operations) {
            if (operation.type() == OperationType.INCOME) {
                balance = balance.add(operation.amount());
            } else {
                balance = balance.subtract(operation.amount());
            }
        }
        return balance;
    }

    public List<Operation> getOperations() {
        return List.copyOf(operations);
    }

    public List<Category> getCategories() {
        return List.copyOf(categories.values());
    }

    public List<Budget> getBudgets() {
        return List.copyOf(budgets.values());
    }

    private void requireCategory(String name) {
        if (!hasCategory(name)) {
            throw new CategoryNotFoundException(name, notFoundMessage(name));
        }
    }

    private String notFoundMessage(String name) {
        String available = categories.isEmpty()
                ? "Категорий пока нет."
                : "Доступные категории: " + categories.values().stream()
                .map(Category::name)
                .collect(Collectors.joining(", ")) + ".";
        return "Категория «" + name + "» не найдена. " + available;
    }

    private static boolean belongsTo(Operation operation, String key) {
        return keyOf(operation.category()).equals(key);
    }

    private static String keyOf(String name) {
        return new Category(name).key();
    }
}