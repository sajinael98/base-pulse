package com.example.base_pulse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.example.base_pulse.repositories.GenericJpaRepositoryCustomImpl;

@Configuration
@EnableJpaRepositories(repositoryBaseClass = GenericJpaRepositoryCustomImpl.class)
public class BaseJpaConfiguration {

}
