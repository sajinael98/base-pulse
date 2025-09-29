package com.example.base_pulse.demo;

import com.example.base_pulse.entities.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table
@Data
public class Demo extends BaseEntity {
  private String name;
  private int age;
}
