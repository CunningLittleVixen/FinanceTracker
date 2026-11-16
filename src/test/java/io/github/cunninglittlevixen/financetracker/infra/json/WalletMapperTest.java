package io.github.cunninglittlevixen.financetracker.infra.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class WalletMapperTest {

    @Test
    void roundTripKeepsEverything() {
        Wallet original = SampleWallet.create();

        Wallet restored = WalletMapper.toWallet(WalletMapper.toData(original));

        assertEquals(original.getOwner(), restored.getOwner());
        assertEquals(original.getCategories(), restored.getCategories());
        assertEquals(original.getBudgets(), restored.getBudgets());
        assertEquals(original.getOperations(), restored.getOperations());
        assertEquals(original.getBalance(), restored.getBalance());
    }

    @Test
    void emptyWalletRoundTrip() {
        Wallet restored = WalletMapper.toWallet(WalletMapper.toData(new Wallet("boris")));

        assertEquals("boris", restored.getOwner());
        assertTrue(restored.getOperations().isEmpty());
    }

    @Test
    void toDataUsesPlainValues() {
        WalletData data = WalletMapper.toData(SampleWallet.create());

        assertEquals(List.of("Еда", "Зарплата", "Перевод"), data.categories());
        assertEquals(3, data.operations().size());
        assertEquals("boris", data.operations().get(2).counterparty());
    }

    @Test
    void missingListsAreTreatedAsEmpty() {
        Wallet wallet = WalletMapper.toWallet(new WalletData("anna", null, null, null));

        assertTrue(wallet.getCategories().isEmpty());
        assertTrue(wallet.getOperations().isEmpty());
    }

    @Test
    void invalidDataIsRejectedByModelRules() {
        WalletData data = new WalletData("anna", List.of("Еда"), List.of(), List.of(
                new WalletData.OperationData(null, "Кино", new BigDecimal("10"),
                        LocalDate.of(2020, 1, 1), null, null)));

        assertThrows(ValidationException.class, () -> WalletMapper.toWallet(data));
    }
}
