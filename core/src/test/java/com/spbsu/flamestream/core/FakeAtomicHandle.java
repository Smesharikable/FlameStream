package com.spbsu.flamestream.core;

import com.spbsu.flamestream.core.data.DataItem;
import com.spbsu.flamestream.core.graph.AtomicHandle;
import com.spbsu.flamestream.core.graph.OutPort;
import com.spbsu.flamestream.core.stat.Statistics;

import java.util.function.BiConsumer;

public final class FakeAtomicHandle implements AtomicHandle {
  private final BiConsumer<OutPort, DataItem<?>> pushConsumer;

  public FakeAtomicHandle(BiConsumer<OutPort, DataItem<?>> pushConsumer) {
    this.pushConsumer = pushConsumer;
  }

  @Override
  public void push(OutPort out, DataItem<?> result) {
    pushConsumer.accept(out, result);
  }

  @Override
  public void ack(DataItem<?> item) {
  }

  @Override
  public void flushAcks() {
  }

  @Override
  public void submitStatistics(Statistics stat) {
  }

  @Override
  public void error(String format, Object... args) {
  }
}
