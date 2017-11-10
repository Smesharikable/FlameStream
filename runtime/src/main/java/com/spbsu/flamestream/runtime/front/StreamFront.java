package com.spbsu.flamestream.runtime.front;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.spbsu.flamestream.core.data.DataItem;
import com.spbsu.flamestream.core.data.PayloadDataItem;
import com.spbsu.flamestream.core.data.meta.GlobalTime;
import com.spbsu.flamestream.core.data.meta.Meta;
import com.spbsu.flamestream.runtime.actor.LoggingActor;
import com.spbsu.flamestream.runtime.source.api.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import static java.util.Collections.synchronizedSet;

public final class StreamFront<T> extends LoggingActor {
  private final int frontId = 1;
  private final Map<ActorRef> holes = synchronizedSet(new HashSet<>());


  private final NavigableSet<DataItem<T>> history = new ConcurrentSkipListSet<>(Comparator.comparing(DataItem::meta));

  private StreamFront(Stream<T> stream) {
    CompletableFuture.runAsync(new Runnable() {
      private final int id = frontId;
      private long prevGlobalTs = 0;

      @Override
      public void run() {
        stream.map(data -> new PayloadDataItem<>(currentMeta(), data))
                .map(history::add)
                .forEach(dataItem -> holes.forEach(hole -> hole.tell(dataItem, self())));

        holes.forEach(hole -> hole.tell(new Heartbeat(GlobalTime.MAX), self()));
      }

      private Meta currentMeta() {
        long globalTs = System.currentTimeMillis();
        if (globalTs <= prevGlobalTs) {
          globalTs = prevGlobalTs + 1;
        }
        prevGlobalTs = globalTs;

        final GlobalTime globalTime = new GlobalTime(globalTs, id);
        return Meta.meta(globalTime);
      }
    });
  }

  public static <T> Props props(Stream<T> stream) {
    return Props.create(Stream.class, stream);
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create()
            .match(NewHole.class, this::onNewHole)
            .match(Cancel.class, this::onCancel)
            .match(SlowDownBro.class, this::onSlowDown)
            .match(Replay.class, this::onReplay)
            .build();
  }

  private void onReplay(Replay replay) {
    history.subSet(
            new PayloadDataItem<>(Meta.meta(replay.from()), null),
            new PayloadDataItem<>(Meta.meta(replay.to()), null)
    ).forEach(dataItem -> holes.forEach(hole -> hole.tell(dataItem, sender())));
  }

  private void onSlowDown(SlowDownBro slowDownBro) {
    //Sorry, Can't do that, bro
  }

  private void onCancel(Cancel cancel) {
    holes.remove(cancel.hole());
  }

  private void onNewHole(NewHole h) {
    holes.add(h.hole());
  }
}
