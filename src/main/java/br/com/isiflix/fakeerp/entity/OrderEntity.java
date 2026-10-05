package br.com.isiflix.fakeerp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pedido. Mapeado para a tabela tbl_orders com as colunas:
 * order_id, cnpj, order_date_time, value, discount, total, status.
 * O cnpj identifica a empresa (tbl_company) que faturou o pedido.
 */
@Entity
@Table(name = "tbl_orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "cnpj", length = 14)
    private String cnpj;

    @Column(name = "order_date_time", nullable = false)
    private LocalDateTime orderDateTime;

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @Column(name = "discount", nullable = false)
    private BigDecimal discount;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "status", nullable = false)
    private String status;

    protected OrderEntity() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getCnpj() {
        return cnpj;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public BigDecimal getValue() {
        return value;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }
}
