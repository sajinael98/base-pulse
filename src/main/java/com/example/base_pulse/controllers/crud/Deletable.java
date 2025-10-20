package com.example.base_pulse.controllers.crud;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface Deletable {
  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable Long id);
}
