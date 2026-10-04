package br.com.isiflix.fakeerp.dto;

import br.com.isiflix.fakeerp.entity.CreditDecisionEntity;
import br.com.isiflix.fakeerp.entity.CreditDecisionStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Decisão de crédito gravada. {@code decision} é o que o squad registrou;
 * {@code status} é a situação atual (alterada só pela aprovação humana).
 */
public record CreditDecisionDTO(
        Long id,
        String cnpj,
        int referenceYear,
        int referenceMonth,
        int revision,
        Long supersedesId,
        BigDecimal requestedAmount,
        BigDecimal proposedScore,
        String policyVersion,
        CreditDecisionStatus decision,
        CreditDecisionStatus status,
        String createdBy,
        Instant createdAt,
        String approvedBy,
        Instant approvedAt) {

    public static CreditDecisionDTO fromEntity(CreditDecisionEntity d) {
        return new CreditDecisionDTO(
                d.getId(),
                d.getCnpj(),
                d.getReferenceYear(),
                d.getReferenceMonth(),
                d.getRevision(),
                d.getSupersedesId(),
                d.getRequestedAmount(),
                d.getProposedScore(),
                d.getPolicyVersion(),
                d.getDecision(),
                d.getStatus(),
                d.getCreatedBy(),
                d.getCreatedAt(),
                d.getApprovedBy(),
                d.getApprovedAt());
    }
}
