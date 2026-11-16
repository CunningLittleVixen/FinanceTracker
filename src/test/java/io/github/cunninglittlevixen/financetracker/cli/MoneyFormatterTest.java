package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyFormatterTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "63000.00|63,000.00",
            "8300.00|8,300.00",
            "-500.00|-500.00",
            "0.00|0.00",
            "1234567.80|1,234,567.80"})
    void formatsWithGroupingAndTwoDecimals(String input, String expected) {
        assertEquals(expected, MoneyFormatter.format(new BigDecimal(input)));
    }
}
