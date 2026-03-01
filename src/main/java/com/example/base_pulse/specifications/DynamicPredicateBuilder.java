package com.example.base_pulse.specifications;

import com.example.base_pulse.utils.TypeConverter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class DynamicPredicateBuilder {

  public static Predicate build(SearchCriteria sc, Root<?> root, CriteriaBuilder cb) {
    Path<?> path = resolvePath(root, sc.getField());
    Class<?> fieldType = path.getJavaType();

    Object value = TypeConverter.parseValue(sc.getValue(), fieldType);
    Object valueTo = sc.getValueTo() != null
        ? TypeConverter.parseValue(sc.getValueTo(), fieldType)
        : null;

    return switch (sc.getOperator()) {

      case EQ -> cb.equal(path, value);
      case NE -> cb.notEqual(path, value);

      case LT -> cb.lt(path.as(Number.class), (Number) value);
      case GT -> cb.gt(path.as(Number.class), (Number) value);
      case LTE -> cb.le(path.as(Number.class), (Number) value);
      case GTE -> cb.ge(path.as(Number.class), (Number) value);

      case IN -> path.in((Iterable<?>) value);
      case NIN -> cb.not(path.in((Iterable<?>) value));

      case CONTAINS -> cb.like(
          cb.lower(path.as(String.class)),
          "%" + value.toString().toLowerCase() + "%");

      case NOT_CONTAINS -> cb.notLike(
          cb.lower(path.as(String.class)),
          "%" + value.toString().toLowerCase() + "%");

      case STARTSWITH -> cb.like(
          cb.lower(path.as(String.class)),
          value.toString().toLowerCase() + "%");

      case NOT_STARTSWITH -> cb.notLike(
          cb.lower(path.as(String.class)),
          value.toString().toLowerCase() + "%");

      case ENDSWITH -> cb.like(
          cb.lower(path.as(String.class)),
          "%" + value.toString().toLowerCase());

      case NOT_ENDSWITH -> cb.notLike(
          cb.lower(path.as(String.class)),
          "%" + value.toString().toLowerCase());

      case BETWEEN -> cb.between(
          path.as(Comparable.class),
          (Comparable) value,
          (Comparable) valueTo);
    };

  }

  private static Path<?> resolvePath(Root<?> root, String field) {
    if (!field.contains("."))
      return root.get(field);
    String[] parts = field.split("\\.");
    Path<?> p = root;
    for (String part : parts)
      p = p.get(part);
    return p;
  }
}
