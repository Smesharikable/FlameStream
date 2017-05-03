package com.spbsu.datastream.core.ack;

public interface AckTable {
  void report(final long windowHead);

  long min();

  long window();

  long start();

  void ack(final long windowHead, final long xor);
}
