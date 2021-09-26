package io.github.richardstartin.rangeindex;

import org.openjdk.jmh.annotations.*;

import java.util.function.LongSupplier;

@State(Scope.Thread)
public class BuildBenchmark {

  @State(Scope.Thread)
  public static class AbstractState {
    @Param({"NORMAL(100000,10)", "UNIFORM(0,100000)", "UNIFORM(1000000,1100000)", "EXP(0.0001)"})
    String distribution;

    @Param("10000000")
    long max;

    @Param("42")
    long seed;

    long[] data;
    long maxValue;
    long minValue;
    @Setup(Level.Trial)
    public void setup() {
      LongSupplier supplier = Distribution.parse(seed, distribution);
      this.data = new long[(int)max];
      for (int i = 0; i < max; ++i) {
        data[i] = supplier.getAsLong();
        minValue = Math.min(minValue, data[i]);
        maxValue = Math.max(maxValue, data[i]);
      }
    }
  }

  @State(Scope.Thread)
  public static class BasedState extends AbstractState {
    @Param({"2", "4", "10", "16"})
    private int base;

    @Param({"RELATIVE", "TRIMMED", "UNCOMPRESSED"})
    IndexType indexType;
  }


  @State(Scope.Thread)
  public static class RangeBitmapState extends AbstractState {
    @Param("2")
    private int base;

    @Param("RANGEBITMAP")
    String indexType;
  }


  @Benchmark
  public RangeIndex basedBuild(IndexSizeCounter counter, BasedState state) {
    Accumulator<? extends RangeIndex> acc = state.indexType.accumulator(state.base);
    for (long datum : state.data) {
      acc.add(datum - (state.indexType == IndexType.RELATIVE ? state.minValue : 0));
    }
    RangeIndex index = acc.seal();
    counter.count = index.bitmapCount();
    counter.size = index.serializedSizeInBytes();
    return index;
  }

  @Benchmark
  public RangeIndex rangeBitmapBuild(IndexSizeCounter counter, RangeBitmapState state) {
    Accumulator<? extends RangeIndex> acc = RangeBitmapIndex.accumulator(state.maxValue - state.minValue);
    for (long datum : state.data) {
      acc.add(datum - state.minValue);
    }
    RangeIndex index = acc.seal();
    counter.count = index.bitmapCount();
    counter.size = index.serializedSizeInBytes();
    return index;
  }
}
