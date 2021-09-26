package io.github.richardstartin.rangeindex;

import java.util.Arrays;
import java.util.BitSet;

public class UncompressedBase16RangeIndex implements RangeIndex<BitSet> {

  public UncompressedBase16RangeIndex(long max, int slices, BitSet[] bitmaps) {
    this.max = max;
    this.slices = slices;
    this.bitmaps = bitmaps;
  }

  public static Accumulator<UncompressedBase16RangeIndex> accumulator() {
    return new Builder();
  }

  private final long max;
  private final int slices;
  private final BitSet[] bitmaps;

  @Override
  public BitSet lessThanOrEqual(long max) {
    int digit = (int) max & 15;
    BitSet bitmap = digit < 15 ? (BitSet) bitmaps[digit].clone() : all();
    for (int i = 1; i < slices; ++i) {
      max >>>= 4;
      digit = (int) max & 15;
      if (digit < 15) {
        bitmap.and(bitmaps[i * 15 + digit]);
      }
      if (digit != 0) {
        bitmap.or(bitmaps[i * 15 + digit - 1]);
      }
    }
    return bitmap;
  }

  @Override
  public long serializedSizeInBytes() {
    return RangeIndex.serializedSizeInBytes(bitmaps);
  }

  @Override
  public int bitmapCount() {
    int count = 0;
    for (BitSet bitmap : bitmaps) {
      count += bitmap == null ? 0 : 1;
    }
    return count;
  }

  private BitSet all() {
    BitSet all = new BitSet((int) max);
    all.set(0, (int)max);
    return all;
  }

  @SuppressWarnings("unchecked")
  private static final class Builder implements Accumulator<UncompressedBase16RangeIndex> {

    private final BitSet[] bitsets;
    private int rid;
    private long mask;

    public Builder() {
      this.bitsets = new BitSet[15 * 16];
      Arrays.setAll(bitsets, i -> new BitSet());
    }

    @Override
    public void add(long value) {
      mask |= value;
      for (int i = 0; i < 16; ++i) {
        int digit = (int)value & 15;
        // mark every bitmap greater than or equal to the digit
        for (int j = digit; j < 15; ++j) {
          bitsets[i * 15 + j].set(rid);
        }
        value >>>= 4;
      }
      ++rid;
    }

    @Override
    public UncompressedBase16RangeIndex seal() {
      int discard = Long.numberOfLeadingZeros(mask) >>> 2;
      return new UncompressedBase16RangeIndex(rid, 16 - discard, Arrays.copyOf(bitsets, bitsets.length - 3 * discard));
    }
  }
}
