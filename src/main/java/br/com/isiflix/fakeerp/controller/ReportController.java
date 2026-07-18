package br.com.isiflix.fakeerp.controller;

import br.com.isiflix.fakeerp.dto.OrderDTO;
import br.com.isiflix.fakeerp.dto.ReportResponse;
import br.com.isiflix.fakeerp.entity.OrderEntity;
import br.com.isiflix.fakeerp.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Relatório de pedidos filtrados por ano/mês. Requer JWT válido.
 */
@RestController
@RequestMapping("/report")
@Tag(name = "Relatório", description = "Relatório de pedidos por ano e mês")
@SecurityRequirement(name = "bearer-jwt")
public class ReportController {

    private final OrderRepository orderRepository;

    public ReportController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/{year}/{month}")
    @Operation(summary = "Recupera os pedidos de tbl_orders filtrados por ano e mês")
    public ReportResponse report(@PathVariable int year, @PathVariable int month) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O mês deve estar entre 1 e 12");
        }

        List<OrderEntity> orders = orderRepository.findByYearAndMonth(year, month);

        List<OrderDTO> items = orders.stream().map(OrderDTO::fromEntity).toList();

        BigDecimal totalValue = orders.stream()
                .map(OrderEntity::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = orders.stream()
                .map(OrderEntity::getDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = orders.stream()
                .map(OrderEntity::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReportResponse(year, month, items.size(), totalValue, totalDiscount, totalAmount, items);
    }
}
