package br.com.isiflix.fakeerp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoints do squad de crédito PJ e o controle de acesso por escopo.
 * Cada teste de gravação usa um período (ano/mês) próprio para não colidir com os demais.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CreditSquadApiTests {

    private static final String HEALTHY_CNPJ = "11111111000191";
    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    @Autowired
    private MockMvc mvc;

    // ------------------------------------------------------------------ login

    @Test
    void loginReturnsUserScopes() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"agent-analista\",\"password\":\"analista\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("report:read"));
    }

    // --------------------------------------------------------------- company

    @Test
    void analystReadsCompany() throws Exception {
        mvc.perform(get("/company/" + HEALTHY_CNPJ).header("Authorization", bearer("agent-analista", "analista")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.corporateName").value("Comercio Fake Ltda"))
                .andExpect(jsonPath("$.tradeName").value("Fake Comercio"))
                .andExpect(jsonPath("$.segment").value("varejo"))
                .andExpect(jsonPath("$.foundedAt").value("2015-03-10"))
                .andExpect(jsonPath("$.declaredMonthlyRevenue").value(180000.00));
    }

    @Test
    void companyNotFoundAndInvalidCnpj() throws Exception {
        String token = bearer("agent-analista", "analista");
        mvc.perform(get("/company/99999999000199").header("Authorization", token))
                .andExpect(status().isNotFound());
        mvc.perform(get("/company/11.111.111/0001-91").header("Authorization", token))
                .andExpect(status().is4xxClientError());
        mvc.perform(get("/company/123").header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void complianceCannotReadCompanyOrReport() throws Exception {
        String token = bearer("agent-compliance", "compliance");
        mvc.perform(get("/company/" + HEALTHY_CNPJ).header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(containsString("escopo")));
        mvc.perform(get("/report/2026/1").header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/company/" + HEALTHY_CNPJ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reportStillWorksForLegacyUsers() throws Exception {
        mvc.perform(get("/report/2026/1").header("Authorization", bearer("user", "user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(4));
        mvc.perform(get("/report/2026/1").header("Authorization", bearer("admin", "admin")))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------- report por cnpj

    @Test
    void reportWithoutFilterIncludesAllCompanies() throws Exception {
        // jul/2026: 2 pedidos originais + 6 (saudável) + 4 (limite) + 3 (divergente)
        mvc.perform(get("/report/2026/7").header("Authorization", bearer("agent-analista", "analista")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").doesNotExist())
                .andExpect(jsonPath("$.count").value(15))
                .andExpect(jsonPath("$.orders[0].cnpj").isNotEmpty());
    }

    @Test
    void reportFilteredByCnpj() throws Exception {
        String token = bearer("agent-analista", "analista");
        mvc.perform(get("/report/2026/7").param("cnpj", "22222222000172").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cnpj").value("22222222000172"))
                .andExpect(jsonPath("$.count").value(4))
                .andExpect(jsonPath("$.totalAmount").value(48700.00))
                .andExpect(jsonPath("$.orders[*].cnpj", everyItem(is("22222222000172"))));

        mvc.perform(get("/report/2026/7").param("cnpj", HEALTHY_CNPJ).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(8))
                .andExpect(jsonPath("$.totalAmount").value(180300.00));

        mvc.perform(get("/report/2026/8").param("cnpj", "33333333000153").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.totalAmount").value(39800.00));
    }

    @Test
    void reportCnpjValidation() throws Exception {
        String token = bearer("agent-analista", "analista");
        mvc.perform(get("/report/2026/7").param("cnpj", "123").header("Authorization", token))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/report/2026/7").param("cnpj", "99999999000199").header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------- credit-policy

    @Test
    void complianceReadsPolicyWithFallbackToGeneral() throws Exception {
        String token = bearer("agent-compliance", "compliance");
        mvc.perform(get("/credit-policy").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyVersion").value("2026.1"))
                .andExpect(jsonPath("$.segment").value("geral"))
                .andExpect(jsonPath("$.minMonthsActive").value(12))
                .andExpect(jsonPath("$.maxDiscountRateAllowed").value(0.15))
                .andExpect(jsonPath("$.minOrdersCountLast3Months").value(10))
                .andExpect(jsonPath("$.maxRequestedAmount").value(300000.00))
                .andExpect(jsonPath("$.updatedAt").value("2026-01-05T00:00:00Z"));
        mvc.perform(get("/credit-policy").param("segment", "varejo").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.segment").value("geral"));
    }

    @Test
    void analystCannotReadPolicy() throws Exception {
        mvc.perform(get("/credit-policy").header("Authorization", bearer("agent-analista", "analista")))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------- credit-decision

    @Test
    void coordinatorCreatesPendingDecision() throws Exception {
        mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision(2026, 1, "PENDING_REVIEW", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.cnpj").value(HEALTHY_CNPJ))
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.revision").value(0))
                .andExpect(jsonPath("$.createdBy").value("agent-coordenador"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void agentCannotRecordApproved() throws Exception {
        mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision(2026, 2, "APPROVED", null)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(containsString("credit:approve")));
    }

    @Test
    void duplicateDecisionIsConflictAndSupersedesCreatesRevision() throws Exception {
        long first = createDecision(2026, 3, "PENDING_REVIEW", null);

        mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision(2026, 3, "PENDING_REVIEW", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("supersedesId=" + first)));

        long second = createDecision(2026, 3, "REJECTED", first);

        // Só a revisão mais recente pode ser corrigida
        mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision(2026, 3, "REJECTED", first)))
                .andExpect(status().isConflict());

        // A revisão substituída não pode mais ser aprovada
        mvc.perform(patch("/credit-decision/" + first + "/approve").header("Authorization", bearer("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"finalDecision\":\"APPROVED\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision(2026, 3, "REJECTED", second)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.revision").value(2))
                .andExpect(jsonPath("$.supersedesId").value(second));
    }

    @Test
    void invalidDecisionPayloads() throws Exception {
        String unknownCnpj = decision(2026, 4, "PENDING_REVIEW", null).replace(HEALTHY_CNPJ, "99999999000199");
        mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON).content(unknownCnpj))
                .andExpect(status().isUnprocessableContent());

        String unknownPolicy = decision(2026, 4, "PENDING_REVIEW", null).replace("2026.1", "1999.9");
        mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON).content(unknownPolicy))
                .andExpect(status().isUnprocessableContent());

        String badMonthAndScore = decision(2026, 13, "PENDING_REVIEW", null).replace("0.78", "1.5");
        mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON).content(badMonthAndScore))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("referenceMonth")))
                .andExpect(jsonPath("$.detail").value(containsString("proposedScore")));

        String badEnum = decision(2026, 4, "MAYBE", null);
        mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON).content(badEnum))
                .andExpect(status().isBadRequest());
    }

    @Test
    void onlyCoordinatorScopeCanWrite() throws Exception {
        mvc.perform(post("/credit-decision").header("Authorization", bearer("agent-analista", "analista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision(2026, 5, "PENDING_REVIEW", null)))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------- aprovação humana (approve)

    @Test
    void agentsCannotApproveButHumanCan() throws Exception {
        long id = createDecision(2026, 6, "PENDING_REVIEW", null);
        String body = "{\"approvedBy\":\"quem-quer-que-seja\",\"finalDecision\":\"APPROVED\"}";

        for (String[] agent : new String[][]{
                {"agent-analista", "analista"}, {"agent-compliance", "compliance"}, {"agent-coordenador", "coordenador"}}) {
            mvc.perform(patch("/credit-decision/" + id + "/approve").header("Authorization", bearer(agent[0], agent[1]))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
        }

        mvc.perform(patch("/credit-decision/" + id + "/approve").header("Authorization", bearer("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.decision").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.approvedBy").value("admin"))
                .andExpect(jsonPath("$.approvedAt").isNotEmpty());

        // Já finalizada -> não pode ser finalizada de novo
        mvc.perform(patch("/credit-decision/" + id + "/approve").header("Authorization", bearer("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"finalDecision\":\"REJECTED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void approveValidations() throws Exception {
        String admin = bearer("admin", "admin");
        mvc.perform(patch("/credit-decision/999999/approve").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"finalDecision\":\"APPROVED\"}"))
                .andExpect(status().isNotFound());

        long id = createDecision(2026, 7, "PENDING_REVIEW", null);
        mvc.perform(patch("/credit-decision/" + id + "/approve").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"finalDecision\":\"PENDING_REVIEW\"}"))
                .andExpect(status().isUnprocessableContent());

        long rejected = createDecision(2026, 8, "REJECTED", null);
        mvc.perform(patch("/credit-decision/" + rejected + "/approve").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"finalDecision\":\"APPROVED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("REJECTED")));
    }

    // ---------------------------------------------------------------- helpers

    private String coordinator() throws Exception {
        return bearer("agent-coordenador", "coordenador");
    }

    private String bearer(String login, String password) throws Exception {
        MvcResult result = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"%s\",\"password\":\"%s\"}".formatted(login, password)))
                .andExpect(status().isOk())
                .andReturn();
        Matcher m = TOKEN.matcher(result.getResponse().getContentAsString());
        if (!m.find()) {
            throw new IllegalStateException("Token não encontrado na resposta do login");
        }
        return "Bearer " + m.group(1);
    }

    private long createDecision(int year, int month, String decision, Long supersedesId) throws Exception {
        MvcResult result = mvc.perform(post("/credit-decision").header("Authorization", coordinator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decision(year, month, decision, supersedesId)))
                .andExpect(status().isCreated())
                .andReturn();
        Matcher m = ID.matcher(result.getResponse().getContentAsString());
        if (!m.find()) {
            throw new IllegalStateException("id não encontrado na resposta");
        }
        return Long.parseLong(m.group(1));
    }

    private static String decision(int year, int month, String decision, Long supersedesId) {
        return """
                {
                  "cnpj": "%s",
                  "referenceYear": %d,
                  "referenceMonth": %d,
                  "requestedAmount": 120000.00,
                  "proposedScore": 0.78,
                  "decision": "%s",
                  "policyVersion": "2026.1",
                  "analystAgentId": "agent-analista-v1",
                  "complianceAgentId": "agent-compliance-v1",
                  "justification": "Faturamento consistente, dentro da política.",
                  "supersedesId": %s
                }""".formatted(HEALTHY_CNPJ, year, month, decision, supersedesId == null ? "null" : supersedesId);
    }
}
