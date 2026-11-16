package io.github.cunninglittlevixen.financetracker.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.model.User;
import org.junit.jupiter.api.Test;

class InMemoryUserRepositoryTest {

    private final InMemoryUserRepository repository = new InMemoryUserRepository();

    @Test
    void savedUserCanBeFound() {
        User user = new User("anna", "hash1");

        repository.save(user);

        assertEquals(user, repository.findByLogin("anna").orElseThrow());
    }

    @Test
    void missingUserIsEmpty() {
        assertTrue(repository.findByLogin("nobody").isEmpty());
    }

    @Test
    void saveReplacesUserWithSameLogin() {
        repository.save(new User("anna", "hash1"));
        repository.save(new User("anna", "hash2"));

        assertEquals("hash2", repository.findByLogin("anna").orElseThrow().passwordHash());
    }
}
