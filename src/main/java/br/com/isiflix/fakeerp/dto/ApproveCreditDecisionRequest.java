package br.com.isiflix.fakeerp.dto;

import br.com.isiflix.fakeerp.entity.CreditDecisionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo da aprovação/reprovação final, feita por um humano.
 */
public record ApproveCreditDecisionRequest(
        @Schema(example = "isidro",
                description = "Informativo. Para auditoria, é gravado o usuário autenticado (subject do JWT).")
        String approvedBy,

        @Schema(example = "APPROVED", description = "APPROVED ou REJECTED")
        @NotNull
        CreditDecisionStatus finalDecision) {
}
