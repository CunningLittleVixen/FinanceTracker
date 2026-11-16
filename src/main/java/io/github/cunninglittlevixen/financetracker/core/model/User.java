package io.github.cunninglittlevixen.financetracker.core.model;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;

public record User(String login, String passwordHash) {

    public User {
        if (login == null || login.isBlank()) {
            throw new ValidationException("Логин не указан.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidationException("Хеш пароля не указан.");
        }
        login = login.trim();
    }
}
