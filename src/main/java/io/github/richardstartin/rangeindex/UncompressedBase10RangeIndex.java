package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;
import java.util.BitSet;

public class UncompressedBase10RangeIndex implements RangeIndex<BitSet> {

  private static final int SLICE_COUNT = (int)Math.log10(Long.MAX_VALUE);

  public UncompressedBase10RangeIndex(long max, int sliceCount, BitSet[] bitmaps) {
    this.max = max;
    this.sliceCount = sliceCount;
    this.bitmaps = bitmaps;
  }

  public static Accumulator<UncompressedBase10RangeIndex> accumulator() {
    return new Builder();
  }

  private final long max;
  private final int sliceCount;
  private final BitSet[] bitmaps;

  @Override
  public BitSet lessThanOrEqual(long max) {
    int digit = (int) (max % 10);
    BitSet bitmap = digit < 9 ? (BitSet) bitmaps[digit].clone() : all();
    for (int i = 1; i < sliceCount; ++i) {
      max /= 10;
      digit = (int) (max % 10);
      if (digit < 9) {
        bitmap.and(bitmaps[i * 9 + digit]);
      }
      if (digit != 0) {
        bitmap.or(bitmaps[i * 9 + digit - 1]);
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

  private static final class Builder implements Accumulator<UncompressedBase10RangeIndex> {
    private int rid;
    private int maxLog;
    private final BitSet[] bitsets;

    private Builder() {
      this.bitsets = new BitSet[SLICE_COUNT * 9];
      Arrays.setAll(bitsets, i -> new BitSet());
    }

    @Override
    public void add(long value) {
      // this can be sped up if necessary
      this.maxLog = Math.max(maxLog, (int)Math.log10(value));
      for (int i = 0; i < SLICE_COUNT; ++i) {
        int digit = (int)(value % 10);
        // mark every bitmap greater than or equal to the digit
        for (int j = digit; j < 9; ++j) {
          bitsets[i * 9 + j].set(rid);
        }
        value /= 10;
      }
      ++rid;
    }

    @Override
    public UncompressedBase10RangeIndex seal() {
      return new UncompressedBase10RangeIndex(rid, maxLog, bitsets);
    }
  }
}
