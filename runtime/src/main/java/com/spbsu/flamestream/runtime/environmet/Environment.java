package com.spbsu.flamestream.runtime.environmet;

import com.spbsu.flamestream.core.TickInfo;
import com.spbsu.flamestream.core.graph.AtomicGraph;
import com.spbsu.flamestream.core.graph.TheGraph;

import java.util.Set;
import java.util.function.Consumer;

public interface Environment extends AutoCloseable {
  void deploy(TickInfo tickInfo);

  Set<Integer> availableFronts();

  // TODO: 9/26/17 Sink interface?
  AtomicGraph wrapInSink(Consumer<Object> mySuperConsumer);

  Consumer<Object> frontConsumer(int frontId);
}
