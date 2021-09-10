package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class GenericBaseRangeIndex implements RangeIndex {

  public static Accumulator<GenericBaseRangeIndex> accumulator(int base) {
    return new Builder(base);
  }

  private final int base;
  private final int sliceCount;
  private final long max;
  private final RoaringBitmap[] bitmaps;

  public GenericBaseRangeIndex(int base, int sliceCount, long max, RoaringBitmap[] bitmaps) {
    this.base = base;
    this.sliceCount = sliceCount;
    this.max = max;
    this.bitmaps = bitmaps;
  }

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    int digit = (int) (max % base);
    RoaringBitmap bitmap = digit < base - 1 ? bitmaps[digit].clone() : range(this.max);
    for (int i = 1; i < sliceCount; ++i) {
      max /= base;
      digit = (int) (max % base);
      if (digit < base - 1) {
        bitmap.and(bitmaps[i * (base - 1) + digit]);
      }
      if (digit != 0) {
        bitmap.or(bitmaps[i * (base - 1) + digit - 1]);
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
    for (RoaringBitmap bitmap : bitmaps) {
      count += bitmap == null ? 0 : 1;
    }
    return count;
  }

  private static final class Builder implements Accumulator<GenericBaseRangeIndex> {

    private final int base;
    private final int sliceCount;
    private int rid;

    private final RoaringBitmapWriter<RoaringBitmap>[] writers;

    @SuppressWarnings("unchecked")
    private Builder(int base) {
      this.base = base;
      this.sliceCount = sliceCount(base);
      this.writers = new RoaringBitmapWriter[sliceCount * (base - 1)];
      Arrays.setAll(writers, i -> RoaringBitmapWriter.writer().runCompress(true).get());
    }

    @Override
    public void add(long value) {
      for (int i = 0; i < sliceCount; ++i) {
        int digit = (int)(value % base);
        // mark every bitmap greater than or equal to the digit
        for (int j = digit; j < base - 1; ++j) {
          writers[i * (base - 1) + j].add(rid);
        }
        value /= base;
      }
      ++rid;
    }

    @Override
    public GenericBaseRangeIndex seal() {
      RoaringBitmap[] bitmaps = new RoaringBitmap[writers.length];
      for (int i = 0; i < bitmaps.length; ++i) {
        bitmaps[i] = writers[i].get();
      }
      return new GenericBaseRangeIndex(base, sliceCount, rid, bitmaps);
    }

    private static int sliceCount(int base) {
      return base == 10 ? (int)Math.log10(Long.MAX_VALUE) : (int)(Math.log(Long.MAX_VALUE)/(Math.log(base)));
    }
  }
}
