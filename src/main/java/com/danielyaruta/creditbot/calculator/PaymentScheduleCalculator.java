package com.danielyaruta.creditbot.calculator;

import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentSchedule;

/**
 * Абстракция расчёта графика платежей по кредиту.
 * <p>
 * Это ключевой пример применения принципа абстракции в проекте:
 * вызывающий код (например, обработчик команды бота) работает только
 * с этим интерфейсом и не знает, какая конкретная схема расчёта
 * используется — аннуитетная или дифференцированная.
 * <p>
 * Реализации не зависят друг от друга и не зависят от Telegram API
 * или слоя хранения данных — отсюда низкая связанность (Low Coupling).
 * Каждая реализация отвечает только за одну формулу расчёта —
 * отсюда высокая связность (High Cohesion).
 */
public interface PaymentScheduleCalculator {

    /**
     * Рассчитывает полный график платежей для заданных параметров кредита.
     *
     * @param request параметры кредита
     * @return график платежей по месяцам с итоговыми суммами
     */
    PaymentSchedule calculate(LoanRequest request);
}
