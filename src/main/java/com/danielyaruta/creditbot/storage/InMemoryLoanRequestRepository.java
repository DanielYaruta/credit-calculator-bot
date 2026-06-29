package com.danielyaruta.creditbot.storage;

import com.danielyaruta.creditbot.domain.LoanRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Хранилище запросов в памяти на основе Java Collections Framework.
 * <p>
 * Структура данных: {@code HashMap<userId, List<LoanRequest>>}.
 * Такая организация даёт быстрый (O(1)) доступ к истории запросов
 * конкретного пользователя — это самая частая операция (команда
 * {@code /history}). Полный список для менеджеров получается
 * объединением всех списков по требованию, а не хранится отдельно —
 * это устраняет дублирование данных и риск их рассинхронизации (DRY).
 * <p>
 * Класс не является потокобезопасным в строгом смысле (используется
 * {@code synchronized} на уровне методов для базовой защиты от
 * параллельных обращений нескольких пользователей бота одновременно).
 * Для прода такого объёма обычно достаточно: операции короткие,
 * а блокировка на уровне всего хранилища простая и понятная (KISS).
 */
public class InMemoryLoanRequestRepository implements LoanRequestRepository {

    private final Map<Long, List<LoanRequest>> requestsByUser = new HashMap<>();

    @Override
    public synchronized void save(LoanRequest request) {
        requestsByUser
                .computeIfAbsent(request.userId(), id -> new ArrayList<>())
                .add(request);
    }

    @Override
    public synchronized List<LoanRequest> findByUserId(long userId) {
        List<LoanRequest> userRequests = requestsByUser.getOrDefault(userId, Collections.emptyList());
        return userRequests.stream()
                .sorted(Comparator.comparing(LoanRequest::requestedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<LoanRequest> findByFilter(LoanRequestFilter filter) {
        return findAll().stream()
                .filter(r -> filter.matches(r.amount(), r.paymentType()))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<LoanRequest> findAll() {
        List<LoanRequest> all = new ArrayList<>();
        for (List<LoanRequest> userRequests : requestsByUser.values()) {
            all.addAll(userRequests);
        }
        all.sort(Comparator.comparing(LoanRequest::requestedAt).reversed());
        return all;
    }
}
