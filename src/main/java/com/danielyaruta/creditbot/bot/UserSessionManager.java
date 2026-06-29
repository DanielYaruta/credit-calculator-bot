package com.danielyaruta.creditbot.bot;

import java.util.HashMap;
import java.util.Map;

/**
 * Хранилище активных сессий диалогов пользователей бота.
 * Структура — {@code HashMap<chatId, UserSession>}, что даёт быстрый
 * доступ к сессии конкретного чата при каждом новом сообщении.
 */
public class UserSessionManager {

    private final Map<Long, UserSession> sessions = new HashMap<>();

    /**
     * Возвращает сессию пользователя, создавая новую при первом обращении.
     */
    public synchronized UserSession getSession(long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new UserSession());
    }
}
