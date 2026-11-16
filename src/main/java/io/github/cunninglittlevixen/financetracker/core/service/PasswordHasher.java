package io.github.cunninglittlevixen.financetracker.core.service;

public interface PasswordHasher {

    String hash(String password);

    boolean verify(String password, String storedHash);
}
