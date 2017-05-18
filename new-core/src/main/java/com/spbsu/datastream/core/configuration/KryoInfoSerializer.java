package com.spbsu.datastream.core.configuration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.spbsu.datastream.core.tick.TickInfo;
import org.objenesis.strategy.StdInstantiatorStrategy;

public final class KryoInfoSerializer implements TickInfoSerializer {
  private final Kryo kryo;

  public KryoInfoSerializer() {
    this.kryo = new Kryo();
    ((Kryo.DefaultInstantiatorStrategy) this.kryo.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
  }

  @Override
  public byte[] serialize(final TickInfo tickInfo) {
    final ByteBufferOutput o = new ByteBufferOutput(1000, 20000);
    this.kryo.writeObject(o, tickInfo);
    return o.toBytes();
  }

  @Override
  public TickInfo deserialize(final byte[] date) {
    final ByteBufferInput input = new ByteBufferInput(date);
    return this.kryo.readObject(input, TickInfo.class);
  }
}