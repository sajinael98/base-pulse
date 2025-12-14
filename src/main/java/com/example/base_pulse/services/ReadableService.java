package com.example.base_pulse.services;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.example.base_pulse.entities.BaseEntity;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;

public interface ReadableService<T extends BaseEntity> {

        T findById(Long id);

        PageResult<T> findAll(
                        Pageable pageable,
                        List<SearchCriteria> filters,
                        List<SortCriteria> sort);

        List<T> findAll();

        List<T> findAllByIds(List<Long> ids);

        boolean exists(Long id);

        long count();
}
