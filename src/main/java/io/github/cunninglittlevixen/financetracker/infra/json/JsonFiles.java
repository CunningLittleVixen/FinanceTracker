package io.github.cunninglittlevixen.financetracker.infra.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cunninglittlevixen.financetracker.core.exception.StorageException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class JsonFiles {

    private JsonFiles() {
    }

    static void writeAtomically(ObjectMapper mapper, Path file, Object value) {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            mapper.writeValue(temp.toFile(), value);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            deleteQuietly(temp);
            throw new StorageException("Не удалось сохранить файл: " + file, e);
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // временный файл не удалился, основной файл при этом цел
        }
    }
}
