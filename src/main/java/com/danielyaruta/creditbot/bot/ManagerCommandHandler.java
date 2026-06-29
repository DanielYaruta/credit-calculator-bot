package com.danielyaruta.creditbot.bot;

import com.danielyaruta.creditbot.analytics.AnalyticsService;
import com.danielyaruta.creditbot.analytics.LoanRequestStatistics;
import com.danielyaruta.creditbot.config.BotConfig;
import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentType;
import com.danielyaruta.creditbot.storage.LoanRequestFilter;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Обработчик команд для менеджеров по продажам: авторизация по
 * логину и паролю, просмотр статистики, фильтрация запросов.
 * <p>
 * Авторизация хранится в самой {@link UserSession} (поле
 * {@code authorizedAsManager}) — это простое решение без отдельной
 * системы токенов или сессий, осознанно выбранное по принципу KISS:
 * для учебного проекта с одним ботом этого достаточно, а каждый
 * пользователь Telegram и так однозначно идентифицируется chatId.
 */
public class ManagerCommandHandler implements CommandHandler {

    private final BotConfig config;
    private final AnalyticsService analyticsService;

    public ManagerCommandHandler(BotConfig config, AnalyticsService analyticsService) {
        this.config = config;
        this.analyticsService = analyticsService;
    }

    @Override
    public String handle(Message message, UserSession session) {
        String text = message.getText().trim();

        if ("/manager".equalsIgnoreCase(text)) {
            if (session.isAuthorizedAsManager()) {
                return managerMenu();
            }
            session.setState(DialogState.AWAITING_MANAGER_LOGIN);
            return "🔐 Введите логин менеджера:";
        }

        if (session.getState() == DialogState.AWAITING_MANAGER_LOGIN) {
            session.setPendingManagerLogin(text);
            session.setState(DialogState.AWAITING_MANAGER_PASSWORD);
            return "🔑 Введите пароль:";
        }

        if (session.getState() == DialogState.AWAITING_MANAGER_PASSWORD) {
            boolean correct = config.managerLogin().equals(session.getPendingManagerLogin())
                    && config.managerPassword().equals(text);

            session.setPendingManagerLogin(null);
            session.setState(DialogState.NONE);

            if (!correct) {
                return "⛔ Неверный логин или пароль.";
            }

            session.setAuthorizedAsManager(true);
            return "✅ Авторизация успешна.\n\n" + managerMenu();
        }

        if (!session.isAuthorizedAsManager()) {
            return "⛔ Сначала авторизуйтесь командой /manager";
        }

        if ("/stats".equalsIgnoreCase(text)) {
            return formatStatistics();
        }

        if (text.toLowerCase().startsWith("/filter")) {
            return handleFilter(text);
        }

        return managerMenu();
    }

    private String managerMenu() {
        return "📊 Меню менеджера:\n" +
                "/stats — общая статистика по запросам\n" +
                "/filter <мин_сумма> <макс_сумма> [вид] — фильтр по сумме и виду платежа\n" +
                "вид: 1 — аннуитетный, 2 — дифференцированный (необязателен)\n" +
                "(например: /filter 100000 500000 или /filter 100000 500000 1)";
    }

    private String formatStatistics() {
        LoanRequestStatistics stats = analyticsService.getStatistics();

        if (stats.totalRequests() == 0) {
            return "📭 Пока нет ни одного запроса от пользователей.";
        }

        StringBuilder sb = new StringBuilder("📊 Статистика по запросам\n\n");
        sb.append("Всего запросов: ").append(stats.totalRequests()).append("\n");
        sb.append("Средняя сумма кредита: ").append(stats.averageAmount()).append(" ₽\n");
        sb.append(String.format("Средний срок: %.1f мес%n", stats.averageTermMonths()));
        sb.append("Самый популярный вид платежа: ").append(label(stats.mostPopularPaymentType())).append("\n\n");
        sb.append("Распределение по видам платежа:\n");
        for (Map.Entry<PaymentType, Long> entry : stats.requestsByPaymentType().entrySet()) {
            sb.append("— ").append(label(entry.getKey())).append(": ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    private String handleFilter(String text) {
        String[] parts = text.split("\\s+");
        if (parts.length != 3 && parts.length != 4) {
            return "⚠️ Использование: /filter <мин_сумма> <макс_сумма> [вид]\n" +
                    "вид: 1 — аннуитетный, 2 — дифференцированный (необязателен)\n" +
                    "Например: /filter 100000 500000 или /filter 100000 500000 1";
        }

        BigDecimal min;
        BigDecimal max;
        try {
            min = new BigDecimal(parts[1]);
            max = new BigDecimal(parts[2]);
        } catch (NumberFormatException e) {
            return "⚠️ Суммы должны быть числами. Например: /filter 100000 500000";
        }

        PaymentType type = null;
        if (parts.length == 4) {
            type = switch (parts[3]) {
                case "1" -> PaymentType.ANNUITY;
                case "2" -> PaymentType.DIFFERENTIATED;
                default -> null;
            };
            if (type == null) {
                return "⚠️ Вид платежа должен быть 1 (аннуитетный) или 2 (дифференцированный).";
            }
        }

        LoanRequestFilter filter = new LoanRequestFilter(min, max, type);
        List<LoanRequest> filtered = analyticsService.getFilteredRequests(filter);

        if (filtered.isEmpty()) {
            return "📭 Нет запросов в диапазоне от " + min + " до " + max + " ₽"
                    + (type != null ? " (" + label(type) + ")" : "");
        }

        StringBuilder sb = new StringBuilder("🔍 Найдено запросов: " + filtered.size() + "\n\n");
        for (LoanRequest request : filtered) {
            sb.append("— ").append(request.amount()).append(" ₽, ")
                    .append(request.termMonths()).append(" мес, ")
                    .append(label(request.paymentType())).append("\n");
        }

        return sb.toString();
    }

    private String label(PaymentType type) {
        if (type == null) {
            return "—";
        }
        return switch (type) {
            case ANNUITY -> "аннуитетный";
            case DIFFERENTIATED -> "дифференцированный";
        };
    }
}
