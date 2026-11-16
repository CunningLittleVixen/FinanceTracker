package io.github.cunninglittlevixen.financetracker.infra.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonUserRepository implements UserRepository {

    public record UsersData(List<User> users) {
    }

    private final Path file;
    private final ObjectMapper mapper;
    private final Map<String, User> users = new LinkedHashMap<>();

    public JsonUserRepository(Path file, ObjectMapper mapper) {
        this.file = file;
        this.mapper = mapper;
        load();
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(login));
    }

    @Override
    public void save(User user) {
        User previous = users.put(user.login(), user);
        try {
            JsonFiles.writeAtomically(mapper, file, new UsersData(List.copyOf(users.values())));
        } catch (StorageException e) {
            if (previous == null) {
                users.remove(user.login());
            } else {
                users.put(user.login(), previous);
            }
            throw e;
        }
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            UsersData data = mapper.readValue(file.toFile(), UsersData.class);
            for (User user : data.users()) {
                if (users.putIfAbsent(user.login(), user) != null) {
                    throw new StorageException(
                            "Логин повторяется в файле " + file + ": " + user.login());
                }
            }
        } catch (StorageException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new StorageException("Файл пользователей повреждён или не читается: " + file
                    + ". Исправьте файл или удалите его.", e);
        }
    }
}
