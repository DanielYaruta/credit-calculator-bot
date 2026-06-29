package com.danielyaruta.creditbot.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Параметры кредита, введённые пользователем.
 * Неизменяемый объект-значение (value object) — чистые данные,
 * никакой логики расчёта здесь нет (это задача калькуляторов).
 *
 * @param amount      сумма кредита
 * @param termMonths  срок кредита в месяцах
 * @param annualRate  годовая процентная ставка в процентах (например, 12.5 означает 12.5%)
 * @param paymentType вид платежа (аннуитетный / дифференцированный)
 * @param userId      идентификатор пользователя Telegram, сделавшего запрос
 * @param requestedAt момент создания запроса (для истории и аналитики)
 */
public record LoanRequest(
        BigDecimal amount,
        int termMonths,
        BigDecimal annualRate,
        PaymentType paymentType,
        long userId,
        Instant requestedAt
) {

    public LoanRequest {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма кредита должна быть больше нуля");
        }
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Срок кредита должен быть больше нуля месяцев");
        }
        if (annualRate == null || annualRate.signum() < 0) {
            throw new IllegalArgumentException("Процентная ставка не может быть отрицательной");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("Вид платежа должен быть указан");
        }
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }

    /**
     * Удобный фабричный метод для создания запроса с текущим временем.
     */
    public static LoanRequest of(BigDecimal amount, int termMonths, BigDecimal annualRate,
                                  PaymentType paymentType, long userId) {
        return new LoanRequest(amount, termMonths, annualRate, paymentType, userId, Instant.now());
    }
}
