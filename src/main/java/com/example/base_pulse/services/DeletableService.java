package com.example.base_pulse.services;

import java.util.List;

import com.example.base_pulse.specifications.SearchCriteria;

public interface DeletableService {

    void delete(Long id);

    void deleteAll(List<SearchCriteria> searchCriterias);
}
