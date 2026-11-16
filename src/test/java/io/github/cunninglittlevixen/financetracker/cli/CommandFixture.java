package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

final class CommandFixture {

    final Session session = new Session();
    final Wallet wallet = new Wallet("anna");
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final Terminal terminal =
            Terminal.forStreams(new ByteArrayInputStream(new byte[0]), bytes);

    CommandFixture() {
        session.login(new User("anna", "h"), wallet);
    }

    void run(Command command, String line) {
        command.execute(CommandLine.parse(line), session, terminal);
    }

    String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
