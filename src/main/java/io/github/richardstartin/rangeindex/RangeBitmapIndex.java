package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RangeBitmap;
import org.roaringbitmap.RoaringBitmap;

public class RangeBitmapIndex implements RangeIndex<RoaringBitmap> {

  public static Accumulator<RangeBitmapIndex> accumulator(long maxValue) {
    RangeBitmap.Appender appender = RangeBitmap.appender(maxValue);
    return new Accumulator<>() {
      @Override
      public void add(long value) {
        appender.add(value);
      }

      @Override
      public RangeBitmapIndex seal() {
        int serializedSize = appender.serializedSizeInBytes();
        return new RangeBitmapIndex(appender.build(), serializedSize, Long.bitCount(rangeMaxForLimit(maxValue)));
      }
    };
  }

  private final RangeBitmap rangeBitmap;
  private final int serializedSize;
  private final int slices;

  public RangeBitmapIndex(RangeBitmap rangeBitmap, int serializedSize, int slices) {
    this.rangeBitmap = rangeBitmap;
    this.serializedSize = serializedSize;
    this.slices = slices;
  }

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    return rangeBitmap.lte(max);
  }

  @Override
  public int bitmapCount() {
    return slices;
  }

  @Override
  public long serializedSizeInBytes() {
    return serializedSize;
  }

  private static long rangeMaxForLimit(long maxValue) {
    int lz = Long.numberOfLeadingZeros(maxValue);
    return lz <= 8 ? -1L : (1L << (64 - lz + 7 & -8)) - 1L;
  }
}
