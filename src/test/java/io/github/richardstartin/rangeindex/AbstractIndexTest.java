package io.github.richardstartin.rangeindex;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.roaringbitmap.RoaringBitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIndexTest<Index extends RangeIndex> {

  abstract Accumulator<Index> create();
  final LongStream values() {
    return LongStream.range(0, 1_000_000);
  }

  private Index index;
  private long min = Long.MAX_VALUE;
  private long max = Long.MIN_VALUE;
  private long count = 0;

  @BeforeAll
  public void before() {
    // TODO check if it's possible to parameterise this by IndexType enum to remove boiler plate
    Accumulator<Index> acc = create();
    var it = values().iterator();
    while (it.hasNext()) {
      long value = it.nextLong();
      min = Math.min(min, value);
      max = Math.max(max, value);
      ++count;
      acc.add(value);
    }
    this.index = acc.seal();
  }

  public static Stream<Arguments> offsets() {
    List<Arguments> args = new ArrayList<>();
    int base = 1;
    for (int i = 0; i < 6; ++i) {
      for (int offset = 0; offset < 3; ++offset) {
        args.add(Arguments.of(base, offset));
      }
      base *= 10;
    }
    return args.stream();
  }

  // TODO tests assume rid == value for simplicity, but this precludes testing more interesting data

  @ParameterizedTest
  @MethodSource("offsets")
  public void testLTE(int base, int offset) {
    RoaringBitmap result = index.lessThanOrEqual(base + offset);
    RoaringBitmap expected = new RoaringBitmap();
    expected.add(0L, base + offset + 1);
    assertEquals(expected, result);
  }

  @Test
  public void testLessThanOrEqualToMax() {
    assertEquals(count, index.lessThanOrEqual(max).getCardinality());
    assertEquals(count, index.lessThanOrEqual(max + 1).getCardinality());
    assertEquals(count, index.lessThanOrEqual(max + 2).getCardinality());
  }

  @Test
  public void testLessThanOrEqualToMin() {
    assertEquals(1, index.lessThanOrEqual(min).getCardinality());
  }

  //@Test
  public void testBetween() {
    RoaringBitmap expected = new RoaringBitmap();
    expected.add(min + 1, max);
    assertEquals(expected.getCardinality(), index.between(min + 1, max - 1).getCardinality());
    assertEquals(expected, index.between(min + 1, max - 1));
  }
}
