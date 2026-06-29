package com.danielyaruta.creditbot.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Полный график платежей по кредиту вместе с итоговыми суммами.
 * Результат работы любого {@code PaymentScheduleCalculator}.
 *
 * @param entries       список платежей по месяцам
 * @param totalPayment  общая сумма всех платежей за весь срок
 * @param totalInterest общая сумма уплаченных процентов (переплата)
 */
public record PaymentSchedule(
        List<PaymentEntry> entries,
        BigDecimal totalPayment,
        BigDecimal totalInterest
) {
}
