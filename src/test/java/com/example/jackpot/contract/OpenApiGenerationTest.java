package com.example.jackpot.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OpenAPI generation")
class OpenApiGenerationTest {

    private static final Path GENERATED_JAVA =
            Path.of("target/generated-sources/openapi/src/main/java");
    private static final Path SOURCE_CONTRACT = Path.of("src/main/openapi/jackpot-api.yaml");
    private static final Path RUNTIME_CONTRACT =
            Path.of("target/classes/static/openapi/jackpot-api.yaml");

    @Test
    @DisplayName(
            "Should generate only required API interfaces and transport models when the build runs")
    void shouldGenerateOnlyRequiredApiInterfacesAndTransportModelsWhenBuildRuns() {
        assertGenerated("com/example/jackpot/generated/api/BetsApi.java");
        assertGenerated("com/example/jackpot/generated/api/RewardsApi.java");
        assertGenerated("com/example/jackpot/generated/model/PublishBetRequest.java");
        assertGenerated("com/example/jackpot/generated/model/RewardEvaluationResponse.java");
        assertGenerated("com/example/jackpot/generated/model/Problem.java");
        assertNotGenerated("com/example/jackpot/generated/api/JackpotsApi.java");
        assertNotGenerated("com/example/jackpot/generated/model/ContributionResponse.java");
        assertNotGenerated("com/example/jackpot/generated/model/JackpotResponse.java");
    }

    @Test
    @DisplayName(
            "Should copy the single contract source for runtime use when resources are processed")
    void shouldCopySingleContractSourceForRuntimeUseWhenResourcesAreProcessed() throws IOException {
        assertThat(RUNTIME_CONTRACT).exists();
        assertThat(Files.mismatch(SOURCE_CONTRACT, RUNTIME_CONTRACT)).isEqualTo(-1);
    }

    private static void assertGenerated(String relativePath) {
        assertThat(GENERATED_JAVA.resolve(relativePath)).exists().isRegularFile();
    }

    private static void assertNotGenerated(String relativePath) {
        assertThat(GENERATED_JAVA.resolve(relativePath)).doesNotExist();
    }
}
