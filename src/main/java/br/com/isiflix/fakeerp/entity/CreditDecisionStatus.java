package br.com.isiflix.fakeerp.entity;

/**
 * Situação de uma decisão de crédito.
 * Agentes só podem gravar PENDING_REVIEW ou REJECTED; APPROVED exige o escopo credit:approve.
 */
public enum CreditDecisionStatus {
    APPROVED,
    REJECTED,
    PENDING_REVIEW
}
