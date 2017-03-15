package com.spbsu.datastream.core.materializer.manager;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.spbsu.datastream.core.HashRange;
import com.spbsu.datastream.core.graph.AtomicGraph;
import com.spbsu.datastream.core.graph.InPort;
import com.spbsu.datastream.core.graph.TheGraph;
import com.spbsu.datastream.core.materializer.TickContext;
import com.spbsu.datastream.core.materializer.TickContextImpl;
import com.spbsu.datastream.core.materializer.atomic.AtomicActor;
import com.spbsu.datastream.core.materializer.atomic.AtomicHandleImpl;
import com.spbsu.datastream.core.routing.ForkRouter;
import com.spbsu.datastream.core.routing.TickLocalRouter;
import scala.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.spbsu.datastream.core.materializer.manager.TickGraphManagerApi.TickStarted;

public class TickGraphManager extends UntypedActor {
  private final LoggingAdapter LOG = Logging.getLogger(context().system(), self());

  private TickGraphManager(final ActorRef remoteRouter, final HashRange range, final TheGraph graph) {
    final ActorRef router = context().actorOf(ForkRouter.props(range, remoteRouter), "forkRouter");

    final TickContext tickContext = new TickContextImpl(graph.downstreams(), router);

    final Map<AtomicGraph, ActorRef> inMapping = initializeAtomics(graph.subGraphs(), tickContext);
    final ActorRef localRouter = localRouter(flatKey(inMapping));

    tickContext.forkRouter().tell(localRouter, self());
  }

  public static Props props(final ActorRef remoteRouter, final HashRange range, final TheGraph graph) {
    return Props.create(TickGraphManager.class, remoteRouter, range, graph);
  }

  @Override
  public void preStart() throws Exception {
    LOG.info("Starting...");
    super.preStart();
  }

  @Override
  public void preRestart(final Throwable reason, final Option<Object> message) throws Exception {
    LOG.error("Restarting, reason: {}, message: {}", reason, message);
    super.preRestart(reason, message);
  }

  @Override
  public void onReceive(final Object message) throws Throwable {
    LOG.debug("Received: {}", message);

    if (message instanceof TickStarted) {
      getContext().getChildren().forEach(actorRef -> actorRef.tell(new TickStarted(), getSelf()));
    }
  }

  private ActorRef localRouter(final Map<InPort, ActorRef> portMappings) {
    return context().actorOf(TickLocalRouter.props(portMappings), "localRouter");
  }

  private Map<InPort, ActorRef> flatKey(final Map<AtomicGraph, ActorRef> map) {
    final Map<InPort, ActorRef> result = new HashMap<>();
    for (Map.Entry<AtomicGraph, ActorRef> e : map.entrySet()) {
      for (InPort port : e.getKey().inPorts()) {
        result.put(port, e.getValue());
      }
    }
    return result;
  }

  private Map<AtomicGraph, ActorRef> initializeAtomics(final Set<? extends AtomicGraph> atomicGraphs,
                                                       final TickContext context) {
    return atomicGraphs.stream().collect(Collectors.toMap(Function.identity(), a -> actorForAtomic(a, context)));
  }

  private ActorRef actorForAtomic(final AtomicGraph atomic, final TickContext context) {
    LOG.info("Creating actor for atomic {}", atomic);
    return context().actorOf(AtomicActor.props(atomic, new AtomicHandleImpl(context)), UUID.randomUUID().toString());
  }
}
