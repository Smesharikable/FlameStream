package com.spbsu.flamestream.runtime;

import com.spbsu.flamestream.common.FlameStreamSuite;
import com.spbsu.flamestream.core.graph.AtomicGraph;
import com.spbsu.flamestream.core.graph.Graph;
import com.spbsu.flamestream.core.graph.HashFunction;
import com.spbsu.flamestream.core.graph.barrier.BarrierSuite;
import com.spbsu.flamestream.core.graph.ops.Grouping;
import com.spbsu.flamestream.core.graph.ops.StatelessMap;
import com.spbsu.flamestream.core.graph.source.AbstractSource;
import com.spbsu.flamestream.core.graph.source.SimpleSource;
import com.spbsu.flamestream.runtime.environment.local.LocalClusterEnvironment;
import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GroupingAcceptanceTest extends FlameStreamSuite {
  private static void doIt(HashFunction<? super Long> groupHash,
          HashFunction<? super Long> filterHash,
          BiPredicate<? super Long, ? super Long> equalz) {
    try (LocalClusterEnvironment lce = new LocalClusterEnvironment(5);
            TestEnvironment environment = new TestEnvironment(lce)) {
      final Set<List<Long>> result = new HashSet<>();
      final int window = 7;

      //noinspection unchecked
      final Consumer<Object> sink = environment.deploy(GroupingAcceptanceTest.groupGraph(
              environment.wrapInSink(HashFunction.OBJECT_HASH, di -> result.add((List<Long>) di)),
              window,
              groupHash,
              equalz,
              filterHash
      ).flattened(), 10, 1, 1);

      final List<Long> source = new Random().longs(1000).boxed().collect(Collectors.toList());
      source.forEach(sink);
      environment.awaitTicks();

      Assert.assertEquals(new HashSet<>(result), GroupingAcceptanceTest.expected(source, groupHash, window));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static Set<List<Long>> expected(List<Long> in, HashFunction<? super Long> hash, int window) {
    final Set<List<Long>> mustHave = new HashSet<>();

    final Map<Integer, List<Long>> buckets = in.stream().collect(Collectors.groupingBy(hash::hash));

    for (List<Long> bucket : buckets.values()) {
      for (int i = 0; i < Math.min(bucket.size(), window - 1); ++i) {
        mustHave.add(bucket.subList(0, i + 1));
      }

      Seq.seq(bucket).sliding(window).map(Collectable::toList).forEach(mustHave::add);
    }

    return mustHave;
  }

  private static Graph groupGraph(AtomicGraph sink,
          int window,
          HashFunction<? super Long> groupHash,
          BiPredicate<? super Long, ? super Long> equalz,
          HashFunction<? super Long> filterHash) {
    final AbstractSource source = new SimpleSource();
    final StatelessMap<Long, Long> filter = new StatelessMap<>(new Id(), filterHash);
    final Grouping<Long> grouping = new Grouping<>(groupHash, equalz, window);

    final BarrierSuite<Long> barrier = new BarrierSuite<>(sink);

    return source.fuse(filter, source.outPort(), filter.inPort())
            .fuse(grouping, filter.outPort(), grouping.inPort())
            .fuse(barrier, grouping.outPort(), barrier.inPort());
  }

  @SuppressWarnings("Convert2Lambda")
  @Test
  public void noReorderingSingleHash() {
    //noinspection Convert2Lambda
    GroupingAcceptanceTest.doIt(
            HashFunction.constantHash(100),
            HashFunction.constantHash(100),
            new BiPredicate<Long, Long>() {
              @Override
              public boolean test(Long a, Long b) {
                return true;
              }
            }
    );
  }

  @Test
  public void noReorderingMultipleHash() {
    final HashFunction<Long> hash = HashFunction.uniformLimitedHash(100);
    GroupingAcceptanceTest.doIt(hash, HashFunction.constantHash(100), new BiPredicate<Long, Long>() {

      final HashFunction<Long> hashFunction = hash;

      @Override
      public boolean test(Long a, Long b) {
        return hashFunction.hash(a) == hashFunction.hash(b);
      }
    });
  }

  @SuppressWarnings("Convert2Lambda")
  @Test
  public void reorderingSingleHash() {
    //noinspection Convert2Lambda
    GroupingAcceptanceTest.doIt(
            HashFunction.constantHash(100),
            HashFunction.uniformLimitedHash(100),
            new BiPredicate<Long, Long>() {
              @Override
              public boolean test(Long a, Long b) {
                return true;
              }
            }
    );
  }

  @Test
  public void reorderingMultipleHash() {
    final HashFunction<Long> hash = HashFunction.uniformLimitedHash(100);

    GroupingAcceptanceTest.doIt(hash, HashFunction.uniformLimitedHash(100), new BiPredicate<Long, Long>() {
      private final HashFunction<Long> hashFunction = hash;

      @Override
      public boolean test(Long a, Long b) {
        return hashFunction.hash(a) == hashFunction.hash(b);
      }
    });
  }

  private static final class Id implements Function<Long, Long> {
    @Override
    public Long apply(Long value) {
      return value;
    }
  }
}

