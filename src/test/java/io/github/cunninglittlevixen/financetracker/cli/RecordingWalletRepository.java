package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class RecordingWalletRepository implements WalletRepository {

    final List<Wallet> saved = new ArrayList<>();
    RuntimeException failure;

    @Override
    public Optional<Wallet> findByOwner(String login) {
        return Optional.empty();
    }

    @Override
    public void save(Wallet wallet) {
        if (failure != null) {
            throw failure;
        }
        saved.add(wallet);
    }
}
