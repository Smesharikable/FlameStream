package com.spbsu.datastream.core;

/**
 * Experts League
 * Created by solar on 27.10.16.
 */
public class TypeUnreachableException extends Exception {
  public TypeUnreachableException() {
  }

  public TypeUnreachableException(final String message) {
    super(message);
  }

  public TypeUnreachableException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public TypeUnreachableException(final Throwable cause) {
    super(cause);
  }
}