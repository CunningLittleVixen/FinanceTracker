package io.github.cunninglittlevixen.financetracker.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class BudgetTest {

    @Test
    void createsBudgetWithTwoDecimals() {
        Budget budget = new Budget("Еда", new BigDecimal("4000"));

        assertEquals("Еда", budget.category());
        assertEquals(new BigDecimal("4000.00"), budget.limit());
    }

    @Test
    void trimsCategory() {
        assertEquals("Еда", new Budget(" Еда ", BigDecimal.TEN).category());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void rejectsNonPositiveLimit(String limit) {
        assertThrows(ValidationException.class,
                () -> new Budget("Еда", new BigDecimal(limit)));
    }

    @Test
    void rejectsNullLimit() {
        assertThrows(ValidationException.class, () -> new Budget("Еда", null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsBlankCategory(String category) {
        assertThrows(ValidationException.class,
                () -> new Budget(category, BigDecimal.TEN));
    }
}
