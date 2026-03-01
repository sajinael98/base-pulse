package com.example.base_pulse.repositories;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;

public interface DynamicJpaRepository {
  PageResult<Map<String, Object>> findDynamic(
      String entity,
      List<String> fields,
      List<SearchCriteria> filters,
      List<SortCriteria> sort,
      Pageable pageable);
}
