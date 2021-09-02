package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class Base10RangeIndex implements RangeIndex {

  private static final int SLICE_COUNT = (int)Math.log10(Long.MAX_VALUE);

  public Base10RangeIndex(long max, RoaringBitmap[] bitmaps) {
    this.max = max;
    this.bitmaps = bitmaps;
  }

  public static Accumulator<Base10RangeIndex> accumulator() {
    return new Builder();
  }

  private final long max;
  private final RoaringBitmap[] bitmaps;

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    int digit = (int) (max % 10);
    RoaringBitmap bitmap = digit < 9 ? bitmaps[digit].clone() : range(this.max);
    for (int i = 1; i < SLICE_COUNT; ++i) {
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

  private static final class Builder implements Accumulator<Base10RangeIndex> {
    private int rid;

    private final RoaringBitmapWriter<RoaringBitmap>[] writers;

    @SuppressWarnings("unchecked")
    private Builder() {
      this.writers = new RoaringBitmapWriter[SLICE_COUNT * 9];
      Arrays.setAll(writers, i -> RoaringBitmapWriter.writer().runCompress(true).get());
    }

    @Override
    public void add(long value) {
      for (int i = 0; i < SLICE_COUNT; ++i) {
        int digit = (int)(value % 10);
        // mark every bitmap greater than or equal to the digit
        for (int j = digit; j < 9; ++j) {
          writers[i * 9 + j].add(rid);
        }
        value /= 10;
      }
      ++rid;
    }

    @Override
    public Base10RangeIndex seal() {
      RoaringBitmap[] bitmaps = new RoaringBitmap[writers.length];
      for (int i = 0; i < bitmaps.length; ++i) {
        bitmaps[i] = writers[i].get();
      }
      return new Base10RangeIndex(rid, bitmaps);
    }
  }
}
