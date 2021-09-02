package io.github.richardstartin.rangeindex;

public interface Accumulator<Index extends RangeIndex> {
  void add(long value);
  Index seal();
}
