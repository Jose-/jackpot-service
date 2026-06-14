package com.example.jackpot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootTest(properties = "jackpot.messaging.mode=log")
@DisplayName("Jackpot application")
class JackpotApplicationTest {
    @Autowired ApplicationContext context;
    @Autowired Environment environment;

    @Test
    @DisplayName("Should load the application context when the application starts")
    void shouldLoadApplicationContextWhenApplicationStarts() {}

    @Test
    @DisplayName("Should keep the H2 console disabled when the dev profile is not active")
    void shouldKeepH2ConsoleDisabledWhenDevProfileIsNotActive() {
        assertThat(environment.getProperty("spring.h2.console.enabled", Boolean.class)).isFalse();
        assertThat(context.containsBean("h2Console")).isFalse();
    }
}
