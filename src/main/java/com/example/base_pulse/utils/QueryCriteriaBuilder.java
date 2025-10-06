package com.example.base_pulse.utils;

import org.springframework.data.domain.Sort;

import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryCriteriaBuilder {

    private final List<SearchCriteria> filters = new ArrayList<>();
    private final List<SortCriteria> sorts = new ArrayList<>();

    private QueryCriteriaBuilder() {
    }

    public static QueryCriteriaBuilder builder() {
        return new QueryCriteriaBuilder();
    }

    public QueryCriteriaBuilder add(String field, String operator, String value) {
        filters.add(new SearchCriteria(field, SearchCriteria.SearchOperation.fromOperator(operator), value));
        return this;
    }

    public QueryCriteriaBuilder sortBy(String field, Sort.Direction direction) {
        sorts.add(new SortCriteria(field, direction));
        return this;
    }

    public List<SearchCriteria> buildFilters() {
        return filters;
    }

    public Sort buildSort() {
        if (sorts.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (SortCriteria sc : sorts) {
            orders.add(new Sort.Order(sc.getDirection(), sc.getField()));
        }

        return Sort.by(orders);
    }

    public static List<SearchCriteria> parseFiltersFromParams(Map<String, String> params) {
        Pattern pattern = Pattern.compile("filters\\[(\\d+)]\\[(\\w+)](?:\\[(\\d+)])?");
        Map<Integer, SearchCriteria> filterMap = new HashMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            Matcher matcher = pattern.matcher(entry.getKey());
            if (matcher.matches()) {
                int index = Integer.parseInt(matcher.group(1));
                String property = matcher.group(2);
                String valueIndex = matcher.group(3); // optional (for arrays)

                filterMap.putIfAbsent(index, new SearchCriteria());
                SearchCriteria filter = filterMap.get(index);

                String value = entry.getValue();

                switch (property) {
                    case "field":
                        filter.setField(value);
                        break;
                    case "operator":
                        filter.setOperation(SearchCriteria.SearchOperation.fromOperator(value));
                        break;
                    case "value":
                        if (valueIndex != null) {
                            if (filter.getValue() == null) {
                                filter.setValue(new ArrayList<>());
                            }
                            ((Collection) filter.getValue()).add(value);
                        } else {
                            filter.setValue(value);
                        }
                        break;
                }
            }
        }

        return filterMap.values().stream().toList();
    }

    public static List<SortCriteria> parseSortsFromParams(Map<String, String> params) {
        Pattern pattern = Pattern.compile("sort\\[(\\d+)]\\[(\\w+)]");
        Map<Integer, SortCriteria> sortMap = new HashMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            Matcher matcher = pattern.matcher(entry.getKey());

            if (matcher.matches()) {
                int index = Integer.parseInt(matcher.group(1));
                String property = matcher.group(2);
                String value = entry.getValue();

                sortMap.putIfAbsent(index, new SortCriteria(null, Sort.Direction.ASC));
                SortCriteria current = sortMap.get(index);

                switch (property) {
                    case "field":
                        current = new SortCriteria(value, current.getDirection());
                        break;
                    case "order":
                        current = new SortCriteria(current.getField(), Sort.Direction.fromString(value));
                        break;
                }

                sortMap.put(index, current);
            }
        }

        return new ArrayList<>(sortMap.values());
    }

    public static Sort buildSort(List<SortCriteria> sortCriteriaList) {
        if (sortCriteriaList == null || sortCriteriaList.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (SortCriteria sc : sortCriteriaList) {
            orders.add(new Sort.Order(sc.getDirection(), sc.getField()));
        }

        return Sort.by(orders);
    }
}
