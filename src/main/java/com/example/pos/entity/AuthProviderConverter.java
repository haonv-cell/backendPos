package com.example.pos.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AuthProviderConverter implements AttributeConverter<AuthProvider, String> {
    
    @Override
    public String convertToDatabaseColumn(AuthProvider provider) {
        if (provider == null) {
            return null;
        }
        return provider.name().toLowerCase();
    }
    
    @Override
    public AuthProvider convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return AuthProvider.valueOf(dbData.toUpperCase());
    }
}

