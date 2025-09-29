package com.example.base_pulse.demo;

import org.springframework.stereotype.Service;

import com.example.base_pulse.repositories.GenericJpaRepository;
import com.example.base_pulse.services.BaseServiceImpl;

@Service
public class DemoService extends BaseServiceImpl<Demo>{

  protected DemoService(GenericJpaRepository<Demo> repository) {
    super(repository);
  }
  
}
