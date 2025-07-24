
# base-pulse

A Spring Boot-based library to simplify building REST APIs with dynamic and reusable CRUD operations.  
No need to write repetitive `Controller`, `Service`, or `Repository` classes for each entity — just extend the base classes and customize as needed.

---

## Features

- Generic `Service` and `Controller` layers with default implementations for CRUD  
- Dynamic filtering with flexible `SearchCriteria`  
- Ability to fetch selected fields dynamically  
- Uses Spring Data JPA Specifications for powerful querying  
- Easily extendable and override default behaviors  

---

## Requirements

- Java 17  
- Spring Boot 3.4.7  
- Maven  

---

## Getting Started

### 1. Maven Setup (`pom.xml`)

```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.7</version>
  </parent>
  <groupId>com.example</groupId>
  <artifactId>base-pulse</artifactId>
  <version>0.0.1-SNAPSHOT</version>

  <properties>
    <java.version>17</java.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>
  </dependencies>
</project>
```

---

### 2. Application Setup

```java
@SpringBootApplication
@Import(BaseJpaConfiguration.class)
@EnableJpaRepositories(
    basePackages = "com.example.base_pulse_demo",
    repositoryBaseClass = GenericJpaRepositoryCustomImpl.class)
public class BasePulseDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(BasePulseDemoApplication.class, args);
    }
}
```

---

### 3. Define Your Entity Extending `BaseEntity`

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Person extends BaseEntity {
    @Column
    private String firstName;

    @Column
    private String lastName;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    @JsonManagedReference
    private Address address;
}
```

---

### 4. Create Repository by Extending `GenericJpaRepository`

```java
@Repository
public interface PersonRepo extends GenericJpaRepository<Person> {
}
```

---

### 5. Create Service by Extending `BaseServiceImpl`

```java
@Service
public class PersonService extends BaseServiceImpl<Person> {
    protected PersonService(PersonRepo repository) {
        super(repository);
    }
}
```

---

### 6. Create Controller by Extending `BaseController`

```java
@RestController
@RequestMapping("/persons")
public class PersonController extends BaseController<Person> {
    public PersonController(PersonService service) {
        super(service);
    }
}
```

---

## API Endpoints

- **POST** `/persons` — create new entity  
- **GET** `/persons/{id}` — get entity by id  
- **PUT** `/persons/{id}` — full update  
- **PATCH** `/persons/{id}` — partial update  
- **DELETE** `/persons/{id}` — delete entity  
- **GET** `/persons` with filters — query with pagination, filtering, and dynamic fields  

---

## Filtering and Dynamic Fields

Filters are passed as query parameters and automatically converted into `SearchCriteria` to support flexible querying. You can also specify which fields to include in the response.

### Filtering Example

You can query entities with filters and select specific fields by passing parameters in the URL like this:

```
http://localhost:8080/persons?entity=Person&fields=firstName&filters[0][field]=firstName&filters[0][operator]=eq&filters[0][value]=Saji
```

- `entity=Person` — specifies the entity to query  
- `fields=firstName` — specifies which fields to return (comma-separated for multiple)  
- `filters` — an array of filter criteria, each with:  
  - `field` — the entity field name  
  - `operator` — filter operation (e.g. `eq` for equals)  
  - `value` — the value to filter by  

This request fetches all `Person` entities where the `firstName` equals `Saji` and returns only the `firstName` field in the response.

---

## Core Components

### BaseController

This is an abstract REST controller providing generic CRUD endpoints for any entity extending `BaseEntity`.  
It supports pagination, filtering, partial updates (PATCH), and dynamic selection of fields via query parameters.

Key features:  
- `POST /` to create a new entity  
- `GET /{id}` to retrieve an entity by ID  
- `PUT /{id}` to replace an entity completely  
- `PATCH /{id}` to partially update an entity  
- `DELETE /{id}` to delete an entity  
- `GET /` to list entities with support for pagination and filters  
- `GET /` with `entity` param to fetch specific fields with filters dynamically

```java
package com.example.base_pulse.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
```

---

### BaseServiceImpl

This abstract service class provides default implementations for CRUD operations using a generic JPA repository.  
It supports:

- Creating entities  
- Finding entities by ID with exception handling  
- Paginated fetching with dynamic filters  
- Full replace and partial patch update (merging non-null fields)  
- Deletion with existence check  
- Fetching selected fields dynamically with filters  

```java
package com.example.base_pulse.services;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.base_pulse.entities.BaseEntity;
import com.example.base_pulse.repositories.GenericJpaRepository;
import com.example.base_pulse.specifications.GenericSpecification;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.utils.ObjectMerger;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

public abstract class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {
    protected final GenericJpaRepository<T> repository;

    protected BaseServiceImpl(GenericJpaRepository<T> repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public T create(T entity) {
        return repository.save(entity);
    }

    @Override
    public T findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity with id " + id + " not found"));
    }

    @Override
    public List<T> findAll(Pageable pageable, List<SearchCriteria> filters) {
        Page<T> page;
        if (filters.isEmpty()) {
            Specification<T> spec = new GenericSpecification<>(filters);
            page = repository.findAll(spec, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.getContent();
    }

    @Override
    @Transactional
    public T replace(Long id, T fullEntity) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity not found: " + id);
        }
        fullEntity.setId(id);
        return repository.save(fullEntity);
    }

    @Override
    @Transactional
    public T patch(Long id, T partialEntity) {
        T existingEntity = this.findById(id);
        ObjectMerger.mergeNonNullFields(partialEntity, existingEntity);
        return repository.save(existingEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Entity with id " + id + " not found");
        }
        repository.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> findWithFieldsAndFilters(String entity, List<String> fields,
            List<SearchCriteria> searchCriterias) {
        return repository.fetchValues(entity, fields, searchCriterias);
    }

}
```

---

## Contribution

Contributions and suggestions are welcome! Please open issues or pull requests.

---

## License

Open source (customize as needed).
