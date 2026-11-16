package io.github.cunninglittlevixen.financetracker.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class Terminal {

    private final BufferedReader in;
    private final PrintStream out;

    private Terminal(BufferedReader in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public static Terminal forStreams(InputStream input, OutputStream output) {
        return new Terminal(
                new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    public void println(String text) {
        out.println(text);
    }

    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String ask(String prompt) {
        String line = readLine(prompt);
        if (line == null) {
            throw new InputEndedException();
        }
        return line;
    }
}
