package com.danielyaruta.creditbot.bot;

import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentType;
import com.danielyaruta.creditbot.storage.LoanRequestRepository;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Обработчик команды {@code /history} — показывает пользователю
 * историю его собственных запросов на расчёт кредита.
 */
public class HistoryCommandHandler implements CommandHandler {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int MAX_HISTORY_ITEMS = 10;

    private final LoanRequestRepository repository;

    public HistoryCommandHandler(LoanRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public String handle(Message message, UserSession session) {
        long chatId = message.getChatId();
        List<LoanRequest> history = repository.findByUserId(chatId);

        if (history.isEmpty()) {
            return "📭 У вас пока нет запросов. Отправьте /calculate, чтобы рассчитать кредит.";
        }

        StringBuilder sb = new StringBuilder("📜 Ваша история запросов:\n\n");
        int shown = 0;
        for (LoanRequest request : history) {
            if (shown >= MAX_HISTORY_ITEMS) {
                sb.append("...и ещё ").append(history.size() - MAX_HISTORY_ITEMS).append(" запрос(ов)\n");
                break;
            }
            String date = DATE_FORMAT.format(request.requestedAt().atZone(ZoneId.systemDefault()));
            sb.append(String.format(
                    "🔹 %s — %s ₽, %d мес, %s%% годовых, %s%n",
                    date, request.amount(), request.termMonths(), request.annualRate(),
                    paymentTypeLabel(request.paymentType())
            ));
            shown++;
        }

        return sb.toString();
    }

    private String paymentTypeLabel(PaymentType type) {
        return switch (type) {
            case ANNUITY -> "аннуитетный";
            case DIFFERENTIATED -> "дифференцированный";
        };
    }
}
