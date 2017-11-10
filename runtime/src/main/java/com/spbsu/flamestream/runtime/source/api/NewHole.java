package com.spbsu.flamestream.runtime.source.api;

import akka.actor.ActorRef;
import com.spbsu.flamestream.core.data.meta.GlobalTime;

public final class NewHole {
  private final ActorRef hole;

  public NewHole(ActorRef hole, GlobalTime from, GlobalTime to) {
    this.hole = hole;
  }

  public ActorRef hole() {
    return hole;
  }
}
