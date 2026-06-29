package com.danielyaruta.creditbot.calculator;

import com.danielyaruta.creditbot.domain.PaymentType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Фабрика калькуляторов графика платежей.
 * <p>
 * Реализует паттерн Factory: вызывающий код не создаёт конкретные
 * калькуляторы напрямую (не пишет {@code new AnnuityCalculator()}),
 * а просто запрашивает калькулятор по виду платежа — фабрика сама
 * решает, какую реализацию вернуть.
 * <p>
 * Это снижает связанность между обработчиками команд бота и
 * конкретными классами калькуляторов: при появлении нового вида
 * платежа достаточно добавить одну запись в карту, не трогая
 * остальной код (открытость для расширения — Open/Closed Principle).
 */
public class PaymentCalculatorFactory {

    private final Map<PaymentType, PaymentScheduleCalculator> calculators = new EnumMap<>(PaymentType.class);

    public PaymentCalculatorFactory() {
        calculators.put(PaymentType.ANNUITY, new AnnuityCalculator());
        calculators.put(PaymentType.DIFFERENTIATED, new DifferentiatedCalculator());
    }

    /**
     * Возвращает калькулятор, соответствующий указанному виду платежа.
     *
     * @param paymentType вид платежа
     * @return подходящая реализация {@link PaymentScheduleCalculator}
     * @throws IllegalArgumentException если для указанного вида платежа нет реализации
     */
    public PaymentScheduleCalculator getCalculator(PaymentType paymentType) {
        PaymentScheduleCalculator calculator = calculators.get(paymentType);
        if (calculator == null) {
            throw new IllegalArgumentException("Нет калькулятора для вида платежа: " + paymentType);
        }
        return calculator;
    }
}
