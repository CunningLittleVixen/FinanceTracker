package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class TerminalTest {

    private static Terminal terminal(String input, ByteArrayOutputStream out) {
        return Terminal.forStreams(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), out);
    }

    @Test
    void readLineShowsPromptReturnsTextAndWritesUtf8() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Terminal terminal = terminal("привет\n", out);

        assertEquals("привет", terminal.readLine("> "));
        terminal.println("Еда");

        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.startsWith("> "));
        assertTrue(text.contains("Еда"));
    }

    @Test
    void readLineReturnsNullAtEndOfInput() {
        assertNull(terminal("", new ByteArrayOutputStream()).readLine("> "));
    }

    @Test
    void askThrowsAtEndOfInput() {
        Terminal terminal = terminal("", new ByteArrayOutputStream());

        assertThrows(InputEndedException.class, () -> terminal.ask("Пароль: "));
    }
}
