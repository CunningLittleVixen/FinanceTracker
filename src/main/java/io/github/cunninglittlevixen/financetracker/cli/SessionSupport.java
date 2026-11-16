package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.User;
import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;

final class SessionSupport {

    private SessionSupport() {
    }

    static Wallet open(Session session, User user, WalletRepository wallets) {
        Wallet wallet = wallets.findByOwner(user.login())
                .orElseGet(() -> new Wallet(user.login()));
        session.login(user, wallet);
        return wallet;
    }
}
