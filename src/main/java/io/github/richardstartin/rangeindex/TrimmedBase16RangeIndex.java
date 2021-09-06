package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class TrimmedBase16RangeIndex implements RangeIndex {

  public TrimmedBase16RangeIndex(long max, int slices, RoaringBitmap[] bitmaps) {
    this.max = max;
    this.slices = slices;
    this.bitmaps = bitmaps;
  }

  public static Accumulator<TrimmedBase16RangeIndex> accumulator() {
    return new Builder();
  }

  private final long max;
  private final int slices;
  private final RoaringBitmap[] bitmaps;

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    int digit = (int) max & 15;
    RoaringBitmap bitmap = digit < 15 ? bitmaps[digit].clone() : range(this.max);
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
    for (RoaringBitmap bitmap : bitmaps) {
      count += bitmap == null ? 0 : 1;
    }
    return count;
  }

  @SuppressWarnings("unchecked")
  private static final class Builder implements Accumulator<TrimmedBase16RangeIndex> {

    private final RoaringBitmapWriter<RoaringBitmap>[] writers;
    private int rid;
    private long mask;

    public Builder() {
      this.writers = new RoaringBitmapWriter[15 * 16];
      Arrays.setAll(writers, i -> RoaringBitmapWriter.writer().runCompress(true).get());
    }

    @Override
    public void add(long value) {
      mask |= value;
      for (int i = 0; i < 16; ++i) {
        int digit = (int)value & 15;
        // mark every bitmap greater than or equal to the digit
        for (int j = digit; j < 15; ++j) {
          writers[i * 15 + j].add(rid);
        }
        value >>>= 4;
      }
      ++rid;
    }

    @Override
    public TrimmedBase16RangeIndex seal() {
      int discard = Long.numberOfLeadingZeros(mask) >>> 2;
      RoaringBitmap[] bitmaps = new RoaringBitmap[writers.length - 3 * discard];
      for (int i = 0; i < bitmaps.length; ++i) {
        bitmaps[i] = writers[i].get();
      }
      return new TrimmedBase16RangeIndex(rid, 16 - discard, bitmaps);
    }
  }
}
