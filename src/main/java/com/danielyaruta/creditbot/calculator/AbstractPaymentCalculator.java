package com.danielyaruta.creditbot.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Общая база для всех калькуляторов графика платежей.
 * <p>
 * Здесь живёт логика, не зависящая от конкретной схемы платежа
 * (перевод годовой ставки в месячную, единые константы округления).
 * Конкретные схемы расчёта (аннуитет, дифференцированный платёж)
 * наследуют этот класс и реализуют только свой алгоритм в {@code calculate}.
 * <p>
 * Это пример контролируемого использования наследования: оно применяется
 * только для переиспользования вспомогательной логики, а не для
 * построения сложной иерархии типов — основной контракт всё равно
 * объявлен через интерфейс {@link PaymentScheduleCalculator}.
 */
public abstract class AbstractPaymentCalculator implements PaymentScheduleCalculator {

    protected static final int CALCULATION_SCALE = 10;
    protected static final int MONEY_SCALE = 2;

    /**
     * Переводит годовую процентную ставку (в процентах, например 12.5)
     * в месячную ставку (в долях, например 0.0104...).
     */
    protected BigDecimal monthlyRate(BigDecimal annualRatePercent) {
        return annualRatePercent
                .divide(BigDecimal.valueOf(100), CALCULATION_SCALE, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), CALCULATION_SCALE, RoundingMode.HALF_UP);
    }
}
