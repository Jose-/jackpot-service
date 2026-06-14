package com.example.jackpot.bet.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

@DisplayName("Application messaging defaults")
class ApplicationMessagingDefaultsTest {
    @Test
    @DisplayName("Should define every messaging setting in application YAML")
    void shouldDefineEveryMessagingSettingInApplicationYaml() {
        var yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource("src/main/resources/application.yml"));
        var properties = yaml.getObject();

        assertThat(properties).isNotNull();
        assertThat(properties.getProperty("jackpot.messaging.mode")).isEqualTo("kafka");
        assertThat(properties)
                .containsEntry("jackpot.messaging.topic", "jackpot-bets")
                .containsEntry("jackpot.messaging.dlt-suffix", ".DLT")
                .containsEntry("jackpot.messaging.partitions", 3)
                .containsEntry("jackpot.messaging.replication-factor", 1)
                .containsEntry("jackpot.messaging.retry-interval-ms", 1000)
                .containsEntry("jackpot.messaging.max-retries", 3)
                .containsEntry("jackpot.messaging.recovery-interval-ms", 30000);
    }
}
