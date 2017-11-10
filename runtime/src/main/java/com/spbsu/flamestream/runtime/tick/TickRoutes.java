package com.spbsu.flamestream.runtime.tick;

import akka.actor.ActorRef;
import com.spbsu.flamestream.core.HashRange;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;

public final class TickRoutes {
  private final Map<HashRange, ActorRef> rangeConcierges;
  private final Set<ActorRef> localFronts;
  private final ActorRef acker;

  public TickRoutes(Map<HashRange, ActorRef> rangeConcierges, Set<ActorRef> localFronts, ActorRef acker) {
    this.rangeConcierges = new HashMap<>(rangeConcierges);
    this.localFronts = localFronts;
    this.acker = acker;
  }

  public Map<HashRange, ActorRef> rangeConcierges() {
    return unmodifiableMap(rangeConcierges);
  }

  public ActorRef acker() {
    return acker;
  }

  @Override
  public String toString() {
    return "TickRoutes{" + "rangeConcierges=" + rangeConcierges + ", acker=" + acker + '}';
  }

  public Set<ActorRef> localFronts() {
    return this.localFronts;
  }
}
