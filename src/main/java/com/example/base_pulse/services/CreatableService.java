package com.example.base_pulse.services;

import com.example.base_pulse.entities.BaseEntity;

public interface CreatableService<T extends BaseEntity> {
    T create(T entity);
}
