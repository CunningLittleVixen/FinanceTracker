package io.github.cunninglittlevixen.financetracker.infra.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CsvWalletFormatTest {

    private static final String HEADER = "date,type,category,amount,description,counterparty";

    private final CsvWalletFormat format = new CsvWalletFormat();

    private static Wallet wallet() {
        Wallet wallet = new Wallet("anna");
        wallet.addCategory(new Category("Еда"));
        wallet.addCategory(new Category("Зарплата"));
        wallet.addCategory(new Category("Перевод"));
        return wallet;
    }

    private static Operation op(OperationType type, String category, String amount, int month,
                                int day, String description, String counterparty) {
        return new Operation(type, category, new BigDecimal(amount),
                LocalDate.of(2020, month, day), description, counterparty);
    }

    private String write(Wallet wallet) throws IOException {
        StringWriter out = new StringWriter();
        format.write(wallet, out);
        return out.toString();
    }

    private WalletContent read(String text) throws IOException {
        return format.read(new StringReader(text));
    }

    @Test
    void writesHeaderAndOneRowPerOperation() throws IOException {
        Wallet wallet = wallet();
        wallet.addOperation(op(OperationType.EXPENSE, "Еда", "300", 1, 5, "обед", null));
        wallet.addOperation(op(OperationType.INCOME, "Зарплата", "20000", 1, 1, null, null));

        assertEquals(HEADER + "\n"
                + "2020-01-05,EXPENSE,Еда,300.00,обед,\n"
                + "2020-01-01,INCOME,Зарплата,20000.00,,\n", write(wallet));
    }

    @Test
    void quotesFieldsWithCommasQuotesAndNewlines() throws IOException {
        Wallet wallet = wallet();
        wallet.addOperation(op(OperationType.EXPENSE, "Еда", "10", 1, 5, "a, \"b\"\nc", null));

        assertTrue(write(wallet).contains("2020-01-05,EXPENSE,Еда,10.00,\"a, \"\"b\"\"\nc\","));
    }

    @Test
    void roundTripKeepsOperations() throws IOException {
        Wallet wallet = wallet();
        wallet.addOperation(op(OperationType.EXPENSE, "Еда", "300", 1, 5, "обед", null));
        wallet.addOperation(op(OperationType.INCOME, "Зарплата", "20000", 1, 1, null, null));
        wallet.addOperation(op(OperationType.EXPENSE, "Перевод", "50", 2, 1, "a, \"b\"\nc", "boris"));

        WalletContent content = read(write(wallet));

        assertEquals(wallet.getOperations(), content.operations());
    }

    @Test
    void readCollectsCategoriesIgnoringCaseAndKeepingFirstSpelling() throws IOException {
        WalletContent content = read(HEADER + "\n"
                + "2020-01-05,EXPENSE,Еда,100,,\n"
                + "2020-01-06,EXPENSE,еда,200,,\n"
                + "2020-01-07,INCOME,Зарплата,300,,\n");

        assertEquals(java.util.List.of(new Category("Еда"), new Category("Зарплата")),
                content.categories());
        assertEquals(3, content.operations().size());
    }

    @Test
    void readAcceptsBomWindowsLineEndingsBlankLinesAndLowercaseType() throws IOException {
        WalletContent content = read("\uFEFF" + HEADER + "\r\n\r\n"
                + "2020-01-05,expense,Еда,\"300,50\",,\r\n");

        assertEquals(1, content.operations().size());
        assertEquals(new BigDecimal("300.50"), content.operations().get(0).amount());
        assertEquals(OperationType.EXPENSE, content.operations().get(0).type());
    }

    @Test
    void missingHeaderIsRejected() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> read("2020-01-05,EXPENSE,Еда,100,,\n"));

        assertTrue(e.getMessage().contains("Первая строка должна быть заголовком"));
    }

    @Test
    void emptyFileIsRejected() {
        assertThrows(ValidationException.class, () -> read(""));
    }

    @Test
    void wrongNumberOfFieldsReportsLineAndCounts() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> read(HEADER + "\n2020-01-05,EXPENSE,Еда\n"));

        assertTrue(e.getMessage().contains("Строка 2"));
        assertTrue(e.getMessage().contains("6"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "05.01.2020,EXPENSE,Еда,300,,",
            "2020-01-05,SPEND,Еда,300,,",
            "2020-01-05,EXPENSE,Еда,abc,,",
            "2020-01-05,EXPENSE,Еда,0,,",
            "2020-01-05,EXPENSE,,300,,",
            "2999-01-05,EXPENSE,Еда,300,,"})
    void invalidRowIsRejectedWithLineNumber(String row) {
        ValidationException e = assertThrows(ValidationException.class,
                () -> read(HEADER + "\n" + row + "\n"));

        assertTrue(e.getMessage().contains("Строка 2"));
    }

    @Test
    void unclosedQuoteIsRejected() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> read(HEADER + "\n2020-01-05,EXPENSE,\"Еда,300,,\n"));

        assertTrue(e.getMessage().contains("кавычк"));
    }

    @Test
    void lineNumbersCountNewlinesInsideQuotedFields() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> read(HEADER + "\n"
                        + "2020-01-05,EXPENSE,Еда,300,\"x\ny\",\n"
                        + "bad,EXPENSE,Еда,300,,\n"));

        assertTrue(e.getMessage().contains("Строка 4"));
    }

    @Test
    void extensionIsCsv() {
        assertEquals("csv", format.extension());
    }
}
