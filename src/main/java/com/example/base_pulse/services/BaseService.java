package com.example.base_pulse.services;

import com.example.base_pulse.entities.BaseEntity;

public interface BaseService<T extends BaseEntity>
                extends CreatableService<T>, ReadableService<T>, UpdatableService<T>, DeletableService {
}
