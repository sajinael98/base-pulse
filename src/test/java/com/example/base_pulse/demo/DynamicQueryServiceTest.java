package com.example.base_pulse.demo;

import com.example.base_pulse.repositories.DynamicJpaRepository;
import com.example.base_pulse.services.DynamicQueryServiceImpl;
import com.example.base_pulse.specifications.CrudOperator;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DynamicQueryServiceTest {

        @Mock
        private DynamicJpaRepository dynamicRepo;

        @InjectMocks
        private DynamicQueryServiceImpl service;

        private Pageable pageable;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);
                pageable = PageRequest.of(0, 10);
        }

        @Test
        void findList_withoutFields_returnsRepoResult() {

                PageResult<Map<String, Object>> mockResult = new PageResult<>(
                                List.of(Map.of("id", 1L, "name", "A")),
                                1);

                when(dynamicRepo.findDynamic(
                                eq("Demo"),
                                isNull(),
                                anyList(),
                                anyList(),
                                eq(pageable)))
                                .thenReturn(mockResult);

                PageResult<Map<String, Object>> result = service.findList("Demo", null, List.of(), List.of(), pageable);

                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).get("name")).isEqualTo("A");
                assertThat(result.getTotal()).isEqualTo(1);

                verify(dynamicRepo).findDynamic("Demo", null, List.of(), List.of(), pageable);
        }

        @Test
        void findList_withFields_passesFieldsToRepo() {

                List<String> fields = List.of("id", "name");

                PageResult<Map<String, Object>> mockResult = new PageResult<>(
                                List.of(Map.of("id", 1L, "name", "A")),
                                1);

                when(dynamicRepo.findDynamic(
                                eq("Demo"),
                                eq(fields),
                                anyList(),
                                anyList(),
                                eq(pageable)))
                                .thenReturn(mockResult);

                PageResult<Map<String, Object>> result = service.findList("Demo", fields, List.of(), List.of(),
                                pageable);

                assertThat(result.getContent().get(0).get("name")).isEqualTo("A");
        }

        @Test
        void findList_withFilters_passesFiltersToRepo() {

                List<SearchCriteria> filters = List.of(new SearchCriteria("name", CrudOperator.EQ, "Saji"));

                PageResult<Map<String, Object>> mockResult = new PageResult<>(
                                List.of(Map.of("name", "Saji")),
                                1);

                when(dynamicRepo.findDynamic(
                                eq("Demo"),
                                any(),
                                eq(filters),
                                anyList(),
                                eq(pageable)))
                                .thenReturn(mockResult);

                PageResult<Map<String, Object>> result = service.findList("Demo", null, filters, List.of(), pageable);

                assertThat(result.getContent().get(0).get("name")).isEqualTo("Saji");

                verify(dynamicRepo).findDynamic("Demo", null, filters, List.of(), pageable);
        }

        @Test
        void findList_withSort_passesSortToRepo() {

                List<SortCriteria> sort = List.of(new SortCriteria("id", Sort.Direction.DESC));

                PageResult<Map<String, Object>> mockResult = new PageResult<>(
                                List.of(Map.of("id", 1L)),
                                1);

                when(dynamicRepo.findDynamic(
                                eq("Demo"),
                                any(),
                                any(),
                                eq(sort),
                                eq(pageable)))
                                .thenReturn(mockResult);

                PageResult<Map<String, Object>> result = service.findList("Demo", null, List.of(), sort, pageable);

                assertThat(result.getContent().get(0).get("id")).isEqualTo(1L);

                verify(dynamicRepo).findDynamic("Demo", null, List.of(), sort, pageable);
        }

        @Test
        void findOne_passesCorrectArgumentsToRepo() {

                List<SearchCriteria> filters = List.of(new SearchCriteria("id", CrudOperator.EQ, 1L));

                Pageable onePage = PageRequest.of(0, 1);

                PageResult<Map<String, Object>> mockResult = new PageResult<>(
                                List.of(Map.of("id", 1L, "name", "Saji")),
                                1);

                when(dynamicRepo.findDynamic(
                                eq("Demo"),
                                isNull(),
                                eq(filters),
                                isNull(),
                                eq(onePage))).thenReturn(mockResult);

                Map<String, Object> result = service.findOne("Demo", null, filters);

                assertThat(result).isNotNull();
                assertThat(result.get("name")).isEqualTo("Saji");

                verify(dynamicRepo).findDynamic(
                                eq("Demo"),
                                isNull(),
                                eq(filters),
                                isNull(),
                                eq(onePage));
        }

        @Test
        void findOne_returnsNullWhenEmpty() {

                List<SearchCriteria> filters = List.of(new SearchCriteria("id", CrudOperator.EQ, 100));

                PageResult<Map<String, Object>> emptyResult = new PageResult<>(List.of(), 0);

                when(dynamicRepo.findDynamic(
                                eq("Demo"),
                                any(),
                                eq(filters),
                                isNull(),
                                any(Pageable.class)))
                                .thenReturn(emptyResult);

                Map<String, Object> result = service.findOne("Demo", null, filters);

                assertThat(result).isNull();
        }
}