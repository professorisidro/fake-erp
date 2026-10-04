package br.com.isiflix.fakeerp.dto;

import br.com.isiflix.fakeerp.entity.CreditPolicyEntity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Política de crédito vigente. {@code policyVersion} deve ser referenciada na decisão gravada.
 */
public record CreditPolicyDTO(
        String policyVersion,
        String segment,
        int minMonthsActive,
        BigDecimal minMonthlyRevenue,
        BigDecimal maxDiscountRateAllowed,
        int minOrdersCountLast3Months,
        BigDecimal maxRequestedAmount,
        Instant updatedAt) {

    public static CreditPolicyDTO fromEntity(CreditPolicyEntity p) {
        return new CreditPolicyDTO(
                p.getPolicyVersion(),
                p.getSegment(),
                p.getMinMonthsActive(),
                p.getMinMonthlyRevenue(),
                p.getMaxDiscountRateAllowed().stripTrailingZeros(),
                p.getMinOrdersCountLast3Months(),
                p.getMaxRequestedAmount(),
                p.getUpdatedAt());
    }
}
