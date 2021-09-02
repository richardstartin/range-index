package io.github.richardstartin.rangeindex;

class Base4RangeIndexTest extends AbstractIndexTest<Base4RangeIndex> {

  @Override
  Accumulator<Base4RangeIndex> create() {
    return Base4RangeIndex.accumulator();
  }
}