package io.github.cunninglittlevixen.financetracker.cli;

import io.github.cunninglittlevixen.financetracker.core.service.Notification;
import java.util.List;

final class NotificationPrinter {

    private NotificationPrinter() {
    }

    static void show(Terminal terminal, List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return;
        }
        terminal.println("Оповещения:");
        for (Notification notification : notifications) {
            terminal.println("  [!] " + notification.message());
        }
    }
}
