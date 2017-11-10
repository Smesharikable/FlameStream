package com.spbsu.flamestream.runtime.source;

import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.spbsu.flamestream.core.TickInfo;
import com.spbsu.flamestream.core.data.DataItem;
import com.spbsu.flamestream.core.graph.source.Source;
import com.spbsu.flamestream.core.graph.source.SourceHandle;
import com.spbsu.flamestream.runtime.range.atomic.AtomicActor;
import com.spbsu.flamestream.runtime.source.api.Accepted;
import com.spbsu.flamestream.runtime.source.api.Heartbeat;
import com.spbsu.flamestream.runtime.source.api.PleaseWait;
import com.spbsu.flamestream.runtime.tick.TickRoutes;

/**
 * User: Artem
 * Date: 10.11.2017
 */
public class SourceActor extends AtomicActor {
  private final Source source;
  private final TickInfo tickInfo;

  private ActorRef frontRef;
  private SourceHandle sourceHandle;

  public SourceActor(Source source, TickInfo tickInfo, TickRoutes tickRoutes) {
    super(source, tickInfo, tickRoutes);
    this.source = source;
    this.tickInfo = tickInfo;
    sourceHandle = new SourceHandleImpl(tickInfo, tickRoutes, context());
  }

  @Override
  public Receive createReceive() {
    //noinspection unchecked
    return super.createReceive().orElse(
            ReceiveBuilder.create()
                    .match(DataItem.class, dataItem -> {
                      final long time = dataItem.meta().globalTime().time();
                      if (time >= tickInfo.startTs() && time < tickInfo.stopTs()) {
                        source.onNext(dataItem, sourceHandle);
                        sender().tell(new Accepted(dataItem), self());
                      } else {
                        // TODO: 10.11.2017 specify duration
                        sender().tell(new PleaseWait(1), self());
                      }
                    })
                    .match(Heartbeat.class, heartbeat -> {
                      final long time = heartbeat.time().time();
                      if (time >= tickInfo.startTs() && time < tickInfo.stopTs()) {
                        source.onHeartbeat(heartbeat.time(), sourceHandle);
                      }
                    })
                    .build()
    );
  }

}
