package com.danielyaruta.creditbot.storage;

import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryLoanRequestRepositoryTest {

    private InMemoryLoanRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLoanRequestRepository();
    }

    @Test
    void save_and_findByUserId_returnsOnlyThatUsersRequests() {
        LoanRequest user1Request1 = LoanRequest.of(BigDecimal.valueOf(500_000), 12, new BigDecimal("10"), PaymentType.ANNUITY, 1L);
        LoanRequest user1Request2 = LoanRequest.of(BigDecimal.valueOf(700_000), 24, new BigDecimal("12"), PaymentType.DIFFERENTIATED, 1L);
        LoanRequest user2Request = LoanRequest.of(BigDecimal.valueOf(300_000), 6, new BigDecimal("8"), PaymentType.ANNUITY, 2L);

        repository.save(user1Request1);
        repository.save(user1Request2);
        repository.save(user2Request);

        List<LoanRequest> user1Results = repository.findByUserId(1L);
        assertEquals(2, user1Results.size(), "Должно быть 2 запроса у пользователя 1");
        assertTrue(user1Results.stream().allMatch(r -> r.userId() == 1L), "Все результаты должны принадлежать пользователю 1");

        List<LoanRequest> user2Results = repository.findByUserId(2L);
        assertEquals(1, user2Results.size(), "Должен быть 1 запрос у пользователя 2");
    }

    @Test
    void findByUserId_sortedByRequestedAtDescending() {
        Instant oldest = Instant.parse("2024-01-01T10:00:00Z");
        Instant middle = Instant.parse("2024-06-15T12:00:00Z");
        Instant newest = Instant.parse("2025-01-01T08:00:00Z");

        LoanRequest oldRequest = new LoanRequest(BigDecimal.valueOf(500_000), 12, new BigDecimal("10"), PaymentType.ANNUITY, 42L, oldest);
        LoanRequest middleRequest = new LoanRequest(BigDecimal.valueOf(600_000), 18, new BigDecimal("11"), PaymentType.ANNUITY, 42L, middle);
        LoanRequest newRequest = new LoanRequest(BigDecimal.valueOf(700_000), 24, new BigDecimal("12"), PaymentType.DIFFERENTIATED, 42L, newest);

        // Сохраняем в произвольном порядке
        repository.save(middleRequest);
        repository.save(oldRequest);
        repository.save(newRequest);

        List<LoanRequest> results = repository.findByUserId(42L);

        assertEquals(3, results.size());
        assertEquals(newest, results.get(0).requestedAt(), "Первым должен идти самый новый запрос");
        assertEquals(middle, results.get(1).requestedAt(), "Вторым — средний по дате");
        assertEquals(oldest, results.get(2).requestedAt(), "Последним — самый старый запрос");
    }

    @Test
    void findByUserId_nonExistingUser_returnsEmptyList() {
        List<LoanRequest> results = repository.findByUserId(999L);
        assertNotNull(results, "Результат не должен быть null");
        assertTrue(results.isEmpty(), "Для несуществующего пользователя должен вернуться пустой список");
    }

    @Test
    void findAll_returnsRequestsFromAllUsers() {
        repository.save(LoanRequest.of(BigDecimal.valueOf(100_000), 6, new BigDecimal("9"), PaymentType.ANNUITY, 1L));
        repository.save(LoanRequest.of(BigDecimal.valueOf(200_000), 12, new BigDecimal("10"), PaymentType.DIFFERENTIATED, 2L));
        repository.save(LoanRequest.of(BigDecimal.valueOf(300_000), 24, new BigDecimal("11"), PaymentType.ANNUITY, 3L));

        List<LoanRequest> all = repository.findAll();
        assertEquals(3, all.size(), "findAll должен возвращать запросы всех пользователей");
    }

    @Test
    void findByFilter_byAmountRange_returnsOnlyMatching() {
        repository.save(LoanRequest.of(BigDecimal.valueOf(100_000), 6, new BigDecimal("9"), PaymentType.ANNUITY, 1L));
        repository.save(LoanRequest.of(BigDecimal.valueOf(500_000), 12, new BigDecimal("10"), PaymentType.ANNUITY, 2L));
        repository.save(LoanRequest.of(BigDecimal.valueOf(1_000_000), 24, new BigDecimal("12"), PaymentType.ANNUITY, 3L));

        LoanRequestFilter filter = new LoanRequestFilter(BigDecimal.valueOf(100_000), BigDecimal.valueOf(500_000), null);
        List<LoanRequest> results = repository.findByFilter(filter);

        assertEquals(2, results.size(), "Должны попасть только запросы в диапазоне [100000, 500000]");
        assertTrue(results.stream().allMatch(r -> r.amount().compareTo(BigDecimal.valueOf(100_000)) >= 0
                && r.amount().compareTo(BigDecimal.valueOf(500_000)) <= 0),
                "Все результаты должны попадать в диапазон суммы (включительно)");
    }

    @Test
    void findByFilter_byPaymentType_returnsOnlyMatching() {
        repository.save(LoanRequest.of(BigDecimal.valueOf(300_000), 12, new BigDecimal("10"), PaymentType.ANNUITY, 1L));
        repository.save(LoanRequest.of(BigDecimal.valueOf(400_000), 18, new BigDecimal("11"), PaymentType.DIFFERENTIATED, 2L));
        repository.save(LoanRequest.of(BigDecimal.valueOf(500_000), 24, new BigDecimal("12"), PaymentType.ANNUITY, 3L));

        LoanRequestFilter filter = new LoanRequestFilter(null, null, PaymentType.ANNUITY);
        List<LoanRequest> results = repository.findByFilter(filter);

        assertEquals(2, results.size(), "Должны вернуться только аннуитетные запросы");
        assertTrue(results.stream().allMatch(r -> r.paymentType() == PaymentType.ANNUITY),
                "Все результаты должны иметь тип ANNUITY");
    }
}
