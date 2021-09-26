package io.github.richardstartin.rangeindex;

import java.util.Arrays;
import java.util.BitSet;

public class UncompressedBase2RangeIndex implements RangeIndex<BitSet> {

  public static Accumulator<UncompressedBase2RangeIndex> accumulator() {
    return new Builder();
  }

  UncompressedBase2RangeIndex(long maxRid, BitSet[] bitmaps) {
    this.maxRid = maxRid;
    this.bitmaps = bitmaps;
  }

  private final long maxRid;
  private final BitSet[] bitmaps;

  @Override
  public BitSet lessThanOrEqual(long max) {
    BitSet bitmap = (max & 1) == 0 ? (BitSet) bitmaps[0].clone() : all();
    long mask = 2;
    for (int i = 1; i < bitmaps.length; ++i) {
      if ((max & mask) != mask) {
        bitmap.and(bitmaps[i]);
      } else {
        bitmap.or(bitmaps[i]);
      }
      mask <<= 1;
    }
    return bitmap;
  }

  private BitSet all() {
    BitSet all = new BitSet((int) maxRid);
    all.set(0, (int)maxRid);
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

  private static final class Builder implements Accumulator<UncompressedBase2RangeIndex> {

    private final BitSet[] bitSets;
    private int rid = 0;
    private long mask = 0;

    public Builder() {
      bitSets = new BitSet[64];
      Arrays.setAll(bitSets, i -> new BitSet());
    }

    public void add(long value) {
      long bits = ~value;
      mask |= value;
      while (bits != 0) {
        int index = Long.numberOfTrailingZeros(bits);
        bitSets[index].set(rid);
        bits &= (bits - 1);
      }
      rid++;
    }

    public UncompressedBase2RangeIndex seal() {
      int numDiscarded = Long.numberOfLeadingZeros(mask);
      return new UncompressedBase2RangeIndex(rid, Arrays.copyOf(bitSets, bitSets.length - numDiscarded));
    }
  }
}
