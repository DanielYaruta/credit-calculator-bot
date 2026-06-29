package com.danielyaruta.creditbot.calculator;

import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentEntry;
import com.danielyaruta.creditbot.domain.PaymentSchedule;
import com.danielyaruta.creditbot.domain.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnuityCalculatorTest {

    private AnnuityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AnnuityCalculator();
    }

    private LoanRequest loan(BigDecimal amount, int termMonths, String annualRate) {
        return LoanRequest.of(amount, termMonths, new BigDecimal(annualRate), PaymentType.ANNUITY, 1L);
    }

    @Test
    void calculate_standardLoan_monthlyPaymentIsConstant() {
        LoanRequest request = loan(BigDecimal.valueOf(1_000_000), 24, "12");
        PaymentSchedule schedule = calculator.calculate(request);
        List<PaymentEntry> entries = schedule.entries();

        BigDecimal expectedPayment = BigDecimal.valueOf(47073.47);

        for (int i = 0; i < entries.size() - 1; i++) {
            assertEquals(0, expectedPayment.compareTo(entries.get(i).payment()),
                    "Платёж месяца " + (i + 1) + " должен быть " + expectedPayment);
        }
    }

    @Test
    void calculate_lastMonth_remainingBalanceIsZero() {
        LoanRequest request = loan(BigDecimal.valueOf(1_000_000), 24, "12");
        PaymentSchedule schedule = calculator.calculate(request);
        List<PaymentEntry> entries = schedule.entries();

        BigDecimal lastBalance = entries.get(entries.size() - 1).remainingBalance();
        assertEquals(0, BigDecimal.ZERO.compareTo(lastBalance),
                "Остаток после последнего платежа должен быть равен нулю");
    }

    @Test
    void calculate_sumOfPrincipalParts_equalsOriginalAmount() {
        BigDecimal principal = BigDecimal.valueOf(1_000_000);
        LoanRequest request = loan(principal, 24, "12");
        PaymentSchedule schedule = calculator.calculate(request);

        BigDecimal sumOfPrincipal = schedule.entries().stream()
                .map(PaymentEntry::principalPart)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, principal.setScale(2, RoundingMode.HALF_UP).compareTo(sumOfPrincipal),
                "Сумма частей основного долга должна равняться исходной сумме кредита");
    }

    @Test
    void calculate_zeroInterestRate_paymentsAreEqualDivision() {
        BigDecimal principal = BigDecimal.valueOf(1_000_000);
        int termMonths = 24;
        LoanRequest request = LoanRequest.of(principal, termMonths, BigDecimal.ZERO, PaymentType.ANNUITY, 1L);

        PaymentSchedule schedule = assertDoesNotThrow(() -> calculator.calculate(request),
                "При нулевой ставке не должно быть ArithmeticException");

        BigDecimal expectedPayment = principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        List<PaymentEntry> entries = schedule.entries();

        for (int i = 0; i < entries.size() - 1; i++) {
            assertEquals(0, expectedPayment.compareTo(entries.get(i).payment()),
                    "При нулевой ставке платёж должен равняться сумме / срок");
        }
    }

    @Test
    void calculate_totalInterest_isPositiveForNonZeroRate() {
        LoanRequest request = loan(BigDecimal.valueOf(1_000_000), 24, "12");
        PaymentSchedule schedule = calculator.calculate(request);

        assertTrue(schedule.totalInterest().compareTo(BigDecimal.ZERO) > 0,
                "Общая сумма процентов при ненулевой ставке должна быть больше нуля");
    }
}
