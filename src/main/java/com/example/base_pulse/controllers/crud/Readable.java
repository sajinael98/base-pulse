package com.example.base_pulse.controllers.crud;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.base_pulse.utils.PageResult;

public interface Readable<T> {
  @GetMapping("/{id}")
  ResponseEntity<T> getById(@PathVariable Long id);

  @GetMapping
  ResponseEntity<PageResult<T>> getAll(
      @RequestParam Map<String, String> filters,
      @PageableDefault(size = 20) Pageable pageable);

}
