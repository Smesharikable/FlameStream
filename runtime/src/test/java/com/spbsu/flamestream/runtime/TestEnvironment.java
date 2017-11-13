package com.spbsu.flamestream.runtime;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import com.spbsu.flamestream.core.graph.AtomicGraph;
import com.spbsu.flamestream.core.graph.ComposedGraph;
import com.spbsu.flamestream.runtime.actor.LoggingActor;
import com.spbsu.flamestream.runtime.environment.Environment;
import com.spbsu.flamestream.runtime.front.ActorFront;
import com.spbsu.flamestream.runtime.range.HashRange;
import com.spbsu.flamestream.runtime.raw.RawData;
import com.spbsu.flamestream.runtime.raw.SingleRawData;
import com.spbsu.flamestream.runtime.tick.TickInfo;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * User: Artem
 * Date: 28.09.2017
 */
public class TestEnvironment implements Environment {
  private static final long DEFAULT_TEST_WINDOW = 10;
  private final ActorSystem system;

  private final Environment innerEnvironment;
  private final long windowInMillis;
  private final Set<Integer> fronts = new HashSet<>();

  public TestEnvironment(Environment inner) {
    this(inner, DEFAULT_TEST_WINDOW);
  }

  public TestEnvironment(Environment inner, long windowInMillis) {
    this.innerEnvironment = inner;
    this.windowInMillis = windowInMillis;

    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 23456)
            .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + "localhost"))
            .withFallback(ConfigFactory.load("remote"));
    this.system = ActorSystem.create("environment", config);
  }

  // TODO: 13.11.2017 accept graph instead of composed graph
  public void deploy(ComposedGraph<AtomicGraph> graph, int tickLengthSeconds, int ticksCount) {
    final Map<HashRange, Integer> workers = rangeMappingForTick();
    final long tickMills = SECONDS.toMillis(tickLengthSeconds);

    long startTs = System.currentTimeMillis();
    for (int i = 0; i < ticksCount; ++i, startTs += tickMills) {
      //noinspection ConstantConditions
      final TickInfo tickInfo = new TickInfo(
              i,
              startTs,
              startTs + tickMills,
              graph,
              workers.values().stream().min(Integer::compareTo).get(),
              workers,
              fronts, windowInMillis,
              i == 0 ? emptySet() : singleton(i - 1L)
      );
      innerEnvironment.deploy(tickInfo);
    }

    //This sleep doesn't affect correctness.
    // Only reduces stashing overhead for first several items
    try {
      SECONDS.sleep(2);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public Consumer<Object> randomFrontConsumer(int n) {
    final ActorRef balancingActor = system.actorOf(Props.create(() -> new LoggingActor() {
      private final List<ActorRef> fronts = new ArrayList<>();

      @Override
      public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(ActorIdentity.class, i -> fronts.add(sender()))
                .match(
                        RawData.class,
                        r -> fronts.get(ThreadLocalRandom.current().nextInt(fronts.size())).tell(r, self())
                )
                .build();
      }
    }));

    final ActorPath path = balancingActor.path();

    final List<Props> props = IntStream.range(0, n)
            .peek(fronts::add)
            .mapToObj(id -> ActorFront.props(id, path))
            .collect(Collectors.toList());

    final List<Integer> workers = new ArrayList<>(availableWorkers());

    for (int i = 0; i < props.size(); i++) {
      deployFront(workers.get(i % workers.size()), i, props.get(i));
    }

    return o -> balancingActor.tell(new SingleRawData<>(o), ActorRef.noSender());
  }

  private Map<HashRange, Integer> rangeMappingForTick() {
    final Map<HashRange, Integer> result = new HashMap<>();
    final Set<Integer> workerIds = innerEnvironment.availableWorkers();

    final int step = (int) (((long) Integer.MAX_VALUE - Integer.MIN_VALUE) / workerIds.size());
    long left = Integer.MIN_VALUE;
    long right = left + step;

    for (int workerId : workerIds) {
      result.put(new HashRange((int) left, (int) right), workerId);

      left += step;
      right = Math.min(Integer.MAX_VALUE, right + step);
    }

    return result;
  }

  public void awaitTick(int seconds) {
    try {
      SECONDS.sleep(seconds);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      innerEnvironment.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deploy(TickInfo tickInfo) {
    innerEnvironment.deploy(tickInfo);
  }

  @Override
  public void deployFront(int nodeId, int frontId, Props frontProps) {
    innerEnvironment.deployFront(nodeId, frontId, frontProps);
  }

  @Override
  public Set<Integer> availableWorkers() {
    return innerEnvironment.availableWorkers();
  }

  @Override
  public <T> AtomicGraph wrapInSink(ToIntFunction<? super T> hash, Consumer<? super T> mySuperConsumer) {
    return innerEnvironment.wrapInSink(hash, mySuperConsumer);
  }
}
