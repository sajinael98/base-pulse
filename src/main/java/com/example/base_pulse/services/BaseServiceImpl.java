package com.example.base_pulse.services;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.example.base_pulse.entities.BaseEntity;
import com.example.base_pulse.repositories.GenericJpaRepository;
import com.example.base_pulse.specifications.GenericSpecification;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.ObjectMerger;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

public abstract class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {
    protected final GenericJpaRepository<T> repository;

    protected BaseServiceImpl(GenericJpaRepository<T> repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public T create(T entity) {
        return repository.save(entity);
    }

    @Override
    public T findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity with id " + id + " not found"));
    }

    @Override
    public List<T> findAll(Pageable pageable, List<SearchCriteria> filters, List<SortCriteria> sort) {
        Sort finalSort = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            List<Sort.Order> orders = sort.stream()
                    .map(s -> new Sort.Order(s.getDirection(), s.getField()))
                    .toList();
            finalSort = Sort.by(orders);
        }

        Pageable finalPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                finalSort);

        Page<T> page;
        if (filters == null || filters.isEmpty()) {
            page = repository.findAll(finalPageable);
        } else {
            Specification<T> spec = new GenericSpecification<>(filters);
            page = repository.findAll(spec, finalPageable);
        }

        return page.getContent();
    }

    @Override
    @Transactional
    public T replace(Long id, T fullEntity) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity not found: " + id);
        }
        fullEntity.setId(id);
        return repository.save(fullEntity);
    }

    @Override
    @Transactional
    public T patch(Long id, T partialEntity) {
        T existingEntity = this.findById(id);
        ObjectMerger.mergeNonNullFields(partialEntity, existingEntity);
        return repository.save(existingEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity with id " + id + " not found");
        }
        repository.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> findWithFieldsAndFilters(String entity,
            List<String> fields,
            List<SearchCriteria> filters,
            List<SortCriteria> sort) {
        return repository.fetchValues(entity, fields, filters, sort);
    }

}
