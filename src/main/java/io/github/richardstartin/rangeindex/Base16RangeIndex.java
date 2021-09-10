package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class Base16RangeIndex implements RangeIndex {

  public Base16RangeIndex(long max, RoaringBitmap[] bitmaps) {
    this.max = max;
    this.bitmaps = bitmaps;
  }

  public static Accumulator<Base16RangeIndex> accumulator() {
    return new Builder();
  }

  private final long max;
  private final RoaringBitmap[] bitmaps;

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    int digit = (int) max & 0xF;
    RoaringBitmap bitmap = digit < 0xF ? bitmaps[digit].clone() : range(this.max);
    for (int i = 1; i < 16; ++i) {
      max >>>= 4;
      digit = (int) max & 0xF;
      if (digit < 0xF) {
        bitmap.and(bitmaps[i * 0xF + digit]);
      }
      if (digit != 0) {
        bitmap.or(bitmaps[i * 0xF + digit - 1]);
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

  @SuppressWarnings("unchecked")
  private static final class Builder implements Accumulator<Base16RangeIndex> {

    private final RoaringBitmapWriter<RoaringBitmap>[] writers;
    private int rid;

    public Builder() {
      this.writers = new RoaringBitmapWriter[0xF * 16];
      Arrays.setAll(writers, i -> RoaringBitmapWriter.writer().runCompress(true).get());
    }

    @Override
    public void add(long value) {
      for (int i = 0; i < 16; ++i) {
        int digit = (int)value & 0xF;
        // mark every bitmap greater than or equal to the digit
        for (int j = digit; j < 0xF; ++j) {
          writers[i * 0xF + j].add(rid);
        }
        value >>>= 4;
      }
      ++rid;
    }

    @Override
    public Base16RangeIndex seal() {
      RoaringBitmap[] bitmaps = new RoaringBitmap[writers.length];
      for (int i = 0; i < bitmaps.length; ++i) {
        bitmaps[i] = writers[i].get();
      }
      return new Base16RangeIndex(rid, bitmaps);
    }
  }
}
