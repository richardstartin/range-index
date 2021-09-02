package io.github.richardstartin.rangeindex;

public abstract class GenericBaseRangeIndexTest extends AbstractIndexTest<GenericBaseRangeIndex> {

  private final int base;

  protected GenericBaseRangeIndexTest(int base) {
    this.base = base;
  }

  @Override
  Accumulator<GenericBaseRangeIndex> create() {
    return GenericBaseRangeIndex.accumulator(base);
  }
}

class GenericBase10RangeIndexTest extends GenericBaseRangeIndexTest {
  public GenericBase10RangeIndexTest() {
    super(10);
  }
}

class GenericBase2RangeIndexTest extends GenericBaseRangeIndexTest {
  public GenericBase2RangeIndexTest() {
    super(2);
  }
}

class GenericBase4RangeIndexTest extends GenericBaseRangeIndexTest {
  public GenericBase4RangeIndexTest() {
    super(4);
  }
}

class GenericBase8RangeIndexTest extends GenericBaseRangeIndexTest {
  public GenericBase8RangeIndexTest() {
    super(8);
  }
}

class GenericBase16RangeIndexTest extends GenericBaseRangeIndexTest {
  public GenericBase16RangeIndexTest() {
    super(16);
  }
}
