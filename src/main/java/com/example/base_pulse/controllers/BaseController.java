package com.example.base_pulse.controllers;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.base_pulse.controllers.annotations.DisableCrud;
import com.example.base_pulse.entities.BaseEntity;
import com.example.base_pulse.services.BaseService;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;
import com.example.base_pulse.utils.QueryCriteriaBuilder;

@RestController
public abstract class BaseController<T extends BaseEntity> {

    protected final BaseService<T> service;

    private final Class<T> entityClass;

    public BaseController(BaseService<T> service) {
        this.service = service;
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class<T>) type.getActualTypeArguments()[0];
    }

    @PostMapping
    public ResponseEntity<T> create(@RequestBody T dto) {
        if (isDisabled("create")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", buildAllowedMethods())
                    .build();
        }
        T created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> replace(@PathVariable Long id, @RequestBody T dto) {
        if (isDisabled("update")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", buildAllowedMethods())
                    .build();
        }
        return ResponseEntity.ok(service.replace(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<T> patch(@PathVariable Long id, @RequestBody T dto) {
        if (isDisabled("update")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", buildAllowedMethods())
                    .build();
        }
        return ResponseEntity.ok(service.patch(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable Long id) {
        if (isDisabled("read")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", buildAllowedMethods())
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
                    .header("Allow", buildAllowedMethods())
                    .build();
        }

        List<SearchCriteria> searchCriterias = QueryCriteriaBuilder.parseFiltersFromParams(requestParams);
        List<SortCriteria> sort = QueryCriteriaBuilder.parseSortsFromParams(requestParams);
        return ResponseEntity.ok(service.findAll(pageable, searchCriterias, sort));
    }

    @GetMapping(params = "fields")
    public ResponseEntity<List<Map<String, Object>>> getDynamic(
            @RequestParam Map<String, String> requestParams,
            @PageableDefault(size = 20, page = 0) Pageable pageable) {

        if (isDisabled("read")) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header("Allow", buildAllowedMethods())
                    .build();
        }

        String entity = getEntityName();

        List<SearchCriteria> filters = QueryCriteriaBuilder.parseFiltersFromParams(requestParams);
        List<String> fields = Arrays.stream(
                Optional.ofNullable(requestParams.get("fields")).orElse("").split(","))
                .filter(s -> !s.isBlank())
                .toList();
        List<SortCriteria> sort = QueryCriteriaBuilder.parseSortsFromParams(requestParams);

        List<Map<String, Object>> values = service.findDynamic(entity, fields, filters, sort, pageable);

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

    private String buildAllowedMethods() {
        DisableCrud annotation = this.getClass().getAnnotation(DisableCrud.class);
        if (annotation == null) {
            return "GET, POST, PUT, PATCH, DELETE";
        }

        StringBuilder allow = new StringBuilder();

        if (!annotation.read())
            allow.append("GET, ");
        if (!annotation.create())
            allow.append("POST, ");
        if (!annotation.update())
            allow.append("PUT, PATCH, ");
        if (!annotation.delete())
            allow.append("DELETE, ");

        if (allow.length() > 2) {
            allow.setLength(allow.length() - 2); // إزالة الفاصلة والمسافة الأخيرة
        }

        return allow.toString();
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    protected String getEntityName() {
        return entityClass.getSimpleName();
    }
}
