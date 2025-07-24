package com.example.base_pulse.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import com.example.base_pulse.entities.BaseEntity;

@NoRepositoryBean
public interface GenericJpaRepository<T extends BaseEntity> extends
                JpaRepository<T, Long>,
                JpaSpecificationExecutor<T>,
                GenericJpaRepositoryCustom<T> {
}
