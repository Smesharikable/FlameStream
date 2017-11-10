package com.spbsu.flamestream.runtime.source;

import com.spbsu.flamestream.core.data.DataItem;
import com.spbsu.flamestream.core.graph.AbstractAtomicGraph;
import com.spbsu.flamestream.core.graph.AtomicHandle;
import com.spbsu.flamestream.core.graph.InPort;
import com.spbsu.flamestream.core.graph.OutPort;
import scala.concurrent.java8.FuturesConvertersImpl;

import java.util.Collections;
import java.util.List;

public final class Source extends AbstractAtomicGraph {
  private final OutPort outPort = new OutPort();

  @Override
  public List<InPort> inPorts() {
    return Collections.emptyList();
  }

  @Override
  public List<OutPort> outPorts() {
    return Collections.singletonList(outPort);
  }

  public long onNext(DataItem<?> item, AtomicHandle handle) {
    handle.push(outPort, item);
  }
}
