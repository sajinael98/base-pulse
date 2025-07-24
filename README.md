
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

## Contribution

Contributions and suggestions are welcome! Please open issues or pull requests.

---

## License

Open source (customize as needed).
