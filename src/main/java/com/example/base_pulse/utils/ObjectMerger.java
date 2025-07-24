package com.example.base_pulse.utils;

import java.lang.reflect.*;
import java.util.*;

public class ObjectMerger {

    private static final Set<Class<?>> PRIMITIVE_TYPES = Set.of(
            String.class, Boolean.class, Integer.class, Long.class,
            Short.class, Byte.class, Double.class, Float.class, Character.class
    );

    public static <T> void mergeNonNullFields(T source, T target) {
        mergeNonNullFields(source, target, new IdentityHashMap<>());
    }

    private static <T> void mergeNonNullFields(T source, T target, Map<Object, Object> visited) {
        if (source == null || target == null || visited.containsKey(source)) return;

        visited.put(source, target);

        for (Class<?> clazz = source.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;

                field.setAccessible(true);
                try {
                    Object sourceValue = field.get(source);
                    Object targetValue = field.get(target);

                    if (sourceValue != null) {
                        Class<?> fieldType = field.getType();

                        if (isPrimitiveOrWrapperOrString(fieldType)) {
                            field.set(target, sourceValue);

                        } else if (Collection.class.isAssignableFrom(fieldType)) {
                            if (targetValue == null) {
                                field.set(target, sourceValue);
                            } else {
                                Collection<?> sourceCol = (Collection<?>) sourceValue;
                                Collection targetCol = (Collection) targetValue;
                                targetCol.clear();
                                targetCol.addAll(sourceCol);
                            }

                        } else if (Map.class.isAssignableFrom(fieldType)) {
                            if (targetValue == null) {
                                field.set(target, sourceValue);
                            } else {
                                Map<?, ?> sourceMap = (Map<?, ?>) sourceValue;
                                Map targetMap = (Map) targetValue;
                                targetMap.clear();
                                targetMap.putAll(sourceMap);
                            }

                        } else if (targetValue == null) {
                            field.set(target, sourceValue);
                        } else {
                            mergeNonNullFields(sourceValue, targetValue, visited);
                        }
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to merge field: " + field.getName(), e);
                }
            }
        }
    }

    private static boolean isPrimitiveOrWrapperOrString(Class<?> type) {
        return type.isPrimitive() || PRIMITIVE_TYPES.contains(type);
    }
}
