package com.example.base_pulse.repositories;

import java.util.List;
import java.util.Map;

import com.example.base_pulse.specifications.SearchCriteria;

public interface GenericJpaRepositoryCustom<T> {
    List<Map<String, Object>> fetchValues(String entityName, List<String> fields, List<SearchCriteria> filters);
}
