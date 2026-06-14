package com.example.jackpot.jackpot.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;
import java.util.Map;

@Converter
public class ContributionParametersConverter
        implements AttributeConverter<Map<String, BigDecimal>, String> {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, BigDecimal>> PARAMETERS_TYPE =
            new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, BigDecimal> parameters) {
        try {
            return JSON.writeValueAsString(parameters);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Could not serialize contribution parameters", exception);
        }
    }

    @Override
    public Map<String, BigDecimal> convertToEntityAttribute(String value) {
        try {
            return JSON.readValue(value, PARAMETERS_TYPE);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Could not deserialize contribution parameters", exception);
        }
    }
}
