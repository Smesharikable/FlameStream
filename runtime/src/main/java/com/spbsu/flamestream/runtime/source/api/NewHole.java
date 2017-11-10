package com.spbsu.flamestream.runtime.source.api;

import akka.actor.ActorRef;
import com.spbsu.flamestream.core.data.meta.GlobalTime;

public final class NewHole {
  private final ActorRef hole;

  private final GlobalTime from;
  private final GlobalTime to;

  public NewHole(ActorRef hole, GlobalTime from, GlobalTime to) {
    this.hole = hole;
    this.from = from;
    this.to = to;
  }

  public GlobalTime from() {
    return from;
  }

  public GlobalTime to() {
    return to;
  }

  public ActorRef hole() {
    return hole;
  }
}
