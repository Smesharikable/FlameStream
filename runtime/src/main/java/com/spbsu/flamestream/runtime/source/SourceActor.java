package com.spbsu.flamestream.runtime.source;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.spbsu.flamestream.core.TickInfo;
import com.spbsu.flamestream.core.data.DataItem;
import com.spbsu.flamestream.core.graph.source.Source;
import com.spbsu.flamestream.core.graph.source.SourceHandle;
import com.spbsu.flamestream.runtime.range.atomic.AtomicActor;
import com.spbsu.flamestream.runtime.source.api.Heartbeat;
import com.spbsu.flamestream.runtime.tick.TickRoutes;

/**
 * User: Artem
 * Date: 10.11.2017
 */
public class SourceActor extends AtomicActor {
  private final Source source;
  private final TickInfo tickInfo;
  private final TickRoutes tickRoutes;

  private ActorRef frontRef;
  private SourceHandle sourceHandle;

  public SourceActor(Source source, TickInfo tickInfo, TickRoutes tickRoutes) {
    super(source, tickInfo, tickRoutes);
    this.source = source;
    this.tickInfo = tickInfo;
    this.tickRoutes = tickRoutes;
  }

  @Override
  public Receive createReceive() {
    //noinspection unchecked
    return super.createReceive().orElse(
            ReceiveBuilder.create()
                    .match(DataItem.class, dataItem -> {
                      if (sourceHandle == null) {
                        sourceHandle = new SourceHandleImpl(tickInfo, tickRoutes, context());
                      }
                      source.onNext(dataItem, sourceHandle);
                    })
                    .match(Heartbeat.class, heartbeat -> {
                      if (sourceHandle == null) {
                        sourceHandle = new SourceHandleImpl(tickInfo, tickRoutes, context());
                      }
                      source.onHeartbeat(heartbeat.time(), sourceHandle);
                    })
                    .build()
    );
  }

}
