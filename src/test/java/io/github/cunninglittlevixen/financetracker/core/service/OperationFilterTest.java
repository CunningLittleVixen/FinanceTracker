package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class OperationFilterTest {

    @Test
    void allHasNoCategoriesAndNoPeriod() {
        OperationFilter filter = OperationFilter.all();

        assertTrue(filter.categories().isEmpty());
    }

    @Test
    void rejectsPeriodWhereStartIsAfterEnd() {
        assertThrows(ValidationException.class, () -> new OperationFilter(
                null, LocalDate.of(2020, 2, 1), LocalDate.of(2020, 1, 1)));
    }

    @Test
    void acceptsSingleDayPeriod() {
        LocalDate day = LocalDate.of(2020, 1, 1);

        assertDoesNotThrow(() -> new OperationFilter(null, day, day));
    }

    @Test
    void rejectsBlankCategoryName() {
        assertThrows(ValidationException.class,
                () -> new OperationFilter(List.of("Еда", " "), null, null));
    }
}
