package com.danielyaruta.creditbot.bot;

import com.danielyaruta.creditbot.calculator.PaymentCalculatorFactory;
import com.danielyaruta.creditbot.domain.LoanRequest;
import com.danielyaruta.creditbot.domain.PaymentSchedule;
import com.danielyaruta.creditbot.domain.PaymentType;
import com.danielyaruta.creditbot.formatter.PaymentScheduleFormatter;
import com.danielyaruta.creditbot.storage.LoanRequestRepository;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.math.BigDecimal;

/**
 * Обработчик команды {@code /calculate} и всех последующих шагов
 * диалога ввода параметров кредита.
 * <p>
 * Пример композиции: обработчик не наследует ни калькулятор, ни
 * репозиторий, ни форматтер — он получает их через конструктор и
 * использует ("has-a"), оставаясь самостоятельным классом со своей
 * единственной ответственностью — провести пользователя через диалог.
 */
public class CalculateCommandHandler implements CommandHandler {

    private final PaymentCalculatorFactory calculatorFactory;
    private final LoanRequestRepository repository;
    private final PaymentScheduleFormatter formatter;

    public CalculateCommandHandler(PaymentCalculatorFactory calculatorFactory,
                                    LoanRequestRepository repository,
                                    PaymentScheduleFormatter formatter) {
        this.calculatorFactory = calculatorFactory;
        this.repository = repository;
        this.formatter = formatter;
    }

    @Override
    public String handle(Message message, UserSession session) {
        String text = message.getText().trim();

        // запуск диалога командой /calculate
        if ("/calculate".equalsIgnoreCase(text)) {
            session.reset();
            session.setState(DialogState.AWAITING_AMOUNT);
            return "💵 Введите сумму кредита (например, 500000):";
        }

        return switch (session.getState()) {
            case AWAITING_AMOUNT -> handleAmount(text, session);
            case AWAITING_TERM -> handleTerm(text, session);
            case AWAITING_RATE -> handleRate(text, session);
            case AWAITING_PAYMENT_TYPE -> handlePaymentType(text, session, message.getChatId());
            case NONE, AWAITING_MANAGER_LOGIN, AWAITING_MANAGER_PASSWORD ->
                    "Чтобы начать расчёт, отправьте /calculate";
        };
    }

    private String handleAmount(String text, UserSession session) {
        BigDecimal amount;
        try {
            amount = new BigDecimal(text.replace(",", "."));
        } catch (NumberFormatException e) {
            return "⚠️ Не похоже на число. Введите сумму кредита (например, 500000):";
        }
        if (amount.signum() <= 0) {
            return "⚠️ Сумма должна быть больше нуля. Попробуйте ещё раз:";
        }

        session.setAmount(amount);
        session.setState(DialogState.AWAITING_TERM);
        return "📆 Введите срок кредита в месяцах (например, 24):";
    }

    private String handleTerm(String text, UserSession session) {
        int term;
        try {
            term = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return "⚠️ Введите срок кредита целым числом месяцев (например, 24):";
        }
        if (term <= 0) {
            return "⚠️ Срок должен быть больше нуля. Попробуйте ещё раз:";
        }

        session.setTermMonths(term);
        session.setState(DialogState.AWAITING_RATE);
        return "📈 Введите годовую процентную ставку (например, 12.5):";
    }

    private String handleRate(String text, UserSession session) {
        BigDecimal rate;
        try {
            rate = new BigDecimal(text.replace(",", "."));
        } catch (NumberFormatException e) {
            return "⚠️ Не похоже на число. Введите годовую ставку (например, 12.5):";
        }
        if (rate.signum() < 0) {
            return "⚠️ Ставка не может быть отрицательной. Попробуйте ещё раз:";
        }

        session.setAnnualRate(rate);
        session.setState(DialogState.AWAITING_PAYMENT_TYPE);
        return "💳 Выберите вид платежа:\n1 — Аннуитетный\n2 — Дифференцированный\n\nОтветьте 1 или 2:";
    }

    private String handlePaymentType(String text, UserSession session, long chatId) {
        PaymentType type = switch (text.trim()) {
            case "1" -> PaymentType.ANNUITY;
            case "2" -> PaymentType.DIFFERENTIATED;
            default -> null;
        };

        if (type == null) {
            return "⚠️ Ответьте 1 (аннуитетный) или 2 (дифференцированный):";
        }

        session.setPaymentType(type);

        LoanRequest request = LoanRequest.of(
                session.getAmount(), session.getTermMonths(), session.getAnnualRate(), type, chatId);

        PaymentSchedule schedule = calculatorFactory.getCalculator(type).calculate(request);
        repository.save(request);

        session.reset();

        return formatter.format(schedule);
    }
}
