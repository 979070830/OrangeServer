package com.orange.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.TYPE})
public @interface Instantiation
{
  InstantiationMode value() default InstantiationMode.NEW_INSTANCE;
  
  public static enum InstantiationMode
  {
    NEW_INSTANCE,  SINGLE_INSTANCE;
  }
}
