package com.example.base_pulse.specifications;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.example.base_pulse.utils.TypeConverter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class GenericSpecification<T> implements Specification<T> {

    private final List<SearchCriteria> criteriaList;

    public GenericSpecification(List<SearchCriteria> criteriaList) {
        this.criteriaList = criteriaList;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Predicate predicate = builder.conjunction(); // AND

        for (SearchCriteria criteria : criteriaList) {

            Class<?> fieldType = root.get(criteria.getField()).getJavaType();

            Object value = TypeConverter.parseValue(criteria.getValue(), fieldType);

            switch (criteria.getOperation()) {
                case EQUAL:
                    predicate = builder.and(predicate,
                            builder.equal(root.get(criteria.getField()), value));
                    break;
                default:
                    throw new RuntimeException("operator: " + criteria.getOperation() + " not supported");
            }
        }

        return predicate;
    }
}
