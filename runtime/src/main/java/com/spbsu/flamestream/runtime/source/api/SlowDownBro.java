package com.spbsu.flamestream.runtime.source.api;

import akka.actor.ActorRef;
import com.spbsu.flamestream.runtime.api.Out;

@Out
public final class SlowDownBro {
  private final ActorRef hole;

  public SlowDownBro(ActorRef hole) {
    this.hole = hole;
  }

  public ActorRef hole() {
    return hole;
  }
}
