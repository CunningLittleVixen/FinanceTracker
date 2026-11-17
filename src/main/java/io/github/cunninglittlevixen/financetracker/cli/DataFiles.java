package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.model.WalletContent;
import io.github.cunninglittlevixen.financetracker.core.service.WalletFormat;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class DataFiles {

    private DataFiles() {
    }

    static Path path(String text) {
        try {
            Path path = Path.of(text);
            if (path.getFileName() == null) {
                throw new ValidationException("Укажите имя файла, а не папку: " + text);
            }
            return path;
        } catch (InvalidPathException e) {
            throw new ValidationException("Некорректный путь к файлу: " + text);
        }
    }

    static void write(WalletFormat format, Wallet wallet, Path file) {
        Writer out = openNew(file);
        try (out) {
            format.write(wallet, out);
        } catch (IOException | RuntimeException e) {
            deleteQuietly(file);
            throw new ValidationException(
                    "Не удалось записать файл " + file + ": " + e.getMessage());
        }
    }

    static WalletContent read(WalletFormat format, Path file) {
        if (!Files.isRegularFile(file)) {
            throw new ValidationException("Файл не найден: " + file);
        }
        try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return format.read(in);
        } catch (UncheckedIOException e) {
            throw readError(file, e.getCause());
        } catch (IOException e) {
            throw readError(file, e);
        } catch (ValidationException e) {
            throw new ValidationException("Файл " + file + ": " + e.getMessage());
        }
    }

    private static Writer openNew(Path file) {
        try {
            return Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException e) {
            throw new ValidationException("Файл " + file + " уже существует. Укажите другое имя: "
                    + "существующие файлы не перезаписываются.");
        } catch (NoSuchFileException e) {
            throw new ValidationException(
                    "Не удалось создать файл " + file + ": такой папки не существует.");
        } catch (IOException e) {
            throw new ValidationException(
                    "Не удалось создать файл " + file + ": " + e.getMessage());
        }
    }

    private static ValidationException readError(Path file, IOException cause) {
        if (cause instanceof CharacterCodingException) {
            return new ValidationException("Файл " + file + " должен быть в кодировке UTF-8.");
        }
        return new ValidationException(
                "Не удалось прочитать файл " + file + ": " + cause.getMessage());
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // файл мог не создаться; сообщение об исходной ошибке важнее
        }
    }
}
