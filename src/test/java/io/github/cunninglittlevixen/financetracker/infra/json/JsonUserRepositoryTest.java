package io.github.cunninglittlevixen.financetracker.infra.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonUserRepositoryTest {

    @TempDir
    Path tempDir;

    private final ObjectMapper mapper = JsonMapperFactory.create();
    private Path file;

    @BeforeEach
    void setUp() {
        file = tempDir.resolve("data").resolve("users.json");
    }

    @Test
    void savedUsersSurviveRestart() {
        JsonUserRepository first = new JsonUserRepository(file, mapper);
        first.save(new User("anna", "pbkdf2$1000$abc$def"));
        first.save(new User("boris", "pbkdf2$1000$ghi$jkl"));

        JsonUserRepository second = new JsonUserRepository(file, mapper);

        assertEquals("pbkdf2$1000$abc$def", second.findByLogin("anna").orElseThrow().passwordHash());
        assertTrue(second.findByLogin("boris").isPresent());
    }

    @Test
    void missingFileGivesEmptyRepositoryWithoutCreatingFile() {
        JsonUserRepository repository = new JsonUserRepository(file, mapper);

        assertTrue(repository.findByLogin("anna").isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void saveCreatesFileWithLogin() throws IOException {
        new JsonUserRepository(file, mapper).save(new User("anna", "hash"));

        assertTrue(Files.readString(file).contains("anna"));
    }

    @Test
    void saveReplacesUserWithSameLogin() {
        JsonUserRepository first = new JsonUserRepository(file, mapper);
        first.save(new User("anna", "hash1"));
        first.save(new User("anna", "hash2"));

        JsonUserRepository second = new JsonUserRepository(file, mapper);

        assertEquals("hash2", second.findByLogin("anna").orElseThrow().passwordHash());
    }

    @Test
    void corruptedFileGivesClearError() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{ not json");

        StorageException e = assertThrows(StorageException.class,
                () -> new JsonUserRepository(file, mapper));

        assertTrue(e.getMessage().contains("users.json"));
    }

    @Test
    void duplicateLoginsInFileAreRejected() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, """
                {"users":[{"login":"anna","passwordHash":"h1"},
                          {"login":"anna","passwordHash":"h2"}]}
                """);

        assertThrows(StorageException.class, () -> new JsonUserRepository(file, mapper));
    }

    @Test
    void failedWriteDoesNotLeaveUserInMemory() throws IOException {
        Path blocker = tempDir.resolve("blocker");
        Files.writeString(blocker, "not a directory");
        JsonUserRepository repository =
                new JsonUserRepository(blocker.resolve("users.json"), mapper);

        assertThrows(StorageException.class, () -> repository.save(new User("anna", "hash")));

        assertTrue(repository.findByLogin("anna").isEmpty());
    }
}
