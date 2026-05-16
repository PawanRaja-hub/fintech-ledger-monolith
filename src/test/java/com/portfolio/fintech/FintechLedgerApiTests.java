package com.portfolio.fintech;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:api-tests;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FintechLedgerApiTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void servesFrontendShellWithoutAuthentication() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Fintech Ledger")));
    }

    @Test
    void loginWalletFundingTransferAndIdempotentReplayWork() throws Exception {
        String adminToken = login("admin@demo.local");
        String aliceToken = login("alice@demo.local");

        JsonNode wallets = json(mvc.perform(get("/api/admin/wallets")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).path("data");

        long aliceWalletId = walletId(wallets, "alice@demo.local");
        long bobWalletId = walletId(wallets, "bob@demo.local");

        mvc.perform(post("/api/admin/wallets/fund")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"walletId":%d,"amount":500.00}
                                """.formatted(aliceWalletId)))
                .andExpect(status().isOk());

        String transferBody = """
                {"toWalletId":%d,"amount":125.50,"memo":"Automation test"}
                """.formatted(bobWalletId);

        JsonNode first = json(mvc.perform(post("/api/payments/transfers")
                        .header("Authorization", bearer(aliceToken))
                        .header("Idempotency-Key", "api-test-transfer-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        JsonNode replay = json(mvc.perform(post("/api/payments/transfers")
                        .header("Authorization", bearer(aliceToken))
                        .header("Idempotency-Key", "api-test-transfer-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertThat(replay.path("data").path("reference").asText())
                .isEqualTo(first.path("data").path("reference").asText());
    }

    @Test
    void customerCannotUseAdminWalletList() throws Exception {
        String aliceToken = login("alice@demo.local");

        mvc.perform(get("/api/admin/wallets")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/wallets/recipients")
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk());
    }

    private String login(String email) throws Exception {
        String response = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password@123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json(response).path("data").path("token").asText();
    }

    private JsonNode json(String response) throws Exception {
        return mapper.readTree(response);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private long walletId(JsonNode wallets, String ownerEmail) {
        for (JsonNode wallet : wallets) {
            if (ownerEmail.equals(wallet.path("ownerEmail").asText())) {
                return wallet.path("walletId").asLong();
            }
        }
        throw new AssertionError("Wallet not found for " + ownerEmail);
    }
}
