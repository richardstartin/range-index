package io.github.richardstartin.rangeindex;

class Base16RangeIndexTest extends AbstractIndexTest<Base16RangeIndex> {

  @Override
  Accumulator<Base16RangeIndex> create() {
    return Base16RangeIndex.accumulator();
  }
}