package com.danielyaruta.creditbot.domain;

import java.math.BigDecimal;

/**
 * Одна строка графика платежей — данные за один месяц.
 *
 * @param month             номер месяца (начиная с 1)
 * @param payment           сумма платежа за месяц (основной долг + проценты)
 * @param principalPart     часть платежа, идущая на погашение основного долга
 * @param interestPart      часть платежа, идущая на уплату процентов
 * @param remainingBalance  остаток основного долга после этого платежа
 */
public record PaymentEntry(
        int month,
        BigDecimal payment,
        BigDecimal principalPart,
        BigDecimal interestPart,
        BigDecimal remainingBalance
) {
}
