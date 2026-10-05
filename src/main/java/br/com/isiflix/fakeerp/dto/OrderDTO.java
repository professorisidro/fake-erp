package br.com.isiflix.fakeerp.dto;

import br.com.isiflix.fakeerp.entity.OrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representação de um pedido no relatório.
 */
public record OrderDTO(
        Long orderId,
        String cnpj,
        LocalDateTime orderDateTime,
        BigDecimal value,
        BigDecimal discount,
        BigDecimal total,
        String status) {

    public static OrderDTO fromEntity(OrderEntity o) {
        return new OrderDTO(
                o.getOrderId(),
                o.getCnpj(),
                o.getOrderDateTime(),
                o.getValue(),
                o.getDiscount(),
                o.getTotal(),
                o.getStatus());
    }
}
