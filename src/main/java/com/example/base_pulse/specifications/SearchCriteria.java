package com.example.base_pulse.specifications;

import java.io.Serializable;
import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCriteria implements Serializable {

    private String field;
    private SearchOperation operation;
    private Object value;

    public enum SearchOperation {
        EQUAL("eq");

        private final String operator;

        SearchOperation(String operator) {
            this.operator = operator;
        }

        public String getOperator() {
            return operator;
        }

        public static SearchOperation fromOperator(String operator) {
            return Arrays.stream(values())
                    .filter(op -> op.getOperator().equalsIgnoreCase(operator))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown operator: " + operator));
        }
    }
}
