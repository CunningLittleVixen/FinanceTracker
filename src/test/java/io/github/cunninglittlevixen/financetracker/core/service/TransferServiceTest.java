package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Category;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;
import io.github.cunninglittlevixen.financetracker.infra.InMemoryUserRepository;
import io.github.cunninglittlevixen.financetracker.infra.json.JsonMapperFactory;
import io.github.cunninglittlevixen.financetracker.infra.json.JsonWalletRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TransferServiceTest {

    private static final LocalDate TODAY = LocalDate.now();

    @TempDir
    Path tempDir;

    private JsonWalletRepository wallets;
    private TransferService service;
    private Wallet sender;

    @BeforeEach
    void setUp() {
        wallets = new JsonWalletRepository(tempDir.resolve("wallets"), JsonMapperFactory.create());
        InMemoryUserRepository users = new InMemoryUserRepository();
        users.save(new User("anna", "h"));
        users.save(new User("boris", "h"));
        service = new TransferService(users, wallets);

        sender = new Wallet("anna");
        sender.addCategory(new Category("Зарплата"));
        sender.addOperation(new Operation(OperationType.INCOME, "Зарплата",
                new BigDecimal("1000"), LocalDate.of(2020, 1, 1), null, null));
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private void assertSenderUntouched() {
        assertEquals(money("1000.00"), sender.getBalance());
        assertEquals(1, sender.getOperations().size());
        assertFalse(sender.hasCategory("Перевод"));
    }

    @Test
    void transferMovesMoneyToRecipientFile() {
        Operation expense = service.transfer(sender, "boris", money("300"), TODAY, "за обед");

        Wallet recipient = wallets.findByOwner("boris").orElseThrow();
        assertEquals(OperationType.EXPENSE, expense.type());
        assertEquals(money("700.00"), sender.getBalance());
        assertEquals(money("300.00"), recipient.getBalance());
    }

    @Test
    void operationsPointToEachOther() {
        Operation expense = service.transfer(sender, "boris", money("300"), TODAY, "за обед");

        Operation income = wallets.findByOwner("boris").orElseThrow().getOperations().get(0);
        assertEquals("boris", expense.counterparty());
        assertEquals("anna", income.counterparty());
        assertEquals(OperationType.INCOME, income.type());
        assertEquals(TransferService.TRANSFER_CATEGORY, income.category());
        assertEquals("за обед", income.description());
        assertTrue(sender.hasCategory(TransferService.TRANSFER_CATEGORY));
    }

    @Test
    void recipientLoginIsNormalized() {
        service.transfer(sender, "  Boris ", money("100"), TODAY, null);

        assertTrue(wallets.findByOwner("boris").isPresent());
    }

    @Test
    void unknownRecipientFailsAndChangesNothing() {
        assertThrows(ValidationException.class,
                () -> service.transfer(sender, "nobody", money("100"), TODAY, null));

        assertSenderUntouched();
        assertTrue(wallets.findByOwner("nobody").isEmpty());
    }

    @Test
    void blankRecipientFails() {
        assertThrows(ValidationException.class,
                () -> service.transfer(sender, null, money("100"), TODAY, null));
        assertThrows(ValidationException.class,
                () -> service.transfer(sender, " ", money("100"), TODAY, null));
    }

    @Test
    void transferToSelfFails() {
        assertThrows(ValidationException.class,
                () -> service.transfer(sender, "anna", money("100"), TODAY, null));
        assertThrows(ValidationException.class,
                () -> service.transfer(sender, "ANNA", money("100"), TODAY, null));
        assertSenderUntouched();
    }

    @Test
    void insufficientFundsFailsWithAmountsInMessage() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> service.transfer(sender, "boris", money("1500"), TODAY, null));

        assertTrue(e.getMessage().contains("1000.00"));
        assertTrue(e.getMessage().contains("1500.00"));
        assertSenderUntouched();
        assertTrue(wallets.findByOwner("boris").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5"})
    void nonPositiveAmountFails(String amount) {
        assertThrows(ValidationException.class,
                () -> service.transfer(sender, "boris", money(amount), TODAY, null));
        assertSenderUntouched();
    }

    @Test
    void futureDateFailsAndChangesNothing() {
        assertThrows(ValidationException.class,
                () -> service.transfer(sender, "boris", money("100"), TODAY.plusDays(1), null));

        assertSenderUntouched();
        assertTrue(wallets.findByOwner("boris").isEmpty());
    }

    @Test
    void existingRecipientWalletKeepsOldData() {
        Wallet boris = new Wallet("boris");
        boris.addCategory(new Category("Еда"));
        boris.addOperation(new Operation(OperationType.EXPENSE, "Еда", money("50"),
                LocalDate.of(2020, 1, 2), null, null));
        wallets.save(boris);

        service.transfer(sender, "boris", money("100"), TODAY, null);

        Wallet reloaded = wallets.findByOwner("boris").orElseThrow();
        assertEquals(2, reloaded.getOperations().size());
        assertTrue(reloaded.hasCategory("Еда"));
        assertEquals(money("50.00"), reloaded.getBalance());
    }

    @Test
    void failedRecipientSaveLeavesSenderUntouched() {
        WalletRepository failing = new WalletRepository() {
            @Override
            public Optional<Wallet> findByOwner(String login) {
                return Optional.empty();
            }

            @Override
            public void save(Wallet wallet) {
                throw new StorageException("диск недоступен");
            }
        };
        InMemoryUserRepository users = new InMemoryUserRepository();
        users.save(new User("boris", "h"));
        TransferService failingService = new TransferService(users, failing);

        assertThrows(StorageException.class,
                () -> failingService.transfer(sender, "boris", money("100"), TODAY, null));

        assertSenderUntouched();
    }
}
