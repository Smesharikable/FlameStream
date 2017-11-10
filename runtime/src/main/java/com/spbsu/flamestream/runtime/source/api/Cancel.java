package com.spbsu.flamestream.runtime.source.api;

import akka.actor.ActorRef;

public final class Cancel {
  private final ActorRef hole;

  public Cancel(ActorRef hole) {
    this.hole = hole;
  }

  public ActorRef hole() {
    return hole;
  }
}
