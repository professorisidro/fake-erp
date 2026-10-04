package br.com.isiflix.fakeerp.dto;

import br.com.isiflix.fakeerp.entity.CreditDecisionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Corpo da gravação de uma decisão de crédito pelo squad de agentes.
 */
public record CreditDecisionRequest(
        @Schema(example = "11111111000191", description = "CNPJ com 14 dígitos, sem máscara")
        @NotBlank @Pattern(regexp = "\\d{14}", message = "deve ter 14 dígitos, sem máscara")
        String cnpj,

        @Schema(example = "2026")
        @NotNull @Min(2000) @Max(2100)
        Integer referenceYear,

        @Schema(example = "7")
        @NotNull @Min(1) @Max(12)
        Integer referenceMonth,

        @Schema(example = "120000.00")
        @NotNull @Positive
        BigDecimal requestedAmount,

        @Schema(example = "0.78", description = "Score preliminar entre 0 e 1")
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal proposedScore,

        @Schema(example = "PENDING_REVIEW",
                description = "Somente PENDING_REVIEW ou REJECTED. APPROVED é recusado: a aprovação final é humana.")
        @NotNull
        CreditDecisionStatus decision,

        @Schema(example = "2026.1", description = "Versão da política usada na avaliação (GET /credit-policy)")
        @NotBlank
        String policyVersion,

        @Schema(example = "agent-analista-v1")
        @NotBlank @Size(max = 100)
        String analystAgentId,

        @Schema(example = "agent-compliance-v1")
        @NotBlank @Size(max = 100)
        String complianceAgentId,

        @Schema(example = "Faturamento consistente, dentro da política; requer revisão por valor acima de R$100k.")
        @NotBlank @Size(max = 4000)
        String justification,

        @Schema(description = "Opcional: id da decisão que esta corrige (mesmo CNPJ/período). A anterior nunca é apagada.")
        Long supersedesId) {
}
