package br.com.isiflix.fakeerp.controller;

import br.com.isiflix.fakeerp.dto.OrderDTO;
import br.com.isiflix.fakeerp.dto.ReportResponse;
import br.com.isiflix.fakeerp.entity.OrderEntity;
import br.com.isiflix.fakeerp.repository.CompanyRepository;
import br.com.isiflix.fakeerp.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Relatório de pedidos filtrados por ano/mês e, opcionalmente, pelo CNPJ da empresa
 * que faturou. Requer o escopo report:read.
 */
@RestController
@RequestMapping("/report")
@Tag(name = "Relatório", description = "Relatório de pedidos por ano e mês, filtrável por CNPJ (escopo report:read)")
@SecurityRequirement(name = "bearer-jwt")
public class ReportController {

    private final OrderRepository orderRepository;
    private final CompanyRepository companyRepository;

    public ReportController(OrderRepository orderRepository, CompanyRepository companyRepository) {
        this.orderRepository = orderRepository;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/{year}/{month}")
    @Operation(summary = "Recupera os pedidos de tbl_orders filtrados por ano e mês "
            + "e, opcionalmente, pelo CNPJ da empresa que faturou")
    @ApiResponse(responseCode = "200", description = "Pedidos do período com os totais consolidados")
    @ApiResponse(responseCode = "400", description = "Mês fora de 1..12 ou CNPJ fora do formato de 14 dígitos")
    @ApiResponse(responseCode = "403", description = "Token sem o escopo report:read")
    @ApiResponse(responseCode = "404", description = "CNPJ não cadastrado")
    public ReportResponse report(
            @PathVariable int year,
            @PathVariable int month,
            @Parameter(description = "Opcional: CNPJ (14 dígitos, sem máscara) da empresa que faturou")
            @RequestParam(required = false) String cnpj) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O mês deve estar entre 1 e 12");
        }

        List<OrderEntity> orders;
        if (StringUtils.hasText(cnpj)) {
            if (!cnpj.matches("\\d{14}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O CNPJ deve ter 14 dígitos, sem máscara (ex.: 11111111000191)");
            }
            if (!companyRepository.existsById(cnpj)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empresa não encontrada para o CNPJ " + cnpj);
            }
            orders = orderRepository.findByCnpjAndYearAndMonth(cnpj, year, month);
        } else {
            cnpj = null;
            orders = orderRepository.findByYearAndMonth(year, month);
        }

        List<OrderDTO> items = orders.stream().map(OrderDTO::fromEntity).toList();

        BigDecimal totalValue = orders.stream()
                .map(OrderEntity::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = orders.stream()
                .map(OrderEntity::getDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = orders.stream()
                .map(OrderEntity::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReportResponse(year, month, cnpj, items.size(), totalValue, totalDiscount, totalAmount, items);
    }
}
