package br.com.isiflix.fakeerp.controller;

import br.com.isiflix.fakeerp.dto.CompanyDTO;
import br.com.isiflix.fakeerp.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Dados cadastrais de empresas PJ. Requer o escopo report:read (Agente Analista).
 */
@RestController
@RequestMapping("/company")
@Tag(name = "Cadastro", description = "Dados cadastrais de empresas (escopo report:read)")
@SecurityRequirement(name = "bearer-jwt")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/{cnpj}")
    @Operation(summary = "Recupera os dados cadastrais de uma empresa pelo CNPJ (14 dígitos, sem máscara)")
    @ApiResponse(responseCode = "200", description = "Empresa encontrada")
    @ApiResponse(responseCode = "400", description = "CNPJ fora do formato de 14 dígitos")
    @ApiResponse(responseCode = "403", description = "Token sem o escopo report:read")
    @ApiResponse(responseCode = "404", description = "CNPJ não cadastrado")
    public CompanyDTO company(@PathVariable String cnpj) {
        if (!cnpj.matches("\\d{14}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O CNPJ deve ter 14 dígitos, sem máscara (ex.: 11111111000191)");
        }

        return companyRepository.findById(cnpj)
                .map(CompanyDTO::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empresa não encontrada para o CNPJ " + cnpj));
    }
}
