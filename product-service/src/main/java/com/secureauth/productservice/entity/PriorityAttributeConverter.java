package com.secureauth.productservice.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PriorityAttributeConverter implements AttributeConverter<GageIssue.Priority, String> {
    @Override
    public String convertToDatabaseColumn(GageIssue.Priority attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public GageIssue.Priority convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return GageIssue.Priority.valueOf(dbData.toUpperCase());
    }
}


