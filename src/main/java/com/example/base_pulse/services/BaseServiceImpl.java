package com.example.base_pulse.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.example.base_pulse.entities.BaseEntity;
import com.example.base_pulse.repositories.GenericJpaRepository;
import com.example.base_pulse.specifications.DynamicPredicateBuilder;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.ObjectMerger;
import com.example.base_pulse.utils.PageResult;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

public class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {

    protected final GenericJpaRepository<T> repository;

    public BaseServiceImpl(GenericJpaRepository<T> repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public T create(T entity) {
        validate(entity);
        beforeCreate(entity);
        beforeSave(entity);
        T saved = save(entity);
        afterCreate(saved);
        afterSave(saved);
        return saved;
    }

    @Override
    public T findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity with id " + id + " not found"));
    }

    @Override
    public PageResult<T> findAll(Pageable pageable, List<SearchCriteria> filters, List<SortCriteria> sort) {

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

        Specification<T> spec = buildSpecification(filters);

        Page<T> page = (spec == null)
                ? repository.findAll(finalPageable)
                : repository.findAll(spec, finalPageable);

        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    @Override
    @Transactional
    public T replace(Long id, T fullEntity) {
        if (!exists(id)) {
            throw new EntityNotFoundException("Entity with id " + id + " not found");
        }

        validate(fullEntity);
        fullEntity.setId(id);
        beforeSave(fullEntity);
        T saved = save(fullEntity);
        afterSave(saved);
        return saved;
    }

    @Override
    @Transactional
    public T patch(Long id, T partialEntity) {
        T existing = findById(id);
        ObjectMerger.mergeNonNullFields(partialEntity, existing);
        validate(existing);
        beforeSave(existing);
        T saved = save(existing);
        afterSave(saved);
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        T entity = findById(id);
        beforeDelete(entity);
        repository.delete(entity);
        afterDelete(entity);
    }

    public boolean exists(Long id) {
        return repository.existsById(id);
    }

    public long count() {
        return repository.count();
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public List<T> findAllByIds(List<Long> ids) {
        return repository.findAllById(ids);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    protected T save(T entity) {
        return repository.save(entity);
    }

    protected Specification<T> buildSpecification(List<SearchCriteria> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            for (SearchCriteria sc : filters) {
                preds.add(DynamicPredicateBuilder.build(sc, root, cb));
            }
            return cb.and(preds.toArray(Predicate[]::new));
        };
    }

    protected void validate(T entity) {
    }

    protected void beforeCreate(T entity) {
    }

    protected void afterCreate(T entity) {
    }

    protected void beforeSave(T entity) {
    }

    protected void afterSave(T entity) {
    }

    protected void beforeDelete(T entity) {
    }

    protected void afterDelete(T entity) {
    }
}
