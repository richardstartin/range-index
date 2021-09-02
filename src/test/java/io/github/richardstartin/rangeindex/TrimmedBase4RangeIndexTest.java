package io.github.richardstartin.rangeindex;

public class TrimmedBase4RangeIndexTest extends AbstractIndexTest<TrimmedBase4RangeIndex> {
  @Override
  Accumulator<TrimmedBase4RangeIndex> create() {
    return TrimmedBase4RangeIndex.accumulator();
  }
}
