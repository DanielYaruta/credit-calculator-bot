package com.danielyaruta.creditbot.bot;

import com.danielyaruta.creditbot.domain.PaymentType;

import java.math.BigDecimal;

/**
 * Состояние диалога одного пользователя с ботом — на каком шаге
 * ввода параметров кредита он находится и что уже успел ввести.
 * <p>
 * В отличие от объектов в пакете {@code domain} (которые неизменяемы),
 * сессия — изменяемый объект: естественно, что состояние диалога
 * меняется по ходу переписки с пользователем.
 */
public class UserSession {

    private DialogState state = DialogState.NONE;
    private BigDecimal amount;
    private Integer termMonths;
    private BigDecimal annualRate;
    private PaymentType paymentType;
    private String pendingManagerLogin;
    private boolean authorizedAsManager;

    public DialogState getState() {
        return state;
    }

    public void setState(DialogState state) {
        this.state = state;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public void setAnnualRate(BigDecimal annualRate) {
        this.annualRate = annualRate;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getPendingManagerLogin() {
        return pendingManagerLogin;
    }

    public void setPendingManagerLogin(String pendingManagerLogin) {
        this.pendingManagerLogin = pendingManagerLogin;
    }

    public boolean isAuthorizedAsManager() {
        return authorizedAsManager;
    }

    public void setAuthorizedAsManager(boolean authorizedAsManager) {
        this.authorizedAsManager = authorizedAsManager;
    }

    /**
     * Сбрасывает диалог в исходное состояние (например, после
     * завершения расчёта или по команде /start).
     */
    public void reset() {
        state = DialogState.NONE;
        amount = null;
        termMonths = null;
        annualRate = null;
        paymentType = null;
    }
}
