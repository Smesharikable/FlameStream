package com.spbsu.flamestream.core.data.meta;

import java.util.function.LongPredicate;

public final class LongRange implements LongPredicate {
  private final long from;
  private final long to;

  public LongRange(long from, long to) {
    this.from = from;
    this.to = to;
  }

  public long from() {
    return from;
  }

  public long to() {
    return to;
  }

  public boolean isIn(long ts) {
    return from <= ts && ts < to;
  }

  @Override
  public boolean test(long value) {
    return isIn(value);
  }
}
