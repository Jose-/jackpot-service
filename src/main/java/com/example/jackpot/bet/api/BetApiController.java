package com.example.jackpot.bet.api;

import com.example.jackpot.bet.application.PublishBetCommand;
import com.example.jackpot.bet.application.PublishBetService;
import com.example.jackpot.generated.api.BetsApi;
import com.example.jackpot.generated.model.BetAcceptedResponse;
import com.example.jackpot.generated.model.PublishBetRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BetApiController implements BetsApi {
    private final PublishBetService service;

    public BetApiController(PublishBetService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<BetAcceptedResponse> publishBet(PublishBetRequest request) {
        var result =
                service.publish(
                        new PublishBetCommand(
                                request.getBetId(),
                                request.getUserId(),
                                request.getJackpotId(),
                                request.getBetAmount()));
        return ResponseEntity.accepted()
                .body(
                        new BetAcceptedResponse(
                                result.betId(),
                                BetAcceptedResponse.StatusEnum.fromValue(result.status().name())));
    }
}
