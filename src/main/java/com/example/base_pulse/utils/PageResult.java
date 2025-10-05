package com.example.base_pulse.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
  private List<T> content;
  private long total;
}
