package com.example.base_pulse.controllers.crud;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface Updatable<T> {
  @PutMapping("/{id}")
  ResponseEntity<T> replace(@PathVariable Long id, @RequestBody T dto);

  @PatchMapping("/{id}")
  ResponseEntity<T> patch(@PathVariable Long id, @RequestBody T dto);
}
