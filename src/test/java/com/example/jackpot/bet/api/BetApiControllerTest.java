package com.example.jackpot.bet.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.configuration.JackpotSeedProperties;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "jackpot.messaging.mode=log")
@AutoConfigureMockMvc
@DisplayName("Bet API")
class BetApiControllerTest {
    @Autowired MockMvc mvc;
    @Autowired BetRepository bets;
    @Autowired JackpotSeedProperties seedProperties;

    @Test
    @DisplayName("Should accept a valid bet through the generated API contract")
    void shouldAcceptValidBetThroughGeneratedApiContract() throws Exception {
        var betId = UUID.randomUUID();

        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(betId, UUID.randomUUID(), "10.00")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.betId").value(betId.toString()))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("Should return sanitized field errors when request validation fails")
    void shouldReturnSanitizedFieldErrorsWhenRequestValidationFails() throws Exception {
        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(UUID.randomUUID(), UUID.randomUUID(), "0")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Request validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid"))
                .andExpect(
                        jsonPath("$.errors.betAmount")
                                .value("must be greater than or equal to 0.01"))
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        org.hamcrest.Matchers.not(
                                                org.hamcrest.Matchers.containsString(
                                                        "BetApiController"))));
    }

    @Test
    @DisplayName("Should reject a bet amount with more than two decimal places")
    void shouldRejectBetAmountWithMoreThanTwoDecimalPlaces() throws Exception {
        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(UUID.randomUUID(), UUID.randomUUID(), "10.005")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request body or parameter is invalid"));
    }

    @Test
    @DisplayName("Should reject an oversized bet amount")
    void shouldRejectOversizedBetAmount() throws Exception {
        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        request(
                                                UUID.randomUUID(),
                                                UUID.randomUUID(),
                                                "100000000.00")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("Should return problem details for malformed JSON")
    void shouldReturnProblemDetailsForMalformedJson() throws Exception {
        mvc.perform(post("/api/v1/bets").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Request body or parameter is invalid"));
    }

    @Test
    @DisplayName("Should treat equivalent monetary scales as an idempotent payload")
    void shouldTreatEquivalentMonetaryScalesAsAnIdempotentPayload() throws Exception {
        var betId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(betId, userId, "10")))
                .andExpect(status().isAccepted());
        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(betId, userId, "10.0")))
                .andExpect(status().isAccepted());
        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(betId, userId, "10.00")))
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName(
            "Should return conflict when an existing bet identifier is reused with different data")
    void shouldReturnConflictWhenExistingBetIdentifierIsReusedWithDifferentData() throws Exception {
        var betId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(betId, userId, "10.00")))
                .andExpect(status().isAccepted());

        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(betId, userId, "20.00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Request conflicts with current state"));
    }

    @Test
    @DisplayName(
            "Should return not found without registering the bet when the jackpot does not exist")
    void shouldReturnNotFoundWithoutRegisteringBetWhenJackpotDoesNotExist() throws Exception {
        var betId = UUID.randomUUID();

        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        request(
                                                betId,
                                                UUID.randomUUID(),
                                                UUID.randomUUID(),
                                                "10.00")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Jackpot not found"));

        assertThat(bets.existsById(betId)).isFalse();
    }

    private String request(UUID betId, UUID userId, String amount) {
        return request(betId, userId, seedProperties.fixedId(), amount);
    }

    private String request(UUID betId, UUID userId, UUID jackpotId, String amount) {
        return """
			{"betId":"%s","userId":"%s","jackpotId":"%s","betAmount":%s}
			"""
                .formatted(betId, userId, jackpotId, amount);
    }
}
