package com.danielyaruta.creditbot.calculator;

import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentEntry;
import com.danielyaruta.creditbot.domain.PaymentSchedule;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Расчёт графика платежей по аннуитетной схеме.
 * <p>
 * Особенность: сумма ежемесячного платежа одинакова на весь срок кредита,
 * но соотношение "проценты / основной долг" внутри платежа меняется —
 * в начале срока большую часть платежа составляют проценты,
 * к концу срока — основной долг.
 * <p>
 * Формула аннуитетного платежа:
 * A = P * (r * (1+r)^n) / ((1+r)^n - 1)
 * где P — сумма кредита, r — месячная ставка, n — срок в месяцах.
 */
public class AnnuityCalculator extends AbstractPaymentCalculator {

    private static final MathContext MATH_CONTEXT = new MathContext(20);

    @Override
    public PaymentSchedule calculate(LoanRequest request) {
        BigDecimal principal = request.amount();
        int termMonths = request.termMonths();
        BigDecimal monthlyRate = monthlyRate(request.annualRate());

        BigDecimal monthlyPayment = calculateAnnuityPayment(principal, monthlyRate, termMonths);

        List<PaymentEntry> entries = new ArrayList<>(termMonths);
        BigDecimal remainingBalance = principal;
        BigDecimal totalPayment = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPart = remainingBalance.multiply(monthlyRate)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal payment = monthlyPayment;
            BigDecimal principalPart;

            if (month == termMonths) {
                // в последний месяц закрываем остаток полностью,
                // чтобы избежать накопленной погрешности округления
                principalPart = remainingBalance;
                payment = principalPart.add(interestPart);
            } else {
                principalPart = payment.subtract(interestPart).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            }

            remainingBalance = remainingBalance.subtract(principalPart).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            entries.add(new PaymentEntry(month, payment, principalPart, interestPart, remainingBalance));

            totalPayment = totalPayment.add(payment);
            totalInterest = totalInterest.add(interestPart);
        }

        return new PaymentSchedule(entries, totalPayment.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                totalInterest.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
    }

    private BigDecimal calculateAnnuityPayment(BigDecimal principal, BigDecimal monthlyRate, int termMonths) {
        if (monthlyRate.signum() == 0) {
            // беспроцентный кредит — просто делим сумму на срок
            return principal.divide(BigDecimal.valueOf(termMonths), MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRatePowN = onePlusRate.pow(termMonths, MATH_CONTEXT);

        BigDecimal numerator = monthlyRate.multiply(onePlusRatePowN, MATH_CONTEXT);
        BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE);

        BigDecimal factor = numerator.divide(denominator, CALCULATION_SCALE, RoundingMode.HALF_UP);

        return principal.multiply(factor).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
