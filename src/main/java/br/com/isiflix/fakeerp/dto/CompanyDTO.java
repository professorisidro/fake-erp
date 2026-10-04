package br.com.isiflix.fakeerp.dto;

import br.com.isiflix.fakeerp.entity.CompanyEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados cadastrais de uma empresa. {@code declaredMonthlyRevenue} é o faturamento
 * que a empresa declara — para ser confrontado com o relatório de pedidos.
 */
public record CompanyDTO(
        String cnpj,
        String corporateName,
        String tradeName,
        String segment,
        LocalDate foundedAt,
        BigDecimal declaredMonthlyRevenue) {

    public static CompanyDTO fromEntity(CompanyEntity c) {
        return new CompanyDTO(
                c.getCnpj(),
                c.getCorporateName(),
                c.getTradeName(),
                c.getSegment(),
                c.getFoundedAt(),
                c.getDeclaredMonthlyRevenue());
    }
}
