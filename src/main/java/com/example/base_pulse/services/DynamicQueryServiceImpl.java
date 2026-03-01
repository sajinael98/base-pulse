package com.example.base_pulse.services;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.base_pulse.repositories.DynamicJpaRepository;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DynamicQueryServiceImpl implements DynamicQueryService {

  private final DynamicJpaRepository dynamicRepo;

  @Override
  public PageResult<Map<String, Object>> findList(
      String entity,
      List<String> fields,
      List<SearchCriteria> filters,
      List<SortCriteria> sort,
      Pageable pageable) {
    return dynamicRepo.findDynamic(entity, fields, filters, sort, pageable);
  }

  @Override
  public Map<String, Object> findOne(
      String entity,
      List<String> fields,
      List<SearchCriteria> filters) {

    Pageable pageable = Pageable.ofSize(1);

    PageResult<Map<String, Object>> result = dynamicRepo.findDynamic(entity, fields, filters, null, pageable);

    if (result == null || result.getContent().isEmpty()) {
      return null;
    }

    return result.getContent().get(0);
  }
}
