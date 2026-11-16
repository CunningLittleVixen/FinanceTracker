package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HelpCommandTest {

    private final Session session = new Session();
    private final CommandRegistry registry = new CommandRegistry();
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final Terminal terminal =
            Terminal.forStreams(new ByteArrayInputStream(new byte[0]), bytes);
    private HelpCommand help;

    @BeforeEach
    void setUp() {
        help = new HelpCommand(registry);
        registry.register(help);
        registry.register(TestCommand.simple("pub"));
        registry.register(new TestCommand("priv", true, false, line -> { }));
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void listShowsOnlyCommandsAvailableNowAndQuotesTip() {
        help.execute(CommandLine.parse("help"), session, terminal);

        assertTrue(output().contains("pub <арг>"));
        assertFalse(output().contains("priv <арг>"));
        assertTrue(output().contains("кавычки"));

        bytes.reset();
        session.login(new User("anna", "h"), new Wallet("anna"));
        help.execute(CommandLine.parse("help"), session, terminal);

        assertTrue(output().contains("priv <арг>"));
    }

    @Test
    void detailsShowUsageDescriptionAndExample() {
        help.execute(CommandLine.parse("help priv"), session, terminal);

        assertTrue(output().contains("Использование: priv <арг>"));
        assertTrue(output().contains("Описание: Тестовая команда"));
        assertTrue(output().contains("Пример: priv x"));
    }

    @Test
    void unknownCommandNameIsRejected() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> help.execute(CommandLine.parse("help nope"), session, terminal));

        assertTrue(e.getMessage().contains("nope"));
    }
}
