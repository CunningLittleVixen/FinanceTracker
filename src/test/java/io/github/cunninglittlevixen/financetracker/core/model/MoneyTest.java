package io.github.cunninglittlevixen.financetracker.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MoneyTest {

    @Test
    void parseAcceptsDot() {
        assertEquals(new BigDecimal("19.99"), Money.parse("19.99"));
    }

    @Test
    void parseAcceptsComma() {
        assertEquals(new BigDecimal("19.99"), Money.parse("19,99"));
    }

    @Test
    void parseTrimsSpacesAndAddsScale() {
        assertEquals(new BigDecimal("5.00"), Money.parse("  5 "));
    }

    @ParameterizedTest
    @CsvSource({"10.015, 10.02", "10.025, 10.02"})
    void parseRoundsHalfEven(String input, String expected) {
        assertEquals(new BigDecimal(expected), Money.parse(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "abc", "0", "-5", "0.001"})
    void parseRejectsInvalidInput(String input) {
        assertThrows(ValidationException.class, () -> Money.parse(input));
    }

    @Test
    void parseErrorMentionsUserInput() {
        ValidationException e = assertThrows(ValidationException.class, () -> Money.parse("abc"));
        assertTrue(e.getMessage().contains("abc"));
    }

    @Test
    void positiveAddsScale() {
        assertEquals(new BigDecimal("5.00"), Money.positive(new BigDecimal("5")));
    }

    @Test
    void positiveRejectsNull() {
        assertThrows(ValidationException.class, () -> Money.positive(null));
    }
}
