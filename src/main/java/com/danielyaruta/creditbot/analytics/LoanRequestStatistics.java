package com.danielyaruta.creditbot.analytics;

import com.danielyaruta.creditbot.domain.PaymentType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Агрегированная статистика по запросам пользователей —
 * то, что менеджер по продажам видит в команде статистики.
 *
 * @param totalRequests        общее количество запросов
 * @param averageAmount        средняя сумма запрошенного кредита
 * @param averageTermMonths    средний срок кредита в месяцах
 * @param requestsByPaymentType  количество запросов по каждому виду платежа
 * @param mostPopularPaymentType вид платежа, который запрашивают чаще всего
 */
public record LoanRequestStatistics(
        long totalRequests,
        BigDecimal averageAmount,
        double averageTermMonths,
        Map<PaymentType, Long> requestsByPaymentType,
        PaymentType mostPopularPaymentType
) {
}
