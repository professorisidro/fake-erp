package br.com.isiflix.fakeerp.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Relatório de pedidos de um determinado ano/mês, com totais consolidados.
 * {@code cnpj} é o filtro aplicado (null quando o relatório inclui todas as empresas).
 */
public record ReportResponse(
        int year,
        int month,
        String cnpj,
        int count,
        BigDecimal totalValue,
        BigDecimal totalDiscount,
        BigDecimal totalAmount,
        List<OrderDTO> orders) {
}
