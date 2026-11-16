package io.github.cunninglittlevixen.financetracker.cli;

import java.math.BigDecimal;
import java.util.Locale;

public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String format(BigDecimal value) {
        return String.format(Locale.ROOT, "%,.2f", value);
    }
}
