package com.example.base_pulse.controllers;

import java.util.List;

import com.example.base_pulse.specifications.SearchCriteria;
import com.example.base_pulse.specifications.SortCriteria;

import lombok.Data;

@Data
public class DynamicQueryRequest {
  private List<String> fields;
  private List<SearchCriteria> filters;
  private List<SortCriteria> sort;
  private int page = 0;
  private int size = 50;
}
