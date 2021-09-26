package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class TrimmedBase2RangeIndex implements RangeIndex<RoaringBitmap> {

  public static Accumulator<TrimmedBase2RangeIndex> accumulator() {
    return new Builder();
  }

  TrimmedBase2RangeIndex(long maxRid, RoaringBitmap[] bitmaps) {
    this.maxRid = maxRid;
    this.bitmaps = bitmaps;
  }

  private final long maxRid;
  private final RoaringBitmap[] bitmaps;

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    RoaringBitmap bitmap = (max & 1) == 0 ? bitmaps[0].clone() : RoaringBitmap.bitmapOfRange(0, maxRid);
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

  private static final class Builder implements Accumulator<TrimmedBase2RangeIndex> {

    private final RoaringBitmapWriter<RoaringBitmap>[] writers;
    private int rid = 0;
    private long mask = 0;

    @SuppressWarnings("unchecked")
    public Builder() {
      writers = new RoaringBitmapWriter[64];
      Arrays.setAll(writers, i -> RoaringBitmapWriter.writer().runCompress(true).get());
    }

    public void add(long value) {
      long bits = ~value;
      mask |= value;
      while (bits != 0) {
        int index = Long.numberOfTrailingZeros(bits);
        writers[index].add(rid);
        bits &= (bits - 1);
      }
      rid++;
    }

    public TrimmedBase2RangeIndex seal() {
      if (writers == null) {
        return new TrimmedBase2RangeIndex(rid, new RoaringBitmap[0]);
      }
      int numDiscarded = Long.numberOfLeadingZeros(mask);
      RoaringBitmap[] bitmaps = new RoaringBitmap[writers.length - numDiscarded];
      for (int i = 0; i < bitmaps.length; ++i) {
        bitmaps[i] = writers[i].get();
      }
      return new TrimmedBase2RangeIndex(rid, bitmaps);
    }
  }
}
