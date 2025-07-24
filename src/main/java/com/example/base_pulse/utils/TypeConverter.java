package com.example.base_pulse.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TypeConverter {

    private TypeConverter() {
    }

    public static Object parseValue(Object value, Class<?> type) {
        if (value == null)
            return null;

        if (type.isInstance(value)) {
            return value;
        }

        String str = value.toString();

        if (type == String.class)
            return str;
        if (type == Integer.class || type == int.class)
            return Integer.parseInt(str);
        if (type == Long.class || type == long.class)
            return Long.parseLong(str);
        if (type == Boolean.class || type == boolean.class)
            return Boolean.parseBoolean(str);
        if (type == Double.class || type == double.class)
            return Double.parseDouble(str);
        if (type == LocalDate.class)
            return LocalDate.parse(str);
        if (type == LocalDateTime.class)
            return LocalDateTime.parse(str);

        // Enum support
        if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, str.toUpperCase());
        }

        throw new IllegalArgumentException("Unsupported field type: " + type.getName());
    }
}
