package com.example.base_pulse.repositories;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.base_pulse.specifications.DynamicPredicateBuilder;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.EntityType;

@Repository
public class DynamicJpaRepositoryImpl implements DynamicJpaRepository {

  @PersistenceContext
  private EntityManager em;

  @Override
  public PageResult<Map<String, Object>> findDynamic(
      String entity,
      List<String> fields,
      List<SearchCriteria> filters,
      List<SortCriteria> sort,
      Pageable pageable) {

    CriteriaBuilder cb = em.getCriteriaBuilder();

    EntityType<?> entityType = resolveEntity(entity);
    Class<?> entityClass = entityType.getJavaType();


    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<?> root = cq.from(entityClass);

    List<String> projection = resolveFields(fields, entityType);

    List<Selection<?>> selections = new ArrayList<>();
    for (String f : projection)
      selections.add(root.get(f).alias(f));

    cq.multiselect(selections);

    List<Predicate> predicates = new ArrayList<>();

    if (filters != null && !filters.isEmpty()) {
      for (SearchCriteria sc : filters)
        predicates.add(DynamicPredicateBuilder.build(sc, root, cb));

      cq.where(predicates.toArray(Predicate[]::new));
    }

    if (sort != null && !sort.isEmpty()) {
      List<Order> orders = new ArrayList<>();

      for (SortCriteria s : sort) {
        Path<Object> p = resolveSortPath(root, s.getField());

        orders.add(
            s.getDirection().isAscending()
                ? cb.asc(p)
                : cb.desc(p));
      }

      cq.orderBy(orders);
    }

    TypedQuery<Tuple> query = em.createQuery(cq);

    if (pageable != null) {
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
    }

    List<Tuple> tuples = query.getResultList();

    List<Map<String, Object>> rows = new ArrayList<>();

    for (Tuple t : tuples) {
      Map<String, Object> row = new LinkedHashMap<>();

      for (String f : projection)
        row.put(f, t.get(f));

      rows.add(row);
    }


    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<?> countRoot = countQuery.from(entityClass);

    countQuery.select(cb.count(countRoot));

    if (!predicates.isEmpty()) {
      List<Predicate> countPreds = new ArrayList<>();

      for (SearchCriteria sc : filters)
        countPreds.add(DynamicPredicateBuilder.build(sc, countRoot, cb));

      countQuery.where(countPreds.toArray(Predicate[]::new));
    }

    Long total = em.createQuery(countQuery).getSingleResult();


    return new PageResult<>(rows, total);
  }

  private EntityType<?> resolveEntity(String name) {
    return em.getMetamodel().getEntities().stream()
        .filter(e -> e.getName().equals(name) || e.getJavaType().getSimpleName().equals(name))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + name));
  }

  private List<String> resolveFields(List<String> fields, EntityType<?> type) {
    if (fields != null && !fields.isEmpty()) {
      return fields;
    }

    return List.of("id");
  }

  private Path<Object> resolveSortPath(Root<?> root, String field) {
    if (!field.contains("."))
      return root.get(field);
    String[] parts = field.split("\\.");
    Path<Object> p = root.get(parts[0]);
    for (int i = 1; i < parts.length; i++)
      p = p.get(parts[i]);
    return p;
  }
}
