package io.github.cunninglittlevixen.financetracker.core.exception;

public class CategoryNotFoundException extends ValidationException {

    private final String categoryName;

    public CategoryNotFoundException(String categoryName, String message) {
        super(message);
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
