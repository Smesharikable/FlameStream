package com.spbsu.flamestream.runtime.source;

import akka.actor.ActorContext;
import com.spbsu.flamestream.core.TickInfo;
import com.spbsu.flamestream.core.data.meta.GlobalTime;
import com.spbsu.flamestream.core.graph.source.SourceHandle;
import com.spbsu.flamestream.runtime.range.atomic.AtomicHandleImpl;
import com.spbsu.flamestream.runtime.source.api.Heartbeat;
import com.spbsu.flamestream.runtime.tick.TickRoutes;

/**
 * User: Artem
 * Date: 10.11.2017
 */
public class SourceHandleImpl extends AtomicHandleImpl implements SourceHandle {
  public SourceHandleImpl(TickInfo tickInfo, TickRoutes tickRoutes, ActorContext context) {
    super(tickInfo, tickRoutes, context);
  }

  @Override
  public void heartbeat(GlobalTime time) {
    tickRoutes.acker().tell(new Heartbeat(time), context.self());
  }
}
