package io.github.cunninglittlevixen.financetracker.core.repository;

import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import java.util.Optional;

public interface WalletRepository {

    Optional<Wallet> findByOwner(String login);

    void save(Wallet wallet);
}
