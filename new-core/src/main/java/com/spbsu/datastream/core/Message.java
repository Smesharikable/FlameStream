package com.spbsu.datastream.core;

public interface Message<T> {
  T payload();

  long tick();
}
