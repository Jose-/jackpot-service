package com.example.jackpot.jackpot.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jackpot.bet.persistence.BetEntity;
import com.example.jackpot.bet.persistence.BetRepository;
import com.example.jackpot.configuration.JackpotSeedProperties;
import com.example.jackpot.jackpot.application.JackpotContributionService;
import com.example.jackpot.jackpot.application.ProcessBetContributionCommand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "jackpot.messaging.mode=log")
@AutoConfigureMockMvc
@DisplayName("Reward API")
class RewardApiControllerTest {
    @Autowired MockMvc mvc;
    @Autowired BetRepository bets;
    @Autowired JackpotContributionService contributionService;
    @Autowired JackpotSeedProperties seedProperties;

    @Test
    @DisplayName("Should return a persisted reward evaluation through the generated API contract")
    void shouldReturnPersistedRewardEvaluationThroughGeneratedApiContract() throws Exception {
        var betId = createBet(true);

        mvc.perform(post("/api/v1/bets/{betId}/evaluation", betId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.betId").value(betId.toString()))
                .andExpect(jsonPath("$.jackpotId").value(seedProperties.fixedId().toString()))
                .andExpect(jsonPath("$.calculatedChance").value(2.5))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("Should return conflict when the bet has not contributed yet")
    void shouldReturnConflictWhenBetHasNotContributedYet() throws Exception {
        var betId = createBet(false);

        mvc.perform(post("/api/v1/bets/{betId}/evaluation", betId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Request conflicts with current state"));
    }

    private UUID createBet(boolean contribute) {
        var betId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var amount = new BigDecimal("10.00");
        bets.saveAndFlush(
                new BetEntity(betId, userId, seedProperties.fixedId(), amount, Instant.now()));
        if (contribute) {
            contributionService.process(
                    new ProcessBetContributionCommand(
                            betId, userId, seedProperties.fixedId(), amount));
        }
        return betId;
    }
}
