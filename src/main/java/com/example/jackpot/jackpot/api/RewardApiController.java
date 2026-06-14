package com.example.jackpot.jackpot.api;

import com.example.jackpot.generated.api.RewardsApi;
import com.example.jackpot.generated.model.RewardEvaluationResponse;
import com.example.jackpot.jackpot.application.JackpotRewardEvaluationService;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RewardApiController implements RewardsApi {
    private final JackpotRewardEvaluationService service;

    public RewardApiController(JackpotRewardEvaluationService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<RewardEvaluationResponse> evaluateJackpotReward(UUID betId) {
        var result = service.evaluate(betId);
        return ResponseEntity.ok(
                new RewardEvaluationResponse(
                                result.betId(),
                                result.userId(),
                                result.jackpotId(),
                                result.won(),
                                result.calculatedChance(),
                                result.generatedDraw(),
                                result.createdAt().atOffset(ZoneOffset.UTC))
                        .rewardAmount(result.rewardAmount()));
    }
}
