package com.danielyaruta.creditbot.analytics;

import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentType;
import com.danielyaruta.creditbot.storage.LoanRequestFilter;
import com.danielyaruta.creditbot.storage.LoanRequestRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис аналитики по запросам пользователей — используется менеджерами
 * по продажам для просмотра агрегированных данных и статистики.
 * <p>
 * Пример композиции: сервис не наследует репозиторий, а получает ссылку
 * на абстракцию {@link LoanRequestRepository} через конструктор
 * (внедрение зависимости). Это позволяет сервису работать с любой
 * реализацией хранилища — в памяти, в файле, в базе данных — без
 * изменения своего кода. Аналитика отделена от самого хранения данных
 * (High Cohesion): репозиторий хранит и выдаёт данные, а это класс
 * только считает по ним статистику.
 */
public class AnalyticsService {

    private final LoanRequestRepository repository;

    public AnalyticsService(LoanRequestRepository repository) {
        this.repository = repository;
    }

    /**
     * Возвращает агрегированные данные по запросам, удовлетворяющим фильтру.
     */
    public List<LoanRequest> getFilteredRequests(LoanRequestFilter filter) {
        return repository.findByFilter(filter);
    }

    /**
     * Считает статистику по всем накопленным запросам:
     * среднюю сумму, средний срок, распределение по видам платежа
     * и самый популярный вид платежа.
     */
    public LoanRequestStatistics getStatistics() {
        List<LoanRequest> allRequests = repository.findAll();

        if (allRequests.isEmpty()) {
            return new LoanRequestStatistics(0, BigDecimal.ZERO, 0.0, new EnumMap<>(PaymentType.class), null);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        long totalTermMonths = 0;
        Map<PaymentType, Long> countByType = new EnumMap<>(PaymentType.class);

        for (LoanRequest request : allRequests) {
            totalAmount = totalAmount.add(request.amount());
            totalTermMonths += request.termMonths();
            countByType.merge(request.paymentType(), 1L, Long::sum);
        }

        int count = allRequests.size();
        BigDecimal averageAmount = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        double averageTermMonths = (double) totalTermMonths / count;

        PaymentType mostPopular = countByType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return new LoanRequestStatistics(count, averageAmount, averageTermMonths, countByType, mostPopular);
    }
}
