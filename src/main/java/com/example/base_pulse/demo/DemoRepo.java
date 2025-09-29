package com.example.base_pulse.demo;

import org.springframework.stereotype.Repository;

import com.example.base_pulse.repositories.GenericJpaRepository;

@Repository
public interface DemoRepo extends GenericJpaRepository<Demo>{
  
}
