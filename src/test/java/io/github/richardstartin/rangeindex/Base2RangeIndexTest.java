package io.github.richardstartin.rangeindex;

class Base2RangeIndexTest extends AbstractIndexTest<Base2RangeIndex> {

  @Override
  Accumulator<Base2RangeIndex> create() {
    return Base2RangeIndex.accumulator();
  }
}