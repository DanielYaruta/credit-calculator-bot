package com.danielyaruta.creditbot.bot;

/**
 * Шаг диалога ввода параметров кредита пользователем.
 * Бот спрашивает параметры по очереди, и это состояние говорит,
 * какой именно вопрос сейчас ожидает ответа.
 */
public enum DialogState {
    NONE,
    AWAITING_AMOUNT,
    AWAITING_TERM,
    AWAITING_RATE,
    AWAITING_PAYMENT_TYPE,
    AWAITING_MANAGER_LOGIN,
    AWAITING_MANAGER_PASSWORD
}
