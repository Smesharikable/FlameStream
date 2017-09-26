package com.spbsu.flamestream.core.barrier;

import com.spbsu.flamestream.core.HashFunction;

public final class PreBarrierMetaElement<T> {
  @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
  public static final HashFunction<PreBarrierMetaElement<?>> HASH_FUNCTION = new HashFunction<PreBarrierMetaElement<?>>() {
    @Override
    public int hash(PreBarrierMetaElement<?> value) {
      return value.metaHash();
    }
  };

  private final T payload;

  private final int metaHash;

  public PreBarrierMetaElement(T payload, int metaHash) {
    this.payload = payload;
    this.metaHash = metaHash;
  }

  public T payload() {
    return payload;
  }

  public int metaHash() {
    return metaHash;
  }


  @Override
  public String toString() {
    return "PreBarrierMetaElement{" + "payload=" + payload +
            ", metaHash=" + metaHash +
            '}';
  }
}
