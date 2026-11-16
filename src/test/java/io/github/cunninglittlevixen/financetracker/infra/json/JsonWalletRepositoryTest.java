package io.github.cunninglittlevixen.financetracker.infra.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.model.Operation;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JsonWalletRepositoryTest {

    @TempDir
    Path tempDir;

    private Path walletsDir;
    private JsonWalletRepository repository;

    @BeforeEach
    void setUp() {
        walletsDir = tempDir.resolve("wallets");
        repository = new JsonWalletRepository(walletsDir, JsonMapperFactory.create());
    }

    @Test
    void savedWalletIsRestoredFromFile() {
        Wallet original = SampleWallet.create();

        repository.save(original);
        Wallet restored = repository.findByOwner("anna").orElseThrow();

        assertEquals(original.getCategories(), restored.getCategories());
        assertEquals(original.getBudgets(), restored.getBudgets());
        assertEquals(original.getOperations(), restored.getOperations());
    }

    @Test
    void missingFileGivesEmptyResult() {
        assertTrue(repository.findByOwner("nobody").isEmpty());
    }

    @Test
    void saveCreatesDirectoryAndReadableUtf8File() throws IOException {
        repository.save(SampleWallet.create());

        Path file = walletsDir.resolve("anna.json");
        assertTrue(Files.exists(file));
        assertTrue(Files.readString(file).contains("Еда"));
    }

    @Test
    void secondSaveReplacesFileAndLeavesNoTempFiles() throws IOException {
        Wallet wallet = SampleWallet.create();
        repository.save(wallet);
        wallet.addOperation(new Operation(OperationType.EXPENSE, "Еда", new BigDecimal("5"),
                LocalDate.of(2020, 2, 1), null, null));

        repository.save(wallet);

        assertEquals(4, repository.findByOwner("anna").orElseThrow().getOperations().size());
        try (Stream<Path> files = Files.list(walletsDir)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void ownerLookupIgnoresCase() {
        repository.save(SampleWallet.create());

        assertTrue(repository.findByOwner("ANNA").isPresent());
    }

    @Test
    void corruptedFileGivesClearErrorAndStaysUntouched() throws IOException {
        Files.createDirectories(walletsDir);
        Path file = walletsDir.resolve("anna.json");
        Files.writeString(file, "{ not json");

        StorageException e = assertThrows(StorageException.class,
                () -> repository.findByOwner("anna"));

        assertTrue(e.getMessage().contains("anna.json"));
        assertEquals("{ not json", Files.readString(file));
    }

    @Test
    void fileBreakingModelRulesIsRejected() throws IOException {
        Files.createDirectories(walletsDir);
        Files.writeString(walletsDir.resolve("anna.json"), """
                {"owner":"anna","categories":["Еда"],"budgets":[],
                 "operations":[{"type":"EXPENSE","category":"Еда","amount":-5,
                 "date":"2020-01-05","description":"","counterparty":null}]}
                """);

        assertThrows(StorageException.class, () -> repository.findByOwner("anna"));
    }

    @Test
    void fileOfAnotherOwnerIsRejected() throws IOException {
        repository.save(SampleWallet.create());
        Files.copy(walletsDir.resolve("anna.json"), walletsDir.resolve("boris.json"));

        assertThrows(StorageException.class, () -> repository.findByOwner("boris"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../evil", "a/b", "a.b", "a b"})
    void unsafeLoginIsRejected(String login) {
        assertThrows(StorageException.class, () -> repository.findByOwner(login));
    }
}
