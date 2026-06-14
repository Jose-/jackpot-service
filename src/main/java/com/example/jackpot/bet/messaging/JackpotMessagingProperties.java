package com.example.jackpot.bet.messaging;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties("jackpot.messaging")
public class JackpotMessagingProperties {
    @NotNull private MessagingMode mode;
    @NotBlank private String topic;
    @NotBlank private String dltSuffix;

    @Min(1)
    private int partitions;

    @Min(1)
    private int replicationFactor;

    @Min(0)
    private long retryIntervalMs;

    @Min(0)
    private long maxRetries;

    public MessagingMode mode() {
        return mode;
    }

    public void setMode(MessagingMode mode) {
        this.mode = mode;
    }

    public String topic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String dltSuffix() {
        return dltSuffix;
    }

    public void setDltSuffix(String dltSuffix) {
        this.dltSuffix = dltSuffix;
    }

    public int partitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public int replicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public long retryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(long retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public long maxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(long maxRetries) {
        this.maxRetries = maxRetries;
    }
}
