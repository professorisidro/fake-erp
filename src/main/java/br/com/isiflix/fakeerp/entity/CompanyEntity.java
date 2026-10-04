package br.com.isiflix.fakeerp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Empresa (cadastro PJ). Mapeada para a tabela tbl_company.
 */
@Entity
@Table(name = "tbl_company")
public class CompanyEntity {

    @Id
    @Column(name = "cnpj", length = 14)
    private String cnpj;

    @Column(name = "corporate_name", nullable = false)
    private String corporateName;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "segment", nullable = false)
    private String segment;

    @Column(name = "founded_at", nullable = false)
    private LocalDate foundedAt;

    @Column(name = "declared_monthly_revenue", nullable = false)
    private BigDecimal declaredMonthlyRevenue;

    protected CompanyEntity() {
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public String getSegment() {
        return segment;
    }

    public LocalDate getFoundedAt() {
        return foundedAt;
    }

    public BigDecimal getDeclaredMonthlyRevenue() {
        return declaredMonthlyRevenue;
    }
}
