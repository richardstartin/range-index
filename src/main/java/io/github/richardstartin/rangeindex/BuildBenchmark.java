package io.github.richardstartin.rangeindex;

import org.openjdk.jmh.annotations.*;

import java.util.function.LongSupplier;

@State(Scope.Thread)
public class BuildBenchmark {

  @Param({"2", "4", "10", "16"})
  private int base;

  @Param({"NORMAL(100000,10)", "UNIFORM(0,100000)", "UNIFORM(1000000,1100000)", "EXP(0.0001)"})
  String distribution;

  @Param("10000000")
  long max;

  @Param("42")
  long seed;

  @Param
  IndexType indexType;

  long[] data;

  @Setup(Level.Trial)
  public void setup() {
    LongSupplier supplier = Distribution.parse(seed, distribution);
    this.data = new long[(int)max];
    for (int i = 0; i < max; ++i) {
      data[i] = supplier.getAsLong();
    }
  }

  @Benchmark
  public RangeIndex build(IndexSizeCounter counter) {
    Accumulator<? extends RangeIndex> acc = indexType.accumulator(base);
    for (long datum : data) {
      acc.add(datum);
    }
    RangeIndex index = acc.seal();
    counter.count = index.bitmapCount();
    counter.size = index.serializedSizeInBytes();
    return index;
  }
}
