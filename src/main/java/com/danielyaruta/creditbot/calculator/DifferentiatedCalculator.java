package com.danielyaruta.creditbot.calculator;

import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentEntry;
import com.danielyaruta.creditbot.domain.PaymentSchedule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Расчёт графика платежей по дифференцированной схеме.
 * <p>
 * Особенность: основной долг делится на равные части на весь срок кредита.
 * Проценты начисляются на остаток долга, поэтому ежемесячный платёж
 * уменьшается с каждым месяцем (в начале срока платежи больше, чем в конце).
 */
public class DifferentiatedCalculator extends AbstractPaymentCalculator {

    @Override
    public PaymentSchedule calculate(LoanRequest request) {
        BigDecimal principal = request.amount();
        int termMonths = request.termMonths();
        BigDecimal monthlyRate = monthlyRate(request.annualRate());

        BigDecimal principalPart = principal.divide(BigDecimal.valueOf(termMonths), MONEY_SCALE, RoundingMode.HALF_UP);

        List<PaymentEntry> entries = new ArrayList<>(termMonths);
        BigDecimal remainingBalance = principal;
        BigDecimal totalPayment = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal currentPrincipalPart = principalPart;

            if (month == termMonths) {
                // последний месяц — закрываем фактический остаток,
                // чтобы компенсировать накопленную погрешность округления
                currentPrincipalPart = remainingBalance;
            }

            BigDecimal interestPart = remainingBalance.multiply(monthlyRate)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal payment = currentPrincipalPart.add(interestPart);

            remainingBalance = remainingBalance.subtract(currentPrincipalPart)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            entries.add(new PaymentEntry(month, payment, currentPrincipalPart, interestPart, remainingBalance));

            totalPayment = totalPayment.add(payment);
            totalInterest = totalInterest.add(interestPart);
        }

        return new PaymentSchedule(entries, totalPayment.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                totalInterest.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
    }
}
