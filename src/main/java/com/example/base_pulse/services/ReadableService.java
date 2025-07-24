package com.example.base_pulse.services;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.example.base_pulse.entities.BaseEntity;
import com.example.base_pulse.specifications.SearchCriteria;

public interface ReadableService<T extends BaseEntity> {

        T findById(Long id);

        List<T> findAll(Pageable pageable, List<SearchCriteria> filters);

        List<Map<String, Object>> findWithFieldsAndFilters(
                        String entityName,
                        List<String> fields,
                        List<SearchCriteria> filters);
}
