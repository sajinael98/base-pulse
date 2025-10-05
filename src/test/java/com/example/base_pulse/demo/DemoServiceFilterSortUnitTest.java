package com.example.base_pulse.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.example.base_pulse.repositories.GenericJpaRepository;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;

class DemoServiceFilterSortUnitTest {

  @Mock
  private GenericJpaRepository<Demo> repository;

  @InjectMocks
  private DemoService service;

  private Demo demo1;
  private Demo demo2;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    demo1 = new Demo();
    demo1.setId(1L);
    demo1.setName("Saji");
    demo1.setAge(25);

    demo2 = new Demo();
    demo2.setId(2L);
    demo2.setName("Omar");
    demo2.setAge(30);
  }

  @Test
  void findAll_withFilterAndSort_shouldReturnFilteredAndSorted() {
    // Arrange
    SearchCriteria filter = new SearchCriteria("name", SearchCriteria.SearchOperation.EQUAL, "Saji");
    SortCriteria sort = new SortCriteria("id", Sort.Direction.ASC);

    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("id")));
    Page<Demo> page = new PageImpl<>(List.of(demo1), pageable, 1);

    when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

    // Act
    PageResult<Demo> result = service.findAll(PageRequest.of(0, 10), List.of(filter), List.of(sort));

    // Assert
    assertThat(result.getContent()).containsExactly(demo1);
    assertThat(result.getTotal()).isEqualTo(1);
    verify(repository).findAll(any(Specification.class), eq(pageable));
  }
}
