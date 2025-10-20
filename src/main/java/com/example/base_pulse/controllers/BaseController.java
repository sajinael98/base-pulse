package com.example.base_pulse.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.base_pulse.controllers.annotations.DisableCrud;
import com.example.base_pulse.entities.BaseEntity;
import com.example.base_pulse.services.BaseService;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;
import com.example.base_pulse.utils.QueryCriteriaBuilder;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public abstract class BaseController<T extends BaseEntity> {

    protected final BaseService<T> service;

    @PostMapping
    public ResponseEntity<T> create(@RequestBody T dto) {
        if (isDisabled("create")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", "GET, DELETE")
                    .build();
        }
        T created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> replace(@PathVariable Long id, @RequestBody T dto) {
        if (isDisabled("update")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", "GET, DELETE")
                    .build();
        }
        return ResponseEntity.ok(service.replace(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<T> patch(@PathVariable Long id, @RequestBody T dto) {
        if (isDisabled("update")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", "GET, DELETE")
                    .build();
        }
        return ResponseEntity.ok(service.patch(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable Long id) {
        if (isDisabled("read")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", "POST, PATCH, PUT, DELETE")
                    .build();
        }
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResult<T>> getAll(
            @RequestParam Map<String, String> requestParams,
            @PageableDefault(size = 20, page = 0) Pageable pageable) {

        if (isDisabled("read")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", "POST, PATCH, PUT, DELETE")
                    .build();
        }

        List<SearchCriteria> searchCriterias = QueryCriteriaBuilder.parseFiltersFromParams(requestParams);
        List<SortCriteria> sort = QueryCriteriaBuilder.parseSortsFromParams(requestParams);
        return ResponseEntity.ok(service.findAll(pageable, searchCriterias, sort));
    }

    @GetMapping(params = "entity")
    public ResponseEntity<List<Map<String, Object>>> getAllWithFiltersAndSort(
            @RequestParam Map<String, String> requestParams,
            @PageableDefault(size = 20, page = 0) Pageable pageable) {

        if (isDisabled("read")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", "POST, PATCH, PUT, DELETE")
                    .build();
        }

        String entity = requestParams.get("entity");
        if (entity == null) {
            throw new IllegalArgumentException("Entity is required. Cannot execute query without entity.");
        }

        List<SearchCriteria> searchCriterias = QueryCriteriaBuilder.parseFiltersFromParams(requestParams);
        if (searchCriterias.isEmpty()) {
            throw new IllegalArgumentException(
                    "Search filters are required. Cannot execute query without filter conditions.");
        }

        List<String> fields = List.of(Optional.ofNullable(requestParams.get("fields")).orElse("").split(","));
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("fields are required. Cannot execute query without fields.");
        }

        List<SortCriteria> sort = QueryCriteriaBuilder.parseSortsFromParams(requestParams);
        List<Map<String, Object>> values = service.findWithFieldsAndFilters(entity, fields, searchCriterias, sort);
        return ResponseEntity.ok(values);
    }

    private boolean isDisabled(String action) {
        DisableCrud annotation = this.getClass().getAnnotation(DisableCrud.class);
        if (annotation == null)
            return false;

        return switch (action) {
            case "create" -> annotation.create();
            case "read" -> annotation.read();
            case "update" -> annotation.update();
            case "delete" -> annotation.delete();
            default -> false;
        };
    }
}
