package com.example.base_pulse.repositories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.TypeConverter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.EntityType;

public class GenericJpaRepositoryCustomImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements GenericJpaRepositoryCustom<T> {

    private final EntityManager em;

    public GenericJpaRepositoryCustomImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.em = em;
    }

    public List<Map<String, Object>> findDynamic(
            String entityName,
            List<String> fields,
            List<SearchCriteria> filters,
            List<SortCriteria> sort,
            Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        EntityType<?> entityType = em.getMetamodel().getEntities().stream()
                .filter(et -> et.getName().equals(entityName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityName));

        Root<?> root = query.from(entityType);

        List<Selection<?>> selections;
        if (fields == null || fields.isEmpty() || (fields.size() == 1 && fields.get(0).isBlank())) {
            selections = List.of(root);
        } else {
            selections = fields.stream()
                    .map(f -> getPath(root, f).alias(f))
                    .collect(Collectors.toList());
        }

        query.multiselect(selections);

        List<Predicate> predicates = new ArrayList<>();
        if (filters != null && !filters.isEmpty()) {
            for (SearchCriteria sc : filters) {
                Path<Object> path = getPath(root, sc.getField());
                Object rawValue = sc.getValue();

                switch (sc.getOperation()) {
                    case EQUAL -> {
                        Object parsed = TypeConverter.parseValue(rawValue, path.getJavaType());
                        predicates.add(cb.equal(path, parsed));
                    }
                }
            }
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        if (sort != null && !sort.isEmpty()) {
            List<Order> orders = sort.stream()
                    .map(sc -> {
                        Path<Object> path = getPath(root, sc.getField());
                        return sc.getDirection() == Sort.Direction.ASC ? cb.asc(path) : cb.desc(path);
                    })
                    .toList();
            query.orderBy(orders);
        }

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        if (pageable != null && pageable.isPaged()) {
            int pageNumber = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();
            typedQuery.setFirstResult(pageNumber * pageSize);
            typedQuery.setMaxResults(pageSize);
        }

        List<Tuple> results = typedQuery.getResultList();

        return results.stream()
                .map(t -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    if (fields == null || fields.isEmpty() || (fields.size() == 1 && fields.get(0).isBlank())) {
                        entityType.getAttributes().forEach(attr -> {
                            row.put(attr.getName(), t.get(attr.getName()));
                        });
                    } else {
                        for (String f : fields) {
                            row.put(f, t.get(f));
                        }
                    }
                    return row;
                })
                .collect(Collectors.toList());
    }

    private Path<Object> getPath(From<?, ?> root, String fieldName) {
        String[] parts = fieldName.split("\\.");
        Path<Object> path = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }
}
