package io.github.cunninglittlevixen.financetracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cunninglittlevixen.financetracker.core.exception.ValidationException;
import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import org.junit.jupiter.api.Test;

class SessionTest {

    @Test
    void newSessionIsRunningAndLoggedOut() {
        Session session = new Session();

        assertTrue(session.isRunning());
        assertFalse(session.isLoggedIn());
    }

    @Test
    void walletAndUserAreUnavailableWhenLoggedOut() {
        Session session = new Session();

        assertThrows(ValidationException.class, session::wallet);
        assertThrows(ValidationException.class, session::user);
    }

    @Test
    void loginStoresUserAndWalletAndLogoutClearsThem() {
        Session session = new Session();
        User user = new User("anna", "hash");
        Wallet wallet = new Wallet("anna");

        session.login(user, wallet);

        assertTrue(session.isLoggedIn());
        assertEquals("anna", session.user().login());
        assertSame(wallet, session.wallet());

        session.logout();

        assertFalse(session.isLoggedIn());
    }

    @Test
    void stopEndsRunning() {
        Session session = new Session();

        session.stop();

        assertFalse(session.isRunning());
    }
}
