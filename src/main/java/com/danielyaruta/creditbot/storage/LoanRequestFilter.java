package com.danielyaruta.creditbot.storage;

import com.danielyaruta.creditbot.domain.PaymentType;

import java.math.BigDecimal;

/**
 * Критерии фильтрации запросов для менеджеров по продажам.
 * <p>
 * Все поля опциональны (могут быть null) — null означает "не фильтровать
 * по этому критерию". Это избавляет от необходимости иметь несколько
 * перегруженных методов поиска в репозитории (KISS): один метод
 * {@code findByFilter} обслуживает все комбинации критериев.
 *
 * @param minAmount   минимальная сумма кредита (включительно), или null
 * @param maxAmount   максимальная сумма кредита (включительно), или null
 * @param paymentType вид платежа, или null для любого вида
 */
public record LoanRequestFilter(
        BigDecimal minAmount,
        BigDecimal maxAmount,
        PaymentType paymentType
) {

    /**
     * Фильтр без ограничений — возвращает все запросы.
     */
    public static LoanRequestFilter all() {
        return new LoanRequestFilter(null, null, null);
    }

    public boolean matches(BigDecimal amount, PaymentType type) {
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            return false;
        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        if (paymentType != null && paymentType != type) {
            return false;
        }
        return true;
    }
}
