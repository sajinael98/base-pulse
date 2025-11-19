package com.example.base_pulse.specifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private String field;
    private CrudOperator operator;
    private Object value;
    private Object valueTo;

    public SearchCriteria(String field, CrudOperator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
}
