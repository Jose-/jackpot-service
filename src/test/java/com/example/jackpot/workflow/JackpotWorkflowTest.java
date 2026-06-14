package com.example.jackpot.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jackpot.JackpotApplication;
import com.example.jackpot.bet.messaging.BetPublisher;
import com.example.jackpot.configuration.JackpotSeedProperties;
import com.example.jackpot.jackpot.application.JackpotBetProcessingService;
import com.example.jackpot.jackpot.application.ProcessBetContributionCommand;
import com.example.jackpot.jackpot.persistence.JackpotContributionRepository;
import com.example.jackpot.jackpot.persistence.JackpotRewardEvaluationRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = "jackpot.messaging.mode=log",
        classes = {
            JackpotApplication.class,
            JackpotWorkflowTest.SynchronousMessagingConfiguration.class
        })
@AutoConfigureMockMvc
@DisplayName("Jackpot end-to-end workflow")
class JackpotWorkflowTest {
    @Autowired MockMvc mvc;
    @Autowired JackpotSeedProperties seedProperties;
    @Autowired JackpotContributionRepository contributions;
    @Autowired JackpotRewardEvaluationRepository evaluations;

    @Test
    @DisplayName("Should automatically contribute and evaluate when a bet is published")
    void shouldAutomaticallyContributeAndEvaluateWhenBetIsPublished() throws Exception {
        var betId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        mvc.perform(
                        post("/api/v1/bets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
					{"betId":"%s","userId":"%s","jackpotId":"%s","betAmount":10.00}
					"""
                                                .formatted(
                                                        betId, userId, seedProperties.fixedId())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("CONTRIBUTED"));

        assertThat(contributions.findByBetId(betId)).isPresent();
        assertThat(evaluations.findByBetId(betId)).isPresent();

        mvc.perform(post("/api/v1/bets/{betId}/evaluation", betId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.betId").value(betId.toString()));
    }

    @TestConfiguration
    static class SynchronousMessagingConfiguration {
        @Bean
        @Primary
        BetPublisher synchronousBetPublisher(JackpotBetProcessingService service) {
            return command -> {
                service.process(
                        new ProcessBetContributionCommand(
                                command.betId(),
                                command.userId(),
                                command.jackpotId(),
                                command.amount()));
                return com.example.jackpot.jackpot.domain.BetStatus.CONTRIBUTED;
            };
        }
    }
}
