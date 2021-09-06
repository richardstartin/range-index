package io.github.richardstartin.rangeindex;

import org.openjdk.jmh.annotations.*;
import org.roaringbitmap.RoaringBitmap;

import java.util.Arrays;
import java.util.function.LongSupplier;

@State(Scope.Benchmark)
public class LTEBenchmark {

  @Param({"2", "4", "10", "16"})
  private int base;

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
    Accumulator<? extends RangeIndex> accumulator = indexType.accumulator(base);
    LongSupplier data = Distribution.parse(seed, distribution);
    long[] values = new long[(int)max];
    long minValue = Long.MAX_VALUE;
    for (int i = 0; i < max; ++i) {
      values[i] = data.getAsLong();
      if (indexType != IndexType.RELATIVE) {
        accumulator.add(values[i]);
      } else {
        minValue = Math.min(values[i], minValue);
      }
    }
    if (indexType == IndexType.RELATIVE) {
      for (long value : values) {
        accumulator.add(value - minValue);
      }
    }
    this.index = accumulator.seal();
    Arrays.sort(values);
    this.threshold = values[values.length / 2] - (indexType == IndexType.RELATIVE ? minValue : 0);
  }

  @Benchmark
  public RoaringBitmap lte() {
    return index.lessThanOrEqual(threshold);
  }
}
