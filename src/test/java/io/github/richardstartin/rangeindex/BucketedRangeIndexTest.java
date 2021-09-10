package io.github.richardstartin.rangeindex;

import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BucketedRangeIndexTest {

  @Test
  public void testBucketedRangeIndex() {
    SplittableRandom random = new SplittableRandom(42);
    Accumulator<BucketedRangeIndex> acc = BucketedRangeIndex.accumulator();
    int[] counts = new int[1_000_000 / 20];
    for (int i = 0; i < 1_000_000; ++i) {
      long value = random.nextLong(1_000_000);
      acc.add(value);
      counts[(int)Math.floorDiv(value, 20)]++;
    }
    BucketedRangeIndex index = acc.seal();
    long sum = 0;
    for (int i = 0; i < counts.length; ++i) {
      sum += counts[i];
      if (i % 1000 == 0) {
        // way too slow to run them all
        assertEquals(sum, index.lessThanOrEqual(20 * i).getCardinality(), "" + (20 * i));
      }
    }
  }
}
