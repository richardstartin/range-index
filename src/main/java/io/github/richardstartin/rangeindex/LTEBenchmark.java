package io.github.richardstartin.rangeindex;

import org.openjdk.jmh.annotations.*;
import org.roaringbitmap.RoaringBitmap;

import java.util.Arrays;
import java.util.function.LongSupplier;

@State(Scope.Benchmark)
public class LTEBenchmark {

  @Param({"NORMAL(100000,10)", "UNIFORM(0,100000)", "UNIFORM(1000000,1100000)", "EXP(0.0001)"})
  String distribution;

  @Param("10000000")
  long max;

  @Param("42")
  long seed;
  long threshold;

  @Param
  IndexType indexType;

  private RangeIndex index;

  @Setup(Level.Trial)
  public void setup() {
    Accumulator<? extends RangeIndex> accumulator = indexType.accumulator();
    LongSupplier data = Distribution.parse(seed, distribution);
    long[] values = new long[(int)max];
    for (int i = 0; i < max; ++i) {
      values[i] = data.getAsLong();
      accumulator.add(values[i]);
    }
    this.index = accumulator.seal();
    Arrays.sort(values);
    this.threshold = values[values.length / 2];
  }

  @Benchmark
  public RoaringBitmap lte() {
    return index.lessThanOrEqual(threshold);
  }
}
