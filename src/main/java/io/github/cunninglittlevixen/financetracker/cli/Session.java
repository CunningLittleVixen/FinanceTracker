package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;

public class Session {

    private User user;
    private Wallet wallet;
    private boolean running = true;

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void login(User user, Wallet wallet) {
        this.user = user;
        this.wallet = wallet;
    }

    public void logout() {
        user = null;
        wallet = null;
    }

    public User user() {
        requireLoggedIn();
        return user;
    }

    public Wallet wallet() {
        requireLoggedIn();
        return wallet;
    }

    private void requireLoggedIn() {
        if (user == null) {
            throw new ValidationException("Сначала войдите в систему: login или register.");
        }
    }
}
