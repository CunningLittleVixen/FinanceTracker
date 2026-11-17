package io.github.cunninglittlevixen.financetracker.core.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WalletContentTest {

    @Test
    void nullListsBecomeEmpty() {
        WalletContent content = new WalletContent(null, null, null);

        assertTrue(content.categories().isEmpty());
        assertTrue(content.budgets().isEmpty());
        assertTrue(content.operations().isEmpty());
    }

    @Test
    void listsAreCopiedAndReadOnly() {
        List<Category> source = new ArrayList<>(List.of(new Category("Еда")));

        WalletContent content = new WalletContent(source, List.of(), List.of());
        source.clear();

        assertTrue(content.categories().size() == 1);
        assertThrows(UnsupportedOperationException.class,
                () -> content.categories().add(new Category("Такси")));
    }
}
