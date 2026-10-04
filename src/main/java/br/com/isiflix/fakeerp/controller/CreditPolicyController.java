package br.com.isiflix.fakeerp.controller;

import br.com.isiflix.fakeerp.dto.CreditPolicyDTO;
import br.com.isiflix.fakeerp.entity.CreditPolicyEntity;
import br.com.isiflix.fakeerp.repository.CreditPolicyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Política de crédito vigente. Requer o escopo policy:read (Agente Compliance).
 * Somente leitura: a política é carga fixa (data.sql).
 */
@RestController
@RequestMapping("/credit-policy")
@Tag(name = "Compliance", description = "Política de crédito e decisões do squad")
@SecurityRequirement(name = "bearer-jwt")
public class CreditPolicyController {

    private static final String GENERAL_SEGMENT = "geral";

    private final CreditPolicyRepository policyRepository;

    public CreditPolicyController(CreditPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @GetMapping
    @Operation(summary = "Recupera a política de crédito vigente (escopo policy:read). "
            + "Sem política específica para o segmento, retorna a política geral.")
    @ApiResponse(responseCode = "200", description = "Política vigente")
    @ApiResponse(responseCode = "403", description = "Token sem o escopo policy:read")
    public CreditPolicyDTO policy(
            @Parameter(description = "Segmento da empresa (ex.: varejo). Opcional.")
            @RequestParam(required = false) String segment) {

        var policy = StringUtils.hasText(segment)
                ? policyRepository.findFirstBySegmentIgnoreCaseOrderByUpdatedAtDesc(segment.trim())
                : Optional.<CreditPolicyEntity>empty();

        return policy
                .or(() -> policyRepository.findFirstBySegmentIgnoreCaseOrderByUpdatedAtDesc(GENERAL_SEGMENT))
                .map(CreditPolicyDTO::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Nenhuma política de crédito cadastrada"));
    }
}
