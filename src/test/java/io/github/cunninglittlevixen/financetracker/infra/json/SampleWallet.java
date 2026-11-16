package io.github.cunninglittlevixen.financetracker.infra.json;

import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;

final class SampleWallet {

    private SampleWallet() {
    }

    static Wallet create() {
        Wallet wallet = new Wallet("anna");
        wallet.addCategory(new Category("Еда"));
        wallet.addCategory(new Category("Зарплата"));
        wallet.addCategory(new Category("Перевод"));
        wallet.setBudget(new Budget("Еда", new BigDecimal("4000")));
        wallet.addOperation(new Operation(OperationType.EXPENSE, "Еда", new BigDecimal("300"),
                LocalDate.of(2020, 1, 5), "обед", null));
        wallet.addOperation(new Operation(OperationType.INCOME, "Зарплата",
                new BigDecimal("20000"), LocalDate.of(2020, 1, 1), null, null));
        wallet.addOperation(new Operation(OperationType.EXPENSE, "Перевод", new BigDecimal("500"),
                LocalDate.of(2020, 1, 10), "", "boris"));
        return wallet;
    }
}
