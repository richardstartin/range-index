package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;
import java.util.BitSet;

import static org.roaringbitmap.RoaringBitmap.bitmapOfRange;

public class UncompressedBase4RangeIndex implements RangeIndex<BitSet> {

  public UncompressedBase4RangeIndex(long max, int slices, BitSet[] bitmaps) {
    this.max = max;
    this.slices = slices;
    this.bitmaps = bitmaps;
  }

  public static Accumulator<UncompressedBase4RangeIndex> accumulator() {
    return new Builder();
  }

  private final long max;
  private final int slices;
  private final BitSet[] bitmaps;

  @Override
  public BitSet lessThanOrEqual(long max) {
    int digit = (int) max & 3;
    BitSet bitmap = digit < 3 ? (BitSet) bitmaps[digit].clone() : all();
    for (int i = 1; i < slices; ++i) {
      max >>>= 2;
      digit = (int) max & 3;
      if (digit < 3) {
        bitmap.and(bitmaps[i * 3 + digit]);
      }
      if (digit != 0) {
        bitmap.or(bitmaps[i * 3 + digit - 1]);
      }
    }
    return bitmap;
  }

  private BitSet all() {
    BitSet all = new BitSet((int) max);
    all.set(0, (int)max);
    return all;
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


  @SuppressWarnings("unchecked")
  private static final class Builder implements Accumulator<UncompressedBase4RangeIndex> {

    private final BitSet[] bitsets;
    private int rid;
    private long mask;

    public Builder() {
      this.bitsets = new BitSet[3 * 32];
      Arrays.setAll(bitsets, i -> new BitSet());
    }

    @Override
    public void add(long value) {
      mask |= value;
      for (int i = 0; i < 32; ++i) {
        int digit = (int)value & 3;
        // mark every bitmap greater than or equal to the digit
        for (int j = digit; j < 3; ++j) {
          bitsets[i * 3 + j].set(rid);
        }
        value >>>= 2;
      }
      ++rid;
    }

    @Override
    public UncompressedBase4RangeIndex seal() {
      int discard = Long.numberOfLeadingZeros(mask) >>> 1;
      return new UncompressedBase4RangeIndex(rid, 32 - discard,
              Arrays.copyOf(bitsets, bitsets.length - 3 * discard));
    }
  }
}
