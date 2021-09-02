package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class TrimmedBase4RangeIndex implements RangeIndex {

  public TrimmedBase4RangeIndex(long max, int slices, RoaringBitmap[] bitmaps) {
    this.max = max;
    this.slices = slices;
    this.bitmaps = bitmaps;
  }

  public static Accumulator<TrimmedBase4RangeIndex> accumulator() {
    return new Builder();
  }

  private final long max;
  private final int slices;
  private final RoaringBitmap[] bitmaps;

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    int digit = (int) max & 3;
    RoaringBitmap bitmap = digit < 3 ? bitmaps[digit].clone() : range(this.max);
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


  @SuppressWarnings("unchecked")
  private static final class Builder implements Accumulator<TrimmedBase4RangeIndex> {

    private final RoaringBitmapWriter<RoaringBitmap>[] writers;
    private int rid;
    private long mask;

    public Builder() {
      this.writers = new RoaringBitmapWriter[3 * 32];
      Arrays.setAll(writers, i -> RoaringBitmapWriter.writer().runCompress(true).get());
    }

    @Override
    public void add(long value) {
      mask |= value;
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
    public TrimmedBase4RangeIndex seal() {
      int discard = Long.numberOfLeadingZeros(mask) >>> 1;
      RoaringBitmap[] bitmaps = new RoaringBitmap[writers.length - 3 * discard];
      for (int i = 0; i < bitmaps.length; ++i) {
        bitmaps[i] = writers[i].get();
      }
      return new TrimmedBase4RangeIndex(rid, 32 - discard, bitmaps);
    }
  }
}
