package io.github.cunninglittlevixen.financetracker.infra.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class JsonWalletRepository implements WalletRepository {

    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9_]+");

    private final Path directory;
    private final ObjectMapper mapper;

    public JsonWalletRepository(Path directory, ObjectMapper mapper) {
        this.directory = directory;
        this.mapper = mapper;
    }

    @Override
    public Optional<Wallet> findByOwner(String login) {
        Path file = fileFor(login);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        Wallet wallet;
        try {
            wallet = WalletMapper.toWallet(mapper.readValue(file.toFile(), WalletData.class));
        } catch (IOException | RuntimeException e) {
            throw new StorageException("Файл кошелька повреждён или не читается: " + file
                    + ". Исправьте файл или удалите его.", e);
        }
        if (!wallet.getOwner().equalsIgnoreCase(login)) {
            throw new StorageException("Файл " + file + " принадлежит другому пользователю: "
                    + wallet.getOwner() + ".");
        }
        return Optional.of(wallet);
    }

    @Override
    public void save(Wallet wallet) {
        Path file = fileFor(wallet.getOwner());
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(directory);
            mapper.writeValue(temp.toFile(), WalletMapper.toData(wallet));
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            deleteQuietly(temp);
            throw new StorageException("Не удалось сохранить кошелёк: " + file, e);
        }
    }

    private Path fileFor(String login) {
        if (login == null || !SAFE_NAME.matcher(login).matches()) {
            throw new StorageException("Недопустимый логин для имени файла: " + login);
        }
        return directory.resolve(login.toLowerCase(Locale.ROOT) + ".json");
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // временный файл не удалился, основной файл при этом цел
        }
    }
}
