package com.danielyaruta.creditbot.storage;

import com.danielyaruta.creditbot.domain.LoanRequest;

import java.util.List;

/**
 * Абстракция хранилища запросов пользователей на расчёт кредита.
 * <p>
 * Обработчики команд бота и сервисы аналитики работают только с этим
 * интерфейсом и не знают, как данные хранятся на самом деле — в памяти
 * (через коллекции), в файле или в реальной базе данных. Это позволяет
 * позже заменить реализацию (например, на JDBC-репозиторий с PostgreSQL)
 * без единой правки в остальном коде — низкая связанность в действии.
 */
public interface LoanRequestRepository {

    /**
     * Сохраняет запрос пользователя в хранилище.
     */
    void save(LoanRequest request);

    /**
     * Возвращает историю запросов конкретного пользователя
     * в порядке от самого нового к самому старому.
     */
    List<LoanRequest> findByUserId(long userId);

    /**
     * Возвращает все запросы, удовлетворяющие критериям фильтра.
     * Используется менеджерами для аналитики.
     */
    List<LoanRequest> findByFilter(LoanRequestFilter filter);

    /**
     * Возвращает абсолютно все запросы, накопленные в хранилище.
     */
    List<LoanRequest> findAll();
}
