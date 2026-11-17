package io.github.cunninglittlevixen.financetracker.infra.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.model.WalletContent;
import io.github.cunninglittlevixen.financetracker.core.service.WalletFormat;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class JsonWalletFormat implements WalletFormat {

    private final ObjectMapper mapper;

    public JsonWalletFormat(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String extension() {
        return "json";
    }

    @Override
    public void write(Wallet wallet, Writer out) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(out, WalletMapper.toData(wallet));
    }

    @Override
    public WalletContent read(Reader in) throws IOException {
        WalletData data;
        try {
            data = mapper.readValue(in, WalletData.class);
        } catch (JsonProcessingException e) {
            throw new ValidationException(
                    "Файл не является корректным JSON-снимком кошелька: " + e.getOriginalMessage());
        }
        if (data == null) {
            throw new ValidationException("Файл не содержит снимка кошелька.");
        }
        try {
            Wallet wallet = WalletMapper.toWallet(data);
            return new WalletContent(wallet.getCategories(), wallet.getBudgets(),
                    wallet.getOperations());
        } catch (RuntimeException e) {
            throw new ValidationException(
                    "Содержимое файла не похоже на снимок кошелька: " + e.getMessage());
        }
    }
}
