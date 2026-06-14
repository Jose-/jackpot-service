package com.example.jackpot.contract;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "jackpot.messaging.mode=log")
@AutoConfigureMockMvc
@DisplayName("Swagger UI")
class SwaggerUiTest {
    @Autowired MockMvc mvc;

    @Test
    @DisplayName("Should expose Swagger UI configured with the committed OpenAPI contract")
    void shouldExposeSwaggerUiConfiguredWithCommittedOpenApiContract() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/swagger-ui/index.html"));

        mvc.perform(get("/openapi/jackpot-api.yaml"))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "title: Jackpot Service API")));
    }
}
