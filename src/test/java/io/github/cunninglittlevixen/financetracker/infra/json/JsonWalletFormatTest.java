package io.github.cunninglittlevixen.financetracker.infra.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Budget;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.model.WalletContent;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class JsonWalletFormatTest {

    private final JsonWalletFormat format = new JsonWalletFormat(JsonMapperFactory.create());

    private static Wallet wallet() {
        Wallet wallet = new Wallet("anna");
        wallet.addCategory(new Category("Еда"));
        wallet.addCategory(new Category("Перевод"));
        wallet.setBudget(new Budget("Еда", new BigDecimal("4000")));
        wallet.addOperation(new Operation(OperationType.EXPENSE, "Еда", new BigDecimal("300"),
                LocalDate.of(2020, 1, 5), "обед", null));
        wallet.addOperation(new Operation(OperationType.EXPENSE, "Перевод", new BigDecimal("50"),
                LocalDate.of(2020, 2, 1), "за кофе", "boris"));
        return wallet;
    }

    private WalletContent read(String text) throws IOException {
        return format.read(new StringReader(text));
    }

    @Test
    void roundTripKeepsCategoriesBudgetsAndOperations() throws IOException {
        Wallet wallet = wallet();
        StringWriter out = new StringWriter();

        format.write(wallet, out);
        WalletContent content = read(out.toString());

        assertEquals(wallet.getCategories(), content.categories());
        assertEquals(wallet.getBudgets(), content.budgets());
        assertEquals(wallet.getOperations(), content.operations());
    }

    @Test
    void writesReadableJsonWithOwner() throws IOException {
        StringWriter out = new StringWriter();

        format.write(wallet(), out);

        assertTrue(out.toString().contains("\"owner\""));
        assertTrue(out.toString().contains("Еда"));
    }

    @Test
    void brokenJsonIsRejected() {
        assertThrows(ValidationException.class, () -> read("{bad"));
    }

    @Test
    void emptyInputIsRejected() {
        assertThrows(ValidationException.class, () -> read(""));
    }

    @Test
    void snapshotWithoutOwnerIsRejected() {
        ValidationException e = assertThrows(ValidationException.class, () -> read("{}"));

        assertTrue(e.getMessage().contains("не похоже на снимок"));
    }

    @Test
    void operationWithUnknownCategoryIsRejected() {
        String json = "{\"owner\":\"anna\",\"categories\":[\"Еда\"],\"budgets\":[],"
                + "\"operations\":[{\"type\":\"EXPENSE\",\"category\":\"Кино\",\"amount\":10,"
                + "\"date\":\"2020-01-01\",\"description\":\"\",\"counterparty\":null}]}";

        assertThrows(ValidationException.class, () -> read(json));
    }
}
