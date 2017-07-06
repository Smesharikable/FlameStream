package com.spbsu.datastream.core.ack;

import java.util.SortedMap;
import java.util.TreeMap;

public final class AckTableImpl implements AckTable {

  private static final class AckEntry {
    private final boolean isReported;

    private final long xor;

    private AckEntry(boolean isReported, long xor) {
      this.isReported = isReported;
      this.xor = xor;
    }

    public boolean isReported() {
      return this.isReported;
    }

    public long xor() {
      return this.xor;
    }

    public boolean isDone() {
      return this.isReported && this.xor == 0;
    }
  }

  private final SortedMap<Long, AckEntry> table;

  private final long startTs;

  private final long window;

  private long waitingFor;

  public AckTableImpl(long startTs, long window) {
    this.startTs = startTs;
    this.window = window;
    this.table = new TreeMap<>();
    this.table.put(startTs, new AckEntry(false, 0));
    this.waitingFor = startTs;
  }

  @Override
  public void report(long windowHead, long xor) {
    if (windowHead == this.waitingFor) {
      this.table.computeIfPresent(windowHead, (ts, entry) -> new AckEntry(true, entry.xor() ^ xor));
      this.table.putIfAbsent(windowHead, new AckEntry(true, xor));
      this.waitingFor += this.window;
    } else {
      throw new IllegalArgumentException("Not monotonic reports. Expected: " + this.waitingFor + ", got: " + windowHead);
    }
  }

  @Override
  public void ack(long ts, long xor) {
    if (ts < this.lastMinReported) {
      throw new IllegalArgumentException("Ack for passed min");
    }

    final long lowerBound = this.startTs + this.window * ((ts - this.startTs) / this.window);

    this.table.computeIfPresent(lowerBound, (t, entry) -> new AckEntry(entry.isReported(), entry.xor() ^ xor));
    this.table.putIfAbsent(lowerBound, new AckEntry(false, xor));
  }

  private long lastMinReported = 0;

  @Override
  public long min() {
    while (this.table.get(this.table.firstKey()).isDone()) {
      final long firstKey = this.table.firstKey();

      if (this.table.get(firstKey).isDone()) {
        this.table.remove(firstKey);
        this.table.putIfAbsent(firstKey + this.window, new AckEntry(false, 0));
      }
    }

    this.lastMinReported = this.table.firstKey();
    return this.table.firstKey();
  }

  @Override
  public long window() {
    return this.window;
  }

  @Override
  public long start() {
    return this.startTs;
  }
}
