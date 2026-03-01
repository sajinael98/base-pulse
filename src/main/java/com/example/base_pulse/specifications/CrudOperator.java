package com.example.base_pulse.specifications;

import java.util.Arrays;

public enum CrudOperator {
  EQ,
  NE,

  LT,
  GT,
  LTE,
  GTE,

  IN,
  NIN,

  CONTAINS,
  NOT_CONTAINS,

  STARTSWITH,
  NOT_STARTSWITH,

  ENDSWITH,
  NOT_ENDSWITH,

  BETWEEN;

  public static CrudOperator fromOperator(String operator) {
    String normalized = operator
        .toUpperCase()
        .replace("-", "_");

    return Arrays.stream(values())
        .filter(op -> op.name().equals(normalized))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown operator: " + operator));
  }

}
