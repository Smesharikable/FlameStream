package com.spbsu.datastream.core;

import java.util.Comparator;
import java.util.Objects;

public final class LocalEvent implements Comparable<LocalEvent> {
  //Inner representation is a subject for a discussion and/or an optimization

  private final int localTime;

  private final int childId;

  public LocalEvent(final int localTime) {
    this.localTime = localTime;
    this.childId = 0;
  }

  public LocalEvent(final int localTime, final int childId) {
    this.localTime = localTime;
    this.childId = childId;
  }

  public int localTime() {
    return localTime;
  }

  public int childId() {
    return childId;
  }

  @Override
  public int compareTo(final LocalEvent that) {
    return Comparator.comparingInt(LocalEvent::localTime)
            .thenComparingInt(LocalEvent::childId)
            .compare(this, that);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    final LocalEvent that = (LocalEvent) o;
    return localTime == that.localTime &&
            childId == that.childId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(localTime, childId);
  }

  @Override
  public String toString() {
    return "LocalEvent{" + "localTime=" + localTime +
            ", childId=" + childId +
            '}';
  }
}
