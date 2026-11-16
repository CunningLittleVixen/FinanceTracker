package io.github.cunninglittlevixen.financetracker;

import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.infra.Pbkdf2PasswordHasher;
import java.nio.file.Path;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Application application =
                new Application(Path.of("data"), Pbkdf2PasswordHasher.DEFAULT_ITERATIONS);
        try {
            application.run(System.in, System.out);
        } catch (StorageException e) {
            System.err.println("Не удалось запустить приложение: " + e.getMessage());
            System.exit(1);
        }
    }
}