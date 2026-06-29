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

class DifferentiatedCalculatorTest {

    private DifferentiatedCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DifferentiatedCalculator();
    }

    private LoanRequest loan(BigDecimal amount, int termMonths, String annualRate) {
        return LoanRequest.of(amount, termMonths, new BigDecimal(annualRate), PaymentType.DIFFERENTIATED, 1L);
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
    void calculate_paymentDecreasesEachMonth() {
        LoanRequest request = loan(BigDecimal.valueOf(1_000_000), 24, "12");
        PaymentSchedule schedule = calculator.calculate(request);
        List<PaymentEntry> entries = schedule.entries();

        for (int i = 0; i < entries.size() - 1; i++) {
            assertTrue(entries.get(i).payment().compareTo(entries.get(i + 1).payment()) >= 0,
                    "Платёж месяца " + (i + 1) + " должен быть не меньше платежа месяца " + (i + 2));
        }
    }

    @Test
    void calculate_zeroInterestRate_allPaymentsEqualPrincipalPart() {
        BigDecimal principal = BigDecimal.valueOf(1_000_000);
        int termMonths = 24;
        LoanRequest request = LoanRequest.of(principal, termMonths, BigDecimal.ZERO,
                PaymentType.DIFFERENTIATED, 1L);

        PaymentSchedule schedule = assertDoesNotThrow(() -> calculator.calculate(request),
                "При нулевой ставке не должно быть исключений");

        List<PaymentEntry> entries = schedule.entries();
        for (PaymentEntry entry : entries) {
            assertEquals(0, entry.payment().compareTo(entry.principalPart()),
                    "При нулевой ставке платёж должен равняться части основного долга");
            assertEquals(0, BigDecimal.ZERO.compareTo(entry.interestPart()),
                    "При нулевой ставке процентная часть должна быть нулём");
        }
    }
}
