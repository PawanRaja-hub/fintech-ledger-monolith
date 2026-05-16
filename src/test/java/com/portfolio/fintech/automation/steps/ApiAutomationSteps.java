package com.portfolio.fintech.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiAutomationSteps {

    private final String baseUrl = System.getProperty("app.baseUrl", "http://127.0.0.1:8081");
    private final Map<String, String> tokens = new HashMap<>();
    private final Map<String, Long> wallets = new HashMap<>();

    private String idempotencyKey;
    private String transferReference;
    private Response transferResponse;

    @Given("the fintech ledger API is available")
    public void theFintechLedgerApiIsAvailable() {
        RestAssured.baseURI = baseUrl;
        RestAssured.get("/index.html").then().statusCode(200);
    }

    @When("the admin signs in through the API")
    public void theAdminSignsInThroughTheApi() {
        tokens.put("admin", login("admin@demo.local"));
    }

    @When("Alice signs in through the API")
    public void aliceSignsInThroughTheApi() {
        tokens.put("alice", login("alice@demo.local"));
    }

    @When("the admin loads the wallet directory")
    public void theAdminLoadsTheWalletDirectory() {
        Response response = RestAssured.given()
                .header("Authorization", bearer(tokens.get("admin")))
                .get("/api/admin/wallets")
                .then()
                .statusCode(200)
                .extract().response();

        response.jsonPath().getList("data").forEach(item -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> wallet = (Map<String, Object>) item;
            wallets.put((String) wallet.get("ownerEmail"), ((Number) wallet.get("walletId")).longValue());
        });
    }

    @When("the admin funds Alice with {bigdecimal}")
    public void theAdminFundsAliceWith(BigDecimal amount) {
        RestAssured.given()
                .header("Authorization", bearer(tokens.get("admin")))
                .contentType(ContentType.JSON)
                .body(Map.of("walletId", wallets.get("alice@demo.local"), "amount", amount))
                .post("/api/admin/wallets/fund")
                .then()
                .statusCode(200);
    }

    @When("Alice transfers {bigdecimal} to Bob through the API")
    public void aliceTransfersToBobThroughTheApi(BigDecimal amount) {
        idempotencyKey = "cucumber-api-" + UUID.randomUUID();
        transferResponse = RestAssured.given()
                .header("Authorization", bearer(tokens.get("alice")))
                .header("Idempotency-Key", idempotencyKey)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "toWalletId", wallets.get("bob@demo.local"),
                        "amount", amount,
                        "memo", "Cucumber API transfer"
                ))
                .post("/api/payments/transfers")
                .then()
                .statusCode(200)
                .extract().response();
        transferReference = transferResponse.jsonPath().getString("data.reference");
    }

    @Then("the API transfer should be completed")
    public void theApiTransferShouldBeCompleted() {
        assertThat(transferResponse.jsonPath().getString("data.status")).isEqualTo("COMPLETED");
        assertThat(transferReference).startsWith("PAY-");
    }

    @Then("replaying the same API transfer should return the same payment reference")
    public void replayingTheSameApiTransferShouldReturnTheSamePaymentReference() {
        Response replay = RestAssured.given()
                .header("Authorization", bearer(tokens.get("alice")))
                .header("Idempotency-Key", idempotencyKey)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "toWalletId", wallets.get("bob@demo.local"),
                        "amount", new BigDecimal("25.50"),
                        "memo", "Cucumber API transfer"
                ))
                .post("/api/payments/transfers")
                .then()
                .statusCode(200)
                .extract().response();

        assertThat(replay.jsonPath().getString("data.reference")).isEqualTo(transferReference);
    }

    @Then("the reconciliation API should show every wallet matched")
    public void theReconciliationApiShouldShowEveryWalletMatched() {
        Response response = RestAssured.given()
                .header("Authorization", bearer(tokens.get("admin")))
                .get("/api/admin/reconciliation")
                .then()
                .statusCode(200)
                .extract().response();

        assertThat(response.jsonPath().getList("data.matched", Boolean.class))
                .isNotEmpty()
                .allMatch(Boolean.TRUE::equals);
    }

    private String login(String email) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", "Password@123"))
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("data.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
