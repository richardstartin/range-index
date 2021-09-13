package io.github.richardstartin.rangeindex;

public class BucketedRangeScanTest extends AbstractIndexTest<BucketedRangeScan> {
  @Override
  Accumulator<BucketedRangeScan> create() {
    return BucketedRangeScan.accumulator();
  }
}
