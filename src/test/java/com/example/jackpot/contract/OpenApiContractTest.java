package com.example.jackpot.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OpenAPI contract")
class OpenApiContractTest {

    private static final Path CONTRACT = Path.of("src/main/openapi/jackpot-api.yaml");

    @Test
    @DisplayName(
            "Should define only the required executable operations when the contract is parsed")
    void shouldDefineOnlyRequiredExecutableOperationsWhenContractIsParsed() {
        OpenAPI openApi = parseContract();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("Jackpot Service API");
        assertThat(openApi.getServers())
                .singleElement()
                .extracting(server -> server.getUrl())
                .isEqualTo("/");
        assertThat(openApi.getSecurity()).isNullOrEmpty();
        assertThat(openApi.getTags())
                .extracting(tag -> tag.getName())
                .containsExactly("Bets", "Rewards");
        assertThat(openApi.getPaths())
                .containsOnlyKeys("/api/v1/bets", "/api/v1/bets/{betId}/evaluation");

        assertOperation(openApi, "/api/v1/bets", "POST", "publishBet");
        assertOperation(
                openApi, "/api/v1/bets/{betId}/evaluation", "POST", "evaluateJackpotReward");

        Set<String> operationIds = new HashSet<>();
        openApi.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .map(Operation::getOperationId)
                .forEach(
                        operationId ->
                                assertThat(operationIds.add(operationId))
                                        .as("operationId %s is unique", operationId)
                                        .isTrue());
    }

    @Test
    @DisplayName(
            "Should define reusable problem responses and validated schemas when the contract is parsed")
    void shouldDefineReusableProblemResponsesAndValidatedSchemasWhenContractIsParsed() {
        OpenAPI openApi = parseContract();

        assertThat(openApi.getComponents().getResponses())
                .containsKeys("BadRequest", "NotFound", "Conflict", "ServiceUnavailable");
        assertThat(openApi.getComponents().getSchemas())
                .containsKeys(
                        "PublishBetRequest",
                        "BetAcceptedResponse",
                        "RewardEvaluationResponse",
                        "Problem");

        Schema<?> request = openApi.getComponents().getSchemas().get("PublishBetRequest");
        assertThat(request.getRequired())
                .containsExactlyInAnyOrder("betId", "userId", "jackpotId", "betAmount");
        assertThat(property(request, "betId").getFormat()).isEqualTo("uuid");
        assertThat(property(request, "userId").getFormat()).isEqualTo("uuid");
        assertThat(property(request, "jackpotId").getFormat()).isEqualTo("uuid");
        assertThat(property(request, "betAmount").getMinimum()).isEqualByComparingTo("0.01");
        assertThat(property(request, "betAmount").getMaximum()).isNotNull();
        assertThat(property(request, "betAmount").getMultipleOf()).isEqualByComparingTo("0.01");
        assertThat(contractText()).contains("maximum: 99999999999999999.99");

        assertThat(openApi.getComponents().getExamples()).containsKey("FixedJackpotBet");
    }

    private static OpenAPI parseContract() {
        var result = new OpenAPIV3Parser().readLocation(CONTRACT.toString(), null, null);

        assertThat(result.getMessages()).isEmpty();
        assertThat(result.getOpenAPI()).isNotNull();
        return result.getOpenAPI();
    }

    private static String contractText() {
        try {
            return Files.readString(CONTRACT);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read OpenAPI contract", exception);
        }
    }

    private static Schema<?> property(Schema<?> schema, String name) {
        return (Schema<?>) schema.getProperties().get(name);
    }

    private static void assertOperation(
            OpenAPI openApi, String path, String method, String operationId) {
        var pathItem = openApi.getPaths().get(path);

        assertThat(pathItem).as("%s exists", path).isNotNull();
        assertThat(pathItem.readOperationsMap().get(PathItem.HttpMethod.valueOf(method)))
                .as("%s supports %s", path, method)
                .isNotNull()
                .extracting(Operation::getOperationId)
                .isEqualTo(operationId);
    }
}
