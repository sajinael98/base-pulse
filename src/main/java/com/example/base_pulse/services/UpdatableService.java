package com.example.base_pulse.services;

import com.example.base_pulse.entities.BaseEntity;

public interface UpdatableService<T extends BaseEntity> {
    T replace(Long id, T fullEntity);
    T patch(Long id, T partialEntity);
}
