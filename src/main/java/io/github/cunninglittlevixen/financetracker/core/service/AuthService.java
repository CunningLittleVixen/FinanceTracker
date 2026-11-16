package io.github.cunninglittlevixen.financetracker.core.service;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.repository.UserRepository;
import java.util.Locale;
import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern LOGIN_PATTERN = Pattern.compile("[a-z0-9_]{3,20}");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String LOGIN_FAILED = "Неверный логин или пароль.";

    private final UserRepository users;
    private final PasswordHasher hasher;

    public AuthService(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public User register(String login, String password) {
        String normalized = normalize(login);
        if (normalized.isEmpty()) {
            throw new ValidationException("Логин не указан.");
        }
        if (!LOGIN_PATTERN.matcher(normalized).matches()) {
            throw new ValidationException("Логин должен содержать от 3 до 20 символов: "
                    + "латинские буквы, цифры и знак подчёркивания.");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                    "Пароль должен содержать не меньше " + MIN_PASSWORD_LENGTH + " символов.");
        }
        if (users.findByLogin(normalized).isPresent()) {
            throw new ValidationException("Логин «" + normalized + "» уже занят.");
        }
        User user = new User(normalized, hasher.hash(password));
        users.save(user);
        return user;
    }

    public User login(String login, String password) {
        User user = users.findByLogin(normalize(login))
                .orElseThrow(() -> new ValidationException(LOGIN_FAILED));
        if (!hasher.verify(password, user.passwordHash())) {
            throw new ValidationException(LOGIN_FAILED);
        }
        return user;
    }

    private static String normalize(String login) {
        return login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
    }
}
