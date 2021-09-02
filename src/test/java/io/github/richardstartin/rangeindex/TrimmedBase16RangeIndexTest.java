package io.github.richardstartin.rangeindex;

public class TrimmedBase16RangeIndexTest extends AbstractIndexTest<TrimmedBase16RangeIndex> {
  @Override
  Accumulator<TrimmedBase16RangeIndex> create() {
    return TrimmedBase16RangeIndex.accumulator();
  }
}
