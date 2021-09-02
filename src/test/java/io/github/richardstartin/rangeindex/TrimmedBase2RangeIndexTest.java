package io.github.richardstartin.rangeindex;

public class TrimmedBase2RangeIndexTest extends AbstractIndexTest<TrimmedBase2RangeIndex> {
  @Override
  Accumulator<TrimmedBase2RangeIndex> create() {
    return TrimmedBase2RangeIndex.accumulator();
  }
}
