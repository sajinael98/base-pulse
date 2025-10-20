package com.example.base_pulse.controllers.crud;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface Creatable<T> {
    @PostMapping
    ResponseEntity<T> create(@RequestBody T dto);
}
