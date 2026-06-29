package com.danielyaruta.creditbot.domain;

/**
 * Вид платежа по кредиту.
 * ANNUITY — аннуитетный платёж: сумма платежа одинакова весь срок.
 * DIFFERENTIATED — дифференцированный платёж: основной долг делится
 * равными частями, проценты начисляются на остаток (платёж уменьшается со временем).
 */
public enum PaymentType {
    ANNUITY,
    DIFFERENTIATED
}
