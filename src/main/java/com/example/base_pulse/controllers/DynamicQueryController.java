package com.example.base_pulse.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.base_pulse.services.DynamicQueryService;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;
import com.example.base_pulse.utils.QueryCriteriaBuilder;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dynamic")
@RequiredArgsConstructor
public class DynamicQueryController {

  private final DynamicQueryService dynamicService;

  @GetMapping("/{entity}")
  public ResponseEntity<PageResult<Map<String, Object>>> findList(
      @PathVariable String entity,
      @RequestParam Map<String, String> params,
      @PageableDefault(size = 20, page = 0) Pageable pageable) {
    List<SearchCriteria> filters = QueryCriteriaBuilder.parseFiltersFromParams(params);
    List<SortCriteria> sorts = QueryCriteriaBuilder.parseSortsFromParams(params);
    List<String> fields = parseFields(params);

    return ResponseEntity.ok(
        dynamicService.findList(entity, fields, filters, sorts, pageable));
  }

  @GetMapping("/{entity}/one")
  public ResponseEntity<Map<String, Object>> findOne(
      @PathVariable String entity,
      @RequestParam Map<String, String> params) {
    List<SearchCriteria> filters = QueryCriteriaBuilder.parseFiltersFromParams(params);
    List<String> fields = parseFields(params);

    return ResponseEntity.ok(
        dynamicService.findOne(entity, fields, filters));
  }

  private List<String> parseFields(Map<String, String> params) {
    if (params.containsKey("fields")) {
      String csv = params.get("fields");
      if (csv != null && !csv.isBlank()) {
        return List.of(csv.split(","));
      }
    }

    return List.of("id");
  }
}
