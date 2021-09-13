package io.github.richardstartin.rangeindex;

public class ScanTest extends AbstractIndexTest<Scan> {

  @Override
  Accumulator<Scan> create() {
    return Scan.accumulator();
  }
}
