package io.github.cunninglittlevixen.financetracker.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class OperationTest {

    private static final LocalDate TODAY = LocalDate.now();

    private static Operation expense(String category, BigDecimal amount, LocalDate date) {
        return new Operation(OperationType.EXPENSE, category, amount, date, null, null);
    }

    @Test
    void createsValidOperationWithTwoDecimals() {
        Operation op = expense("Еда", new BigDecimal("300"), TODAY);

        assertEquals(OperationType.EXPENSE, op.type());
        assertEquals(new BigDecimal("300.00"), op.amount());
        assertEquals(TODAY, op.date());
    }

    @Test
    void trimsCategory() {
        assertEquals("Еда", expense("  Еда ", BigDecimal.TEN, TODAY).category());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsBlankCategory(String category) {
        assertThrows(ValidationException.class, () -> expense(category, BigDecimal.TEN, TODAY));
    }

    @Test
    void rejectsNullType() {
        assertThrows(ValidationException.class,
                () -> new Operation(null, "Еда", BigDecimal.TEN, TODAY, null, null));
    }

    @Test
    void rejectsNullDate() {
        assertThrows(ValidationException.class,
                () -> expense("Еда", BigDecimal.TEN, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void rejectsNonPositiveAmount(String amount) {
        assertThrows(ValidationException.class,
                () -> expense("Еда", new BigDecimal(amount), TODAY));
    }

    @Test
    void rejectsFutureDate() {
        assertThrows(ValidationException.class,
                () -> expense("Еда", BigDecimal.TEN, TODAY.plusDays(1)));
    }

    @Test
    void nullDescriptionBecomesEmpty() {
        assertEquals("", expense("Еда", BigDecimal.TEN, TODAY).description());
    }

    @Test
    void operationWithCounterpartyIsTransfer() {
        Operation op = new Operation(
                OperationType.EXPENSE, "Перевод", BigDecimal.TEN, TODAY, "", "boris");

        assertTrue(op.isTransfer());
    }

    @Test
    void operationWithoutCounterpartyIsNotTransfer() {
        assertFalse(expense("Еда", BigDecimal.TEN, TODAY).isTransfer());
    }

    @Test
    void blankCounterpartyBecomesNull() {
        Operation op = new Operation(
                OperationType.INCOME, "Зарплата", BigDecimal.TEN, TODAY, null, "  ");

        assertNull(op.counterparty());
    }
}
