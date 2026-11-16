package io.github.cunninglittlevixen.financetracker.core.repository;

import io.github.cunninglittlevixen.financetracker.core.model.User;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByLogin(String login);

    void save(User user);
}
