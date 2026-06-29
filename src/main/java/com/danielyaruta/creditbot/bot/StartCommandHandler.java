package com.danielyaruta.creditbot.bot;

import org.telegram.telegrambots.meta.api.objects.message.Message;

/**
 * Обработчик команды {@code /start}. Приветствует пользователя
 * и сбрасывает его диалог в исходное состояние.
 */
public class StartCommandHandler implements CommandHandler {

    @Override
    public String handle(Message message, UserSession session) {
        session.reset();
        return "👋 Привет! Я бот для расчёта графиков погашения кредитов.\n\n" +
                "Доступные команды:\n" +
                "/calculate — рассчитать график платежей по кредиту\n" +
                "/history — посмотреть историю своих запросов\n" +
                "/manager — вход для менеджеров по продажам\n\n" +
                "Чтобы начать расчёт, отправьте /calculate";
    }
}
