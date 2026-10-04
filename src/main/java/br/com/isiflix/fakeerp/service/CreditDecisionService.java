package br.com.isiflix.fakeerp.service;

import br.com.isiflix.fakeerp.dto.ApproveCreditDecisionRequest;
import br.com.isiflix.fakeerp.dto.CreditDecisionRequest;
import br.com.isiflix.fakeerp.entity.CreditDecisionEntity;
import br.com.isiflix.fakeerp.entity.CreditDecisionStatus;
import br.com.isiflix.fakeerp.repository.CompanyRepository;
import br.com.isiflix.fakeerp.repository.CreditDecisionRepository;
import br.com.isiflix.fakeerp.repository.CreditPolicyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * Regras de gravação e aprovação das decisões de crédito.
 * Os guardrails ficam aqui, no servidor — não dependem do prompt do agente.
 */
@Service
public class CreditDecisionService {

    private final CreditDecisionRepository decisionRepository;
    private final CompanyRepository companyRepository;
    private final CreditPolicyRepository policyRepository;

    public CreditDecisionService(CreditDecisionRepository decisionRepository,
                                 CompanyRepository companyRepository,
                                 CreditPolicyRepository policyRepository) {
        this.decisionRepository = decisionRepository;
        this.companyRepository = companyRepository;
        this.policyRepository = policyRepository;
    }

    @Transactional
    public CreditDecisionEntity create(CreditDecisionRequest request, String createdBy) {
        // Guardrail: quem grava (credit:write) nunca aprova; aprovação é só via PATCH /approve.
        if (request.decision() == CreditDecisionStatus.APPROVED) {
            throw unprocessable("A decisão APPROVED não pode ser gravada por este endpoint. "
                    + "Use PENDING_REVIEW ou REJECTED; a aprovação final exige o escopo credit:approve.");
        }
        if (!companyRepository.existsById(request.cnpj())) {
            throw unprocessable("CNPJ não cadastrado: " + request.cnpj());
        }
        if (!policyRepository.existsByPolicyVersion(request.policyVersion())) {
            throw unprocessable("Versão de política inexistente: " + request.policyVersion());
        }

        var latest = decisionRepository.findFirstByCnpjAndReferenceYearAndReferenceMonthOrderByRevisionDesc(
                request.cnpj(), request.referenceYear(), request.referenceMonth());

        int revision = 0;
        if (request.supersedesId() == null) {
            if (latest.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Já existe a decisão " + latest.get().getId() + " para este CNPJ e período. "
                                + "Para corrigi-la, envie supersedesId=" + latest.get().getId() + ".");
            }
        } else {
            CreditDecisionEntity previous = decisionRepository.findById(request.supersedesId())
                    .orElseThrow(() -> unprocessable("supersedesId inexistente: " + request.supersedesId()));
            if (!previous.getCnpj().equals(request.cnpj())
                    || previous.getReferenceYear() != request.referenceYear()
                    || previous.getReferenceMonth() != request.referenceMonth()) {
                throw unprocessable("supersedesId deve referenciar uma decisão do mesmo CNPJ e período.");
            }
            if (!latest.map(CreditDecisionEntity::getId).orElseThrow().equals(previous.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A decisão " + previous.getId() + " já foi substituída. "
                                + "Só a revisão mais recente (" + latest.get().getId() + ") pode ser corrigida.");
            }
            revision = previous.getRevision() + 1;
        }

        var decision = new CreditDecisionEntity(
                request.cnpj(),
                request.referenceYear(),
                request.referenceMonth(),
                revision,
                request.requestedAmount(),
                request.proposedScore(),
                request.decision(),
                request.policyVersion(),
                request.analystAgentId(),
                request.complianceAgentId(),
                request.justification(),
                request.supersedesId(),
                createdBy,
                Instant.now());

        // saveAndFlush: se outra requisição gravou o mesmo período em paralelo,
        // a constraint única estoura aqui e vira 409 no GlobalExceptionHandler.
        return decisionRepository.saveAndFlush(decision);
    }

    @Transactional
    public CreditDecisionEntity approve(Long id, ApproveCreditDecisionRequest request, String approvedBy) {
        if (request.finalDecision() != CreditDecisionStatus.APPROVED
                && request.finalDecision() != CreditDecisionStatus.REJECTED) {
            throw unprocessable("finalDecision deve ser APPROVED ou REJECTED.");
        }

        CreditDecisionEntity decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Decisão de crédito não encontrada: " + id));

        if (decision.getStatus() != CreditDecisionStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Só decisões PENDING_REVIEW podem ser finalizadas. Situação atual: " + decision.getStatus());
        }
        if (decisionRepository.existsBySupersedesId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A decisão " + id + " foi substituída por uma revisão mais recente.");
        }

        decision.finalizeBy(approvedBy, request.finalDecision(), Instant.now());
        return decision;
    }

    private static ResponseStatusException unprocessable(String reason) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, reason);
    }
}
