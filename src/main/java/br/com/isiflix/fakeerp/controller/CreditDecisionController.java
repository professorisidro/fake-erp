package br.com.isiflix.fakeerp.controller;

import br.com.isiflix.fakeerp.dto.ApproveCreditDecisionRequest;
import br.com.isiflix.fakeerp.dto.CreditDecisionDTO;
import br.com.isiflix.fakeerp.dto.CreditDecisionRequest;
import br.com.isiflix.fakeerp.service.CreditDecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Decisões de crédito do squad.
 * <ul>
 *   <li>POST: escopo credit:write (Agente Coordenador) — grava só PENDING_REVIEW ou REJECTED.</li>
 *   <li>PATCH /approve: escopo credit:approve — nenhum agente recebe; só humano.</li>
 * </ul>
 * Não existe DELETE: decisão é auditoria.
 */
@RestController
@RequestMapping("/credit-decision")
@Tag(name = "Compliance", description = "Política de crédito e decisões do squad")
@SecurityRequirement(name = "bearer-jwt")
public class CreditDecisionController {

    private final CreditDecisionService creditDecisionService;

    public CreditDecisionController(CreditDecisionService creditDecisionService) {
        this.creditDecisionService = creditDecisionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Grava a decisão do squad (escopo credit:write). "
            + "Aceita apenas PENDING_REVIEW ou REJECTED.")
    @ApiResponse(responseCode = "201", description = "Decisão gravada")
    @ApiResponse(responseCode = "400", description = "Campos ausentes ou inválidos")
    @ApiResponse(responseCode = "403", description = "Token sem o escopo credit:write")
    @ApiResponse(responseCode = "409", description = "Já existe decisão para o CNPJ/período (use supersedesId para corrigir)")
    @ApiResponse(responseCode = "422", description = "decision=APPROVED, CNPJ não cadastrado ou policyVersion inexistente")
    public CreditDecisionDTO create(@Valid @RequestBody CreditDecisionRequest request,
                                    @Parameter(hidden = true) Authentication authentication) {
        return CreditDecisionDTO.fromEntity(creditDecisionService.create(request, authentication.getName()));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Aprovação/reprovação final por um humano (escopo credit:approve)")
    @ApiResponse(responseCode = "200", description = "Decisão finalizada")
    @ApiResponse(responseCode = "403", description = "Token sem o escopo credit:approve")
    @ApiResponse(responseCode = "404", description = "Decisão não encontrada")
    @ApiResponse(responseCode = "409", description = "Decisão não está PENDING_REVIEW ou já foi substituída")
    @ApiResponse(responseCode = "422", description = "finalDecision diferente de APPROVED/REJECTED")
    public CreditDecisionDTO approve(@PathVariable Long id,
                                     @Valid @RequestBody ApproveCreditDecisionRequest request,
                                     @Parameter(hidden = true) Authentication authentication) {
        return CreditDecisionDTO.fromEntity(
                creditDecisionService.approve(id, request, authentication.getName()));
    }
}
