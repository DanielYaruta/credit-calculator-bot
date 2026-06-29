package com.danielyaruta.creditbot.formatter;

import com.danielyaruta.creditbot.domain.PaymentEntry;
import com.danielyaruta.creditbot.domain.PaymentSchedule;

/**
 * Форматирует {@link PaymentSchedule} в текст, удобный для отображения
 * в сообщении Telegram.
 * <p>
 * Вынесен в отдельный класс осознанно: ни {@code calculator} (отвечает
 * только за математику), ни {@code bot} (отвечает только за диалог)
 * не должны знать, как именно график превращается в текст — это
 * отдельная ответственность представления данных (High Cohesion).
 * Telegram ограничивает длину сообщения 4096 символами, поэтому для
 * длинных графиков (например, ипотека на 20+ лет) выводится только
 * первые и последние несколько платежей плюс итоговая сводка.
 */
public class PaymentScheduleFormatter {

    private static final int MAX_FULL_MONTHS_DISPLAY = 24;
    private static final int EDGE_MONTHS_DISPLAY = 6;

    public String format(PaymentSchedule schedule) {
        StringBuilder sb = new StringBuilder();
        sb.append("📅 График платежей\n\n");

        var entries = schedule.entries();

        if (entries.size() <= MAX_FULL_MONTHS_DISPLAY) {
            for (PaymentEntry entry : entries) {
                appendEntry(sb, entry);
            }
        } else {
            for (int i = 0; i < EDGE_MONTHS_DISPLAY; i++) {
                appendEntry(sb, entries.get(i));
            }
            sb.append("...\n");
            for (int i = entries.size() - EDGE_MONTHS_DISPLAY; i < entries.size(); i++) {
                appendEntry(sb, entries.get(i));
            }
        }

        sb.append("\n💰 Итого:\n");
        sb.append("Общая сумма выплат: ").append(schedule.totalPayment()).append(" ₽\n");
        sb.append("Переплата по процентам: ").append(schedule.totalInterest()).append(" ₽\n");

        return sb.toString();
    }

    private void appendEntry(StringBuilder sb, PaymentEntry entry) {
        sb.append(String.format(
                "Месяц %d: платёж %s ₽ (долг %s / проценты %s), остаток %s ₽%n",
                entry.month(), entry.payment(), entry.principalPart(),
                entry.interestPart(), entry.remainingBalance()
        ));
    }
}
