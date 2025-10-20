package com.example.base_pulse.controllers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisableCrud {
  boolean create() default false;

  boolean read() default false;

  boolean update() default false;

  boolean delete() default false;
}
