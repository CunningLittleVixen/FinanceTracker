package io.github.cunninglittlevixen.financetracker.cli;

public class InputEndedException extends RuntimeException {

    public InputEndedException() {
        super("Ввод завершён.");
    }
}
