package br.com.isiflix.fakeerp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Política de crédito versionada. Mapeada para a tabela tbl_credit_policy.
 */
@Entity
@Table(name = "tbl_credit_policy")
public class CreditPolicyEntity {

    @Id
    private Long id;

    @Column(name = "policy_version", nullable = false)
    private String policyVersion;

    @Column(name = "segment", nullable = false)
    private String segment;

    @Column(name = "min_months_active", nullable = false)
    private int minMonthsActive;

    @Column(name = "min_monthly_revenue", nullable = false)
    private BigDecimal minMonthlyRevenue;

    @Column(name = "max_discount_rate_allowed", nullable = false)
    private BigDecimal maxDiscountRateAllowed;

    @Column(name = "min_orders_count_last_3_months", nullable = false)
    private int minOrdersCountLast3Months;

    @Column(name = "max_requested_amount", nullable = false)
    private BigDecimal maxRequestedAmount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CreditPolicyEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public String getSegment() {
        return segment;
    }

    public int getMinMonthsActive() {
        return minMonthsActive;
    }

    public BigDecimal getMinMonthlyRevenue() {
        return minMonthlyRevenue;
    }

    public BigDecimal getMaxDiscountRateAllowed() {
        return maxDiscountRateAllowed;
    }

    public int getMinOrdersCountLast3Months() {
        return minOrdersCountLast3Months;
    }

    public BigDecimal getMaxRequestedAmount() {
        return maxRequestedAmount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
