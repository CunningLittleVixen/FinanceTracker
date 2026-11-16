package io.github.cunninglittlevixen.financetracker.infra.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class WalletJsonTest {

    private final ObjectMapper mapper = JsonMapperFactory.create();

    @Test
    void writesIsoDatesAndRussianTextAsUtf8() throws Exception {
        WalletData data = WalletMapper.toData(SampleWallet.create());

        String json = new String(mapper.writeValueAsBytes(data), StandardCharsets.UTF_8);

        assertTrue(json.contains("2020-01-05"));
        assertTrue(json.contains("Еда"));
        assertTrue(json.contains("EXPENSE"));
    }

    @Test
    void jsonRoundTripGivesEqualData() throws Exception {
        WalletData data = WalletMapper.toData(SampleWallet.create());

        WalletData restored = mapper.readValue(mapper.writeValueAsString(data), WalletData.class);

        assertEquals(data, restored);
    }

    @Test
    void readsIntegerAmountAndMissingDescription() throws Exception {
        String json = """
                {"owner":"anna","categories":["Еда"],"budgets":[],
                 "operations":[{"type":"EXPENSE","category":"Еда","amount":300,
                 "date":"2020-01-05","description":null,"counterparty":null}]}
                """;

        Wallet wallet = WalletMapper.toWallet(mapper.readValue(json, WalletData.class));

        assertEquals(new BigDecimal("300.00"), wallet.getOperations().get(0).amount());
        assertEquals("", wallet.getOperations().get(0).description());
    }
}
