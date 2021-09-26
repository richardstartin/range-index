package io.github.richardstartin.rangeindex;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.function.LongSupplier;

public class LTEBenchmark {

  @State(Scope.Benchmark)
  public static class AbstractState {
    @Param({"NORMAL(100000,10)", "UNIFORM(0,100000)", "UNIFORM(1000000,1100000)", "EXP(0.0001)"})
    String distribution;

    @Param("10000000")
    long max;

    @Param("42")
    long seed;
  }

  public static class StateWithBase extends AbstractState{
    @Param({"2", "4", "10", "16"})
    private int base;

    @Param({"RELATIVE", "TRIMMED", "UNCOMPRESSED"})
    IndexType indexType;


    long threshold;

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
  }

  public static class RangeBitmapState extends AbstractState {
    @Param({"RANGEBITMAP"})
    String indexType;

    @Param("2")
    int base;

    long threshold;

    private RangeIndex index;

    @Setup(Level.Trial)
    public void setup() {
      LongSupplier data = Distribution.parse(seed, distribution);
      long[] values = new long[(int)max];
      long minValue = Long.MAX_VALUE;
      long maxValue = Long.MIN_VALUE;
      for (int i = 0; i < max; ++i) {
        values[i] = data.getAsLong();
        minValue = Math.min(values[i], minValue);
        maxValue = Math.max(values[i], maxValue);
      }
      Accumulator<RangeBitmapIndex> accumulator = RangeBitmapIndex.accumulator(maxValue - minValue);
      for (long value : values) {
        accumulator.add(value - minValue);
      }
      this.index = accumulator.seal();
      Arrays.sort(values);
      this.threshold = values[values.length / 2] - minValue;
    }
  }

  @Benchmark
  public Object based(StateWithBase state) {
    return state.index.lessThanOrEqual(state.threshold);
  }

  @Benchmark
  public Object rangeBitmap(RangeBitmapState state) {
    return state.index.lessThanOrEqual(state.threshold);
  }
}
