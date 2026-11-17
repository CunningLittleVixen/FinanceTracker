package io.github.cunninglittlevixen.financetracker.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.model.WalletContent;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormatRegistryTest {

    private final WalletFormat csv = stub("csv");
    private final WalletFormat json = stub("json");
    private final FormatRegistry registry = new FormatRegistry(List.of(csv, json));

    private static WalletFormat stub(String extension) {
        return new WalletFormat() {
            @Override
            public String extension() {
                return extension;
            }

            @Override
            public void write(Wallet wallet, Writer out) {
            }

            @Override
            public WalletContent read(Reader in) {
                return new WalletContent(null, null, null);
            }
        };
    }

    @Test
    void findsFormatByExtensionIgnoringCase() {
        assertSame(json, registry.forFile("Backup.JSON"));
        assertSame(csv, registry.forFile("ops.csv"));
    }

    @Test
    void rejectsUnknownExtensionAndListsSupportedOnes() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> registry.forFile("report.txt"));

        assertTrue(e.getMessage().contains("Неизвестный формат"));
        assertTrue(e.getMessage().contains(".csv"));
    }

    @Test
    void rejectsFileWithoutExtension() {
        assertThrows(ValidationException.class, () -> registry.forFile("backup"));
    }

    @Test
    void supportedListsAllExtensions() {
        assertEquals(".csv, .json", registry.supported());
    }
}
