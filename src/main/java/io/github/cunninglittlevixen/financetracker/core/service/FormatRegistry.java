package io.github.cunninglittlevixen.financetracker.core.service;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class FormatRegistry {

    private final List<WalletFormat> formats;

    public FormatRegistry(List<WalletFormat> formats) {
        this.formats = List.copyOf(formats);
    }

    public WalletFormat forFile(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return formats.stream()
                .filter(format -> format.extension().equals(extension))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Неизвестный формат файла «" + fileName
                        + "». Поддерживаются: " + supported() + "."));
    }

    public String supported() {
        return formats.stream()
                .map(format -> "." + format.extension())
                .collect(Collectors.joining(", "));
    }
}
