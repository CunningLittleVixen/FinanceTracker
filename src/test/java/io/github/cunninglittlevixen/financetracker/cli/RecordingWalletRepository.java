package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.model.Wallet;
import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class RecordingWalletRepository implements WalletRepository {

    final List<Wallet> saved = new ArrayList<>();
    final Map<String, Wallet> stored = new HashMap<>();
    RuntimeException failure;
    RuntimeException loadFailure;

    @Override
    public Optional<Wallet> findByOwner(String login) {
        if (loadFailure != null) {
            throw loadFailure;
        }
        return Optional.ofNullable(stored.get(login));
    }

    @Override
    public void save(Wallet wallet) {
        if (failure != null) {
            throw failure;
        }
        saved.add(wallet);
        stored.put(wallet.getOwner(), wallet);
    }
}