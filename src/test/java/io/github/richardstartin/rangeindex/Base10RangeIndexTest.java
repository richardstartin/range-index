package io.github.richardstartin.rangeindex;

public class Base10RangeIndexTest extends AbstractIndexTest<Base10RangeIndex> {
  @Override
  Accumulator<Base10RangeIndex> create() {
    return Base10RangeIndex.accumulator();
  }
}
