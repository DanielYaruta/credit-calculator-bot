package com.danielyaruta.creditbot.bot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Главный класс бота — точка входа для всех обновлений от Telegram.
 * <p>
 * Это самый верхний слой архитектуры: он НЕ содержит бизнес-логику
 * расчётов или хранения данных, только маршрутизацию (диспетчеризацию)
 * входящих сообщений к подходящему {@link CommandHandler}. Такое
 * разделение — пример низкой связанности: если потребуется добавить
 * новую команду, не нужно трогать этот класс, достаточно создать
 * новый обработчик и зарегистрировать его в карте команд.
 */
public class CreditCalculatorBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserSessionManager sessionManager;
    private final StartCommandHandler startHandler;
    private final CalculateCommandHandler calculateHandler;
    private final HistoryCommandHandler historyHandler;
    private final ManagerCommandHandler managerHandler;

    public CreditCalculatorBot(String botToken,
                                UserSessionManager sessionManager,
                                StartCommandHandler startHandler,
                                CalculateCommandHandler calculateHandler,
                                HistoryCommandHandler historyHandler,
                                ManagerCommandHandler managerHandler) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.sessionManager = sessionManager;
        this.startHandler = startHandler;
        this.calculateHandler = calculateHandler;
        this.historyHandler = historyHandler;
        this.managerHandler = managerHandler;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Message message = update.getMessage();
        long chatId = message.getChatId();
        String text = message.getText().trim();
        UserSession session = sessionManager.getSession(chatId);

        String responseText = routeMessage(message, text, session);

        sendMessage(chatId, responseText);
    }

    /**
     * Определяет, какой обработчик должен ответить на сообщение.
     * Сначала проверяются явные команды, а если пользователь находится
     * в середине диалога (например, вводит параметры кредита) —
     * сообщение передаётся обработчику, который этот диалог ведёт.
     */
    private String routeMessage(Message message, String text, UserSession session) {
        if ("/start".equalsIgnoreCase(text)) {
            return startHandler.handle(message, session);
        }
        if ("/history".equalsIgnoreCase(text)) {
            return historyHandler.handle(message, session);
        }
        if ("/calculate".equalsIgnoreCase(text) || isInCalculationDialog(session)) {
            return calculateHandler.handle(message, session);
        }
        if (isManagerCommandOrDialog(text, session)) {
            return managerHandler.handle(message, session);
        }

        return "Не понимаю эту команду 🤔\n\nДоступные команды:\n/calculate — рассчитать кредит\n/history — история запросов";
    }

    private boolean isInCalculationDialog(UserSession session) {
        return session.getState() == DialogState.AWAITING_AMOUNT
                || session.getState() == DialogState.AWAITING_TERM
                || session.getState() == DialogState.AWAITING_RATE
                || session.getState() == DialogState.AWAITING_PAYMENT_TYPE;
    }

    private boolean isManagerCommandOrDialog(String text, UserSession session) {
        return "/manager".equalsIgnoreCase(text)
                || "/stats".equalsIgnoreCase(text)
                || text.toLowerCase().startsWith("/filter")
                || session.getState() == DialogState.AWAITING_MANAGER_LOGIN
                || session.getState() == DialogState.AWAITING_MANAGER_PASSWORD;
    }

    private void sendMessage(long chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Не удалось отправить сообщение в чат " + chatId + ": " + e.getMessage());
        }
    }
}
