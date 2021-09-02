package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class Base4RangeIndex implements RangeIndex {

  public Base4RangeIndex(long max, RoaringBitmap[] bitmaps) {
    this.max = max;
    this.bitmaps = bitmaps;
  }

  public static Accumulator<Base4RangeIndex> accumulator() {
    return new Builder();
  }

  private final long max;
  private final RoaringBitmap[] bitmaps;

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    int digit = (int) max & 3;
    RoaringBitmap bitmap = digit < 3 ? bitmaps[digit].clone() : range(this.max);
    for (int i = 1; i < 32; ++i) {
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


  @SuppressWarnings("unchecked")
  private static final class Builder implements Accumulator<Base4RangeIndex> {

    private final RoaringBitmapWriter<RoaringBitmap>[] writers;
    private int rid;

    public Builder() {
      this.writers = new RoaringBitmapWriter[3 * 32];
      Arrays.setAll(writers, i -> RoaringBitmapWriter.writer().runCompress(true).get());
    }

    @Override
    public void add(long value) {
      for (int i = 0; i < 32; ++i) {
        int digit = (int)value & 3;
        // mark every bitmap greater than or equal to the digit
        for (int j = digit; j < 3; ++j) {
          writers[i * 3 + j].add(rid);
        }
        value >>>= 2;
      }
      ++rid;
    }

    @Override
    public Base4RangeIndex seal() {
      RoaringBitmap[] bitmaps = new RoaringBitmap[writers.length];
      for (int i = 0; i < bitmaps.length; ++i) {
        bitmaps[i] = writers[i].get();
      }
      return new Base4RangeIndex(rid, bitmaps);
    }
  }
}
