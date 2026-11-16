package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record CommandLine(String name, List<String> args, Map<String, String> options) {

    public CommandLine {
        args = List.copyOf(args);
        options = Collections.unmodifiableMap(new LinkedHashMap<>(options));
    }

    public static CommandLine parse(String line) {
        List<String> tokens = line == null ? List.of() : tokenize(line);
        if (tokens.isEmpty()) {
            return new CommandLine("", List.of(), Map.of());
        }
        List<String> args = new ArrayList<>();
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 1; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (!token.startsWith("--")) {
                args.add(token);
                continue;
            }
            String key = token.substring(2).toLowerCase(Locale.ROOT);
            if (key.isEmpty()) {
                throw new ValidationException("После «--» нужно указать название опции.");
            }
            if (i + 1 >= tokens.size() || tokens.get(i + 1).startsWith("--")) {
                throw new ValidationException("Для опции --" + key + " нужно указать значение.");
            }
            if (options.containsKey(key)) {
                throw new ValidationException("Опция --" + key + " указана дважды.");
            }
            options.put(key, tokens.get(++i));
        }
        return new CommandLine(tokens.get(0).toLowerCase(Locale.ROOT), args, options);
    }

    public boolean isEmpty() {
        return name.isEmpty();
    }

    public Optional<String> option(String optionName) {
        return Optional.ofNullable(options.get(optionName));
    }

    public Optional<LocalDate> optionDate(String optionName) {
        return option(optionName).map(text -> {
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Дата в опции --" + optionName
                        + " должна быть в формате ГГГГ-ММ-ДД, например 2026-11-30. Вы ввели: "
                        + text);
            }
        });
    }

    public void check(int minArgs, int maxArgs, Set<String> allowedOptions, String usage) {
        if (args.size() < minArgs || args.size() > maxArgs) {
            throw new ValidationException("Неверное число аргументов. Использование: " + usage);
        }
        for (String key : options.keySet()) {
            if (!allowedOptions.contains(key)) {
                String allowed = allowedOptions.isEmpty()
                        ? "У этой команды нет опций."
                        : "Допустимые опции: " + allowedOptions.stream().sorted()
                        .map(option -> "--" + option).collect(Collectors.joining(", "))
                        + ".";
                throw new ValidationException("Неизвестная опция --" + key + ". " + allowed
                        + " Использование: " + usage);
            }
        }
    }

    private static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean hasToken = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                hasToken = true;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (hasToken) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    hasToken = false;
                }
            } else {
                current.append(c);
                hasToken = true;
            }
        }
        if (inQuotes) {
            throw new ValidationException(
                    "Не закрыта кавычка: добавьте закрывающую \" после названия.");
        }
        if (hasToken) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}