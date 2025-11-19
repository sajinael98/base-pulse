package com.example.base_pulse.demo;

import com.example.base_pulse.repositories.GenericJpaRepository;
import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;
import com.example.base_pulse.utils.PageResult;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DemoServiceTest {

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

    // ====== CREATE ======
    @Test
    void create_shouldSaveEntity() {
        when(repository.save(demo1)).thenReturn(demo1);

        Demo saved = service.create(demo1);

        assertThat(saved).isEqualTo(demo1);
        verify(repository).save(demo1);
    }

    // ====== FIND BY ID ======
    @Test
    void findById_whenExists_returnsEntity() {
        when(repository.findById(1L)).thenReturn(Optional.of(demo1));

        Demo found = service.findById(1L);

        assertThat(found.getName()).isEqualTo("Saji");
        verify(repository).findById(1L);
    }

    @Test
    void findById_whenNotExists_throwsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ====== REPLACE ======
    @Test
    void replace_whenExists_shouldSetIdAndSave() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(any(Demo.class))).thenReturn(demo1);

        Demo replaced = service.replace(1L, new Demo());

        assertThat(replaced.getId()).isEqualTo(1L);
        verify(repository).save(any(Demo.class));
    }

    @Test
    void replace_whenNotExists_shouldThrow() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.replace(99L, new Demo()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ====== PATCH ======
    @Test
    void patch_shouldMergeAndSave() {
        Demo partial = new Demo();
        partial.setName("Updated");

        when(repository.findById(1L)).thenReturn(Optional.of(demo1));
        when(repository.save(any(Demo.class))).thenReturn(demo1);

        Demo patched = service.patch(1L, partial);

        assertThat(patched.getName()).isEqualTo("Updated");
        verify(repository).save(demo1);
    }

    // ====== DELETE ======
    @Test
    void delete_whenExists_deletes() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_whenNotExists_throws() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ====== FIND ALL ======
    @Test
    void findAll_withoutFilters_returnsPagedContent() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Demo> page = new PageImpl<>(List.of(demo1), pageable, 1);
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        PageResult<Demo> result = service.findAll(pageable, null, null);

        assertThat(result.getContent()).containsExactly(demo1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void findAll_withFilters_shouldUseSpecification() {
        SearchCriteria filter = new SearchCriteria("name", SearchCriteria.SearchOperation.EQUAL, "Saji");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Demo> page = new PageImpl<>(List.of(demo1), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResult<Demo> result = service.findAll(pageable, List.of(filter), null);

        assertThat(result.getContent()).containsExactly(demo1);
        assertThat(result.getTotal()).isEqualTo(1);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAll_withSort_shouldApplySorting() {
        SortCriteria sort = new SortCriteria("id", Sort.Direction.DESC);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("id")));
        Page<Demo> page = new PageImpl<>(List.of(demo1, demo2), pageable, 2);

        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        PageResult<Demo> result = service.findAll(PageRequest.of(0, 10), null, List.of(sort));

        assertThat(result.getContent()).containsExactly(demo1, demo2);
        assertThat(result.getTotal()).isEqualTo(2);
        verify(repository).findAll(eq(pageable));
    }

    @Test
    void findAll_withFiltersAndSort_shouldReturnFilteredAndSorted() {
        SearchCriteria filter = new SearchCriteria("name", SearchCriteria.SearchOperation.EQUAL, "Saji");
        SortCriteria sort = new SortCriteria("id", Sort.Direction.ASC);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<Demo> page = new PageImpl<>(List.of(demo1), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResult<Demo> result = service.findAll(PageRequest.of(0, 10), List.of(filter), List.of(sort));

        assertThat(result.getContent()).containsExactly(demo1);
        assertThat(result.getTotal()).isEqualTo(1);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    // ====== FETCH VALUES ======
    // @Test
    // void findWithFieldsAndFilters_shouldCallRepository() {
    //     List<String> fields = List.of("name");
    //     List<SearchCriteria> filters = List.of();
    //     List<SortCriteria> sort = List.of();

    //     when(repository.findDynamic("Demo", fields, filters, sort))
    //             .thenReturn(List.of(Map.of("name", "Saji")));

    //     List<Map<String, Object>> result = service.findWithFieldsAndFilters("Demo", fields, filters, sort);

    //     assertThat(result).hasSize(1);
    //     assertThat(result.get(0).get("name")).isEqualTo("Saji");
    // }
}
