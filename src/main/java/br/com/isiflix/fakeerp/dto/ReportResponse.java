package br.com.isiflix.fakeerp.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Relatório de pedidos de um determinado ano/mês, com totais consolidados.
 */
public record ReportResponse(
        int year,
        int month,
        int count,
        BigDecimal totalValue,
        BigDecimal totalDiscount,
        BigDecimal totalAmount,
        List<OrderDTO> orders) {
}
