package com.example.base_pulse.demo;

import com.example.base_pulse.repositories.GenericJpaRepository;
import com.example.base_pulse.services.BaseServiceImpl;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    SearchCriteria filter = new SearchCriteria("name", SearchCriteria.SearchOperation.EQUAL, "Saji");
    SortCriteria sort = new SortCriteria("id", Sort.Direction.ASC);

    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("id")));
    Page<Demo> page = new PageImpl<>(List.of(demo1));

    when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

    List<Demo> result = service.findAll(
        PageRequest.of(0, 10),
        List.of(filter),
        List.of(sort));

    assertThat(result).containsExactly(demo1);
    verify(repository).findAll(any(Specification.class), eq(pageable));
  }

}
