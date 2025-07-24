package com.example.base_pulse.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.base_pulse.entities.BaseEntity;
import com.example.base_pulse.services.BaseService;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.utils.QueryCriteriaBuilder;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public abstract class BaseController<T extends BaseEntity> {
    private final BaseService<T> service;

    @PostMapping
    public ResponseEntity<T> create(@RequestBody T dto) {
        T created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> replace(@PathVariable Long id, @RequestBody T dto) {
        return ResponseEntity.ok(service.replace(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<T> patch(@PathVariable Long id, @RequestBody T dto) {
        return ResponseEntity.ok(service.patch(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<T>> getAll(@RequestParam Map<String, String> requestParams,
            @PageableDefault(size = 20, page = 0) Pageable pageable) {
        List<SearchCriteria> searchCriterias = QueryCriteriaBuilder.parseFiltersFromParams(requestParams);
        if (searchCriterias.isEmpty()) {
            throw new IllegalArgumentException(
                    "Search filters are required. Cannot execute query without filter conditions.");
        }
        return ResponseEntity.ok(service.findAll(pageable, searchCriterias));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(params = "entity")
    public ResponseEntity<List<Map<String, Object>>> getAllWithFiltersAndSort(
            @RequestParam Map<String, String> requestParams, @PageableDefault(size = 20, page = 0) Pageable pageable) {
        String entity = requestParams.get("entity");
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Entity is required. Cannot execute query without entity.");
        }

        List<SearchCriteria> searchCriterias = QueryCriteriaBuilder.parseFiltersFromParams(requestParams);
        if (searchCriterias.isEmpty()) {
            throw new IllegalArgumentException(
                    "Search filters are required. Cannot execute query without filter conditions.");
        }

        List<String> fields = List.of(Optional.of(requestParams.get("fields")).orElse("").split(","));
        if (fields.isEmpty()) {
            throw new IllegalArgumentException(
                    "fields are required. Cannot execute query without fields.");
        }

        List<Map<String, Object>> values = service.findWithFieldsAndFilters(entity, fields, searchCriterias);
        return ResponseEntity.ok().body(values);
    }
}
