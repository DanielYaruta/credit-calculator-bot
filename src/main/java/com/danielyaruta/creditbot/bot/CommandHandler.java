package com.danielyaruta.creditbot.bot;

import org.telegram.telegrambots.meta.api.objects.message.Message;

/**
 * Абстракция обработчика одной команды или одного шага диалога бота.
 * <p>
 * Каждая команда ({@code /start}, {@code /calculate}, {@code /history})
 * реализуется отдельным классом, не зависящим от остальных команд —
 * это даёт высокую связность (каждый обработчик решает одну задачу)
 * и низкую связанность (обработчики не знают друг о друге, только
 * о общих абстракциях — сессии, репозитории, форматтере).
 */
public interface CommandHandler {

    /**
     * Обрабатывает входящее текстовое сообщение пользователя и
     * возвращает текст ответа, который нужно отправить обратно.
     *
     * @param message входящее сообщение Telegram
     * @param session текущая сессия диалога пользователя
     * @return текст ответа пользователю
     */
    String handle(Message message, UserSession session);
}
