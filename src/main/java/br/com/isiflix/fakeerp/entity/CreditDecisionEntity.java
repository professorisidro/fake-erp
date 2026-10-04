package br.com.isiflix.fakeerp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Decisão de crédito do squad. Mapeada para a tabela tbl_credit_decision.
 * <p>
 * {@code decision} guarda o que o squad gravou (imutável, para auditoria);
 * {@code status} é a situação atual, alterada apenas pela aprovação humana.
 * Correções não alteram o registro: viram uma nova revisão com {@code supersedesId}.
 */
@Entity
@Table(name = "tbl_credit_decision")
public class CreditDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cnpj", nullable = false, length = 14)
    private String cnpj;

    @Column(name = "reference_year", nullable = false)
    private int referenceYear;

    @Column(name = "reference_month", nullable = false)
    private int referenceMonth;

    @Column(name = "revision", nullable = false)
    private int revision;

    @Column(name = "requested_amount", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "proposed_score", nullable = false)
    private BigDecimal proposedScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private CreditDecisionStatus decision;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CreditDecisionStatus status;

    @Column(name = "policy_version", nullable = false)
    private String policyVersion;

    @Column(name = "analyst_agent_id", nullable = false)
    private String analystAgentId;

    @Column(name = "compliance_agent_id", nullable = false)
    private String complianceAgentId;

    @Column(name = "justification", nullable = false, length = 4000)
    private String justification;

    @Column(name = "supersedes_id")
    private Long supersedesId;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    protected CreditDecisionEntity() {
    }

    public CreditDecisionEntity(String cnpj, int referenceYear, int referenceMonth, int revision,
                                BigDecimal requestedAmount, BigDecimal proposedScore,
                                CreditDecisionStatus decision, String policyVersion,
                                String analystAgentId, String complianceAgentId, String justification,
                                Long supersedesId, String createdBy, Instant createdAt) {
        this.cnpj = cnpj;
        this.referenceYear = referenceYear;
        this.referenceMonth = referenceMonth;
        this.revision = revision;
        this.requestedAmount = requestedAmount;
        this.proposedScore = proposedScore;
        this.decision = decision;
        this.status = decision;
        this.policyVersion = policyVersion;
        this.analystAgentId = analystAgentId;
        this.complianceAgentId = complianceAgentId;
        this.justification = justification;
        this.supersedesId = supersedesId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    /**
     * Registra a decisão final tomada por um humano.
     */
    public void finalizeBy(String approvedBy, CreditDecisionStatus finalDecision, Instant approvedAt) {
        this.status = finalDecision;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public int getReferenceYear() {
        return referenceYear;
    }

    public int getReferenceMonth() {
        return referenceMonth;
    }

    public int getRevision() {
        return revision;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public BigDecimal getProposedScore() {
        return proposedScore;
    }

    public CreditDecisionStatus getDecision() {
        return decision;
    }

    public CreditDecisionStatus getStatus() {
        return status;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public String getAnalystAgentId() {
        return analystAgentId;
    }

    public String getComplianceAgentId() {
        return complianceAgentId;
    }

    public String getJustification() {
        return justification;
    }

    public Long getSupersedesId() {
        return supersedesId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }
}
