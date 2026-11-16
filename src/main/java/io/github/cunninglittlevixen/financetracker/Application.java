package io.github.cunninglittlevixen.financetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cunninglittlevixen.financetracker.cli.AddCategoryCommand;
import io.github.cunninglittlevixen.financetracker.cli.AddOperationCommand;
import io.github.cunninglittlevixen.financetracker.cli.CategoriesCommand;
import io.github.cunninglittlevixen.financetracker.cli.CommandLoop;
import io.github.cunninglittlevixen.financetracker.cli.CommandRegistry;
import io.github.cunninglittlevixen.financetracker.cli.ExitCommand;
import io.github.cunninglittlevixen.financetracker.cli.HelpCommand;
import io.github.cunninglittlevixen.financetracker.cli.HistoryCommand;
import io.github.cunninglittlevixen.financetracker.cli.LoginCommand;
import io.github.cunninglittlevixen.financetracker.cli.LogoutCommand;
import io.github.cunninglittlevixen.financetracker.cli.RegisterCommand;
import io.github.cunninglittlevixen.financetracker.cli.RemoveBudgetCommand;
import io.github.cunninglittlevixen.financetracker.cli.RemoveCategoryCommand;
import io.github.cunninglittlevixen.financetracker.cli.RenameCategoryCommand;
import io.github.cunninglittlevixen.financetracker.cli.ReportCommand;
import io.github.cunninglittlevixen.financetracker.cli.Session;
import io.github.cunninglittlevixen.financetracker.cli.SetBudgetCommand;
import io.github.cunninglittlevixen.financetracker.cli.Terminal;
import io.github.cunninglittlevixen.financetracker.cli.TransferCommand;
import io.github.cunninglittlevixen.financetracker.core.model.OperationType;
import io.github.cunninglittlevixen.financetracker.core.repository.UserRepository;
import io.github.cunninglittlevixen.financetracker.core.repository.WalletRepository;
import io.github.cunninglittlevixen.financetracker.core.service.AuthService;
import io.github.cunninglittlevixen.financetracker.core.service.BudgetService;
import io.github.cunninglittlevixen.financetracker.core.service.CategoryService;
import io.github.cunninglittlevixen.financetracker.core.service.NotificationService;
import io.github.cunninglittlevixen.financetracker.core.service.OperationService;
import io.github.cunninglittlevixen.financetracker.core.service.StatisticsService;
import io.github.cunninglittlevixen.financetracker.core.service.TransferService;
import io.github.cunninglittlevixen.financetracker.infra.Pbkdf2PasswordHasher;
import io.github.cunninglittlevixen.financetracker.infra.json.JsonMapperFactory;
import io.github.cunninglittlevixen.financetracker.infra.json.JsonUserRepository;
import io.github.cunninglittlevixen.financetracker.infra.json.JsonWalletRepository;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class Application {

    private final Path dataDir;
    private final int passwordIterations;

    public Application(Path dataDir, int passwordIterations) {
        this.dataDir = dataDir;
        this.passwordIterations = passwordIterations;
    }

    public void run(InputStream in, OutputStream out) {
        ObjectMapper mapper = JsonMapperFactory.create();
        UserRepository users = new JsonUserRepository(dataDir.resolve("users.json"), mapper);
        WalletRepository wallets = new JsonWalletRepository(dataDir.resolve("wallets"), mapper);

        StatisticsService statistics = new StatisticsService();
        NotificationService notifications = new NotificationService(statistics);
        AuthService auth = new AuthService(users, new Pbkdf2PasswordHasher(passwordIterations));
        CategoryService categories = new CategoryService();
        BudgetService budgets = new BudgetService();
        OperationService operations = new OperationService();
        TransferService transfers = new TransferService(users, wallets);

        CommandRegistry registry = new CommandRegistry();
        registry.register(new HelpCommand(registry));
        registry.register(new RegisterCommand(auth, wallets));
        registry.register(new LoginCommand(auth, wallets, notifications));
        registry.register(new LogoutCommand(wallets));
        registry.register(new CategoriesCommand());
        registry.register(new AddCategoryCommand(categories));
        registry.register(new RenameCategoryCommand(categories));
        registry.register(new RemoveCategoryCommand(categories));
        registry.register(new SetBudgetCommand(budgets));
        registry.register(new RemoveBudgetCommand(budgets));
        registry.register(new AddOperationCommand(OperationType.INCOME, operations, notifications));
        registry.register(new AddOperationCommand(OperationType.EXPENSE, operations, notifications));
        registry.register(new TransferCommand(transfers, notifications));
        registry.register(new ReportCommand(statistics, notifications));
        registry.register(new HistoryCommand(statistics));
        registry.register(new ExitCommand());

        new CommandLoop(Terminal.forStreams(in, out), new Session(), registry, wallets).run();
    }
}
