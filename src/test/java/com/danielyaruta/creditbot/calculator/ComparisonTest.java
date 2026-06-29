package com.danielyaruta.creditbot.calculator;

import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentSchedule;
import com.danielyaruta.creditbot.domain.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ComparisonTest {

    private final AnnuityCalculator annuityCalculator = new AnnuityCalculator();
    private final DifferentiatedCalculator differentiatedCalculator = new DifferentiatedCalculator();

    @Test
    void annuityVsDifferentiated_sameParameters_differentiatedHasLessTotalInterest() {
        BigDecimal amount = BigDecimal.valueOf(1_000_000);
        int termMonths = 24;
        BigDecimal annualRate = new BigDecimal("12");

        LoanRequest annuityRequest = LoanRequest.of(amount, termMonths, annualRate, PaymentType.ANNUITY, 1L);
        LoanRequest differentiatedRequest = LoanRequest.of(amount, termMonths, annualRate, PaymentType.DIFFERENTIATED, 1L);

        PaymentSchedule annuitySchedule = annuityCalculator.calculate(annuityRequest);
        PaymentSchedule differentiatedSchedule = differentiatedCalculator.calculate(differentiatedRequest);

        assertTrue(differentiatedSchedule.totalInterest().compareTo(annuitySchedule.totalInterest()) < 0,
                "Переплата по дифференцированному кредиту должна быть строго меньше, чем по аннуитетному: "
                        + "дифф=" + differentiatedSchedule.totalInterest()
                        + ", аннуитет=" + annuitySchedule.totalInterest());
    }
}
