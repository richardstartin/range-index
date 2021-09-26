package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class TrimmedBase10RangeIndex implements RangeIndex<RoaringBitmap> {

  private static final int SLICE_COUNT = (int)Math.log10(Long.MAX_VALUE);

  public TrimmedBase10RangeIndex(long max, int sliceCount, RoaringBitmap[] bitmaps) {
    this.max = max;
    this.sliceCount = sliceCount;
    this.bitmaps = bitmaps;
  }

  public static Accumulator<TrimmedBase10RangeIndex> accumulator() {
    return new Builder();
  }

  private final long max;
  private final int sliceCount;
  private final RoaringBitmap[] bitmaps;

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    int digit = (int) (max % 10);
    RoaringBitmap bitmap = digit < 9 ? bitmaps[digit].clone() : RoaringBitmap.bitmapOfRange(0, this.max);
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
    for (RoaringBitmap bitmap : bitmaps) {
      count += bitmap == null ? 0 : 1;
    }
    return count;
  }

  private static final class Builder implements Accumulator<TrimmedBase10RangeIndex> {
    private int rid;
    private int maxLog;
    private final RoaringBitmapWriter<RoaringBitmap>[] writers;

    @SuppressWarnings("unchecked")
    private Builder() {
      this.writers = new RoaringBitmapWriter[SLICE_COUNT * 9];
      Arrays.setAll(writers, i -> RoaringBitmapWriter.writer().runCompress(true).get());
    }

    @Override
    public void add(long value) {
      // this can be sped up if necessary
      this.maxLog = Math.max(maxLog, (int)Math.log10(value));
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
    public TrimmedBase10RangeIndex seal() {
      RoaringBitmap[] bitmaps = new RoaringBitmap[9 * maxLog];
      for (int i = 0; i < bitmaps.length; ++i) {
        bitmaps[i] = writers[i].get();
      }
      return new TrimmedBase10RangeIndex(rid, maxLog, bitmaps);
    }
  }
}
