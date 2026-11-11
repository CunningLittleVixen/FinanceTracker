package io.github.cunninglittlevixen.financetracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void greetingReturnsExpectedText() {
        assertEquals("FinanceTracker запущен", Main.greeting());
    }
}