package com.spbsu.datastream.core.graph.ops;

import com.spbsu.datastream.core.DataItem;
import com.spbsu.datastream.core.feedback.DICompeted;
import com.spbsu.datastream.core.graph.AbstractAtomicGraph;
import com.spbsu.datastream.core.graph.InPort;
import com.spbsu.datastream.core.graph.OutPort;
import com.spbsu.datastream.core.tick.atomic.AtomicHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class ConsumerBarrierSink<T> extends AbstractAtomicGraph {
  private final Consumer<T> consumer;

  private final InPort inPort;

  private final InPort feedbackPort;

  public ConsumerBarrierSink(final Consumer<T> consumer) {
    super();
    this.consumer = consumer;
    this.inPort = new InPort(PreSinkMetaElement.HASH_FUNCTION);
    this.feedbackPort = new InPort(DICompeted.HASH_FUNCTION);
  }


  @SuppressWarnings({"unchecked", "CastToConcreteClass"})
  @Override
  public void onPush(final InPort inPort, final DataItem<?> item, final AtomicHandle handler) {
    if (inPort.equals(this.inPort)) {
      consumer.accept(((PreSinkMetaElement<T>) item.payload()).payload());
      this.ack(item, handler);
    } else if (inPort.equals(feedbackPort)) {
      this.ack(item, handler);
    }
  }

  public Consumer<T> consumer() {
    return consumer;
  }

  public InPort inPort() {
    return inPort;
  }

  public InPort feedbackPort() {
    return feedbackPort;
  }

  @Override
  public List<InPort> inPorts() {
    final List<InPort> inPorts = new ArrayList<>();
    inPorts.add(inPort);
    inPorts.add(feedbackPort);
    return Collections.unmodifiableList(inPorts);
  }

  @Override
  public List<OutPort> outPorts() {
    return Collections.singletonList(this.ackPort());
  }
}
