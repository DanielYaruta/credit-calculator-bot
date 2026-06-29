package com.danielyaruta.creditbot;

import com.danielyaruta.creditbot.bot.CalculateCommandHandler;
import com.danielyaruta.creditbot.bot.CreditCalculatorBot;
import com.danielyaruta.creditbot.bot.HistoryCommandHandler;
import com.danielyaruta.creditbot.bot.ManagerCommandHandler;
import com.danielyaruta.creditbot.bot.StartCommandHandler;
import com.danielyaruta.creditbot.bot.UserSessionManager;
import com.danielyaruta.creditbot.analytics.AnalyticsService;
import com.danielyaruta.creditbot.calculator.PaymentCalculatorFactory;
import com.danielyaruta.creditbot.config.BotConfig;
import com.danielyaruta.creditbot.formatter.PaymentScheduleFormatter;
import com.danielyaruta.creditbot.storage.InMemoryLoanRequestRepository;
import com.danielyaruta.creditbot.storage.LoanRequestRepository;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

/**
 * Точка входа приложения.
 * <p>
 * Единственная задача этого класса — собрать граф зависимостей
 * (репозиторий, фабрику калькуляторов, форматтер, обработчики команд)
 * и передать их боту. Здесь нет бизнес-логики — только "монтаж"
 * (composition root). Такой подход позволяет менять реализации
 * (например, заменить in-memory репозиторий на JDBC) в одном месте,
 * без изменений в остальных классах.
 */
public class Main {

    public static void main(String[] args) {
        BotConfig config = BotConfig.load();

        // общие для всего приложения компоненты (создаются один раз)
        LoanRequestRepository repository = new InMemoryLoanRequestRepository();
        PaymentCalculatorFactory calculatorFactory = new PaymentCalculatorFactory();
        PaymentScheduleFormatter formatter = new PaymentScheduleFormatter();
        UserSessionManager sessionManager = new UserSessionManager();

        // обработчики команд получают свои зависимости через конструктор
        StartCommandHandler startHandler = new StartCommandHandler();
        CalculateCommandHandler calculateHandler =
                new CalculateCommandHandler(calculatorFactory, repository, formatter);
        HistoryCommandHandler historyHandler = new HistoryCommandHandler(repository);

        AnalyticsService analyticsService = new AnalyticsService(repository);
        ManagerCommandHandler managerHandler = new ManagerCommandHandler(config, analyticsService);

        CreditCalculatorBot bot = new CreditCalculatorBot(
                config.botToken(), sessionManager, startHandler, calculateHandler, historyHandler, managerHandler);

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(config.botToken(), bot);
            System.out.println("✅ Бот успешно запущен и готов принимать сообщения!");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("❌ Ошибка при запуске бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
