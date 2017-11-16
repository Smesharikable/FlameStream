package com.spbsu.flamestream.common;

import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public abstract class FlameStreamSuite {
  @BeforeMethod
  public void beforeMethod(Method method) {
    System.out.println("Method name:" + method.getName());
  }
}
