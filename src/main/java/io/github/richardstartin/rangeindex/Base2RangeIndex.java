package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class Base2RangeIndex implements RangeIndex {

  private final RoaringBitmap[] bitmaps;
  private final long maxRid;

  public static Accumulator<Base2RangeIndex> accumulator() {
    return new Base2RangeIndexBuilder();
  }

  public Base2RangeIndex(RoaringBitmap[] bitmaps, long maxRid) {
    this.bitmaps = bitmaps;
    this.maxRid = maxRid;
  }

  public RoaringBitmap lessThanOrEqual(long max) {
    RoaringBitmap bitmap = (max & 1) == 0 ? bitmaps[0].clone() : range(maxRid);
    long mask = 2;
    for (int i = 1; i < bitmaps.length; ++i) {
      if ((max & mask) != mask) {
        bitmap.and(bitmaps[i]);
      } else {
        bitmap.or(bitmaps[i]);
      }
      mask <<=  1;
    }
    return bitmap;
  }

  @Override
  public int bitmapCount() {
    int count = 0;
    for (RoaringBitmap bitmap : bitmaps) {
      count += bitmap == null ? 0 : 1;
    }
    return count;
  }

  @Override
  public long serializedSizeInBytes() {
    return RangeIndex.serializedSizeInBytes(bitmaps);
  }

  private static class Base2RangeIndexBuilder implements Accumulator<Base2RangeIndex> {

    private final RoaringBitmapWriter<RoaringBitmap>[] writers;
    private int rid = 0;

    @SuppressWarnings("unchecked")
    public Base2RangeIndexBuilder() {
      writers = new RoaringBitmapWriter[64];
      Arrays.setAll(writers, i -> RoaringBitmapWriter.writer().runCompress(true).get());
    }

    public void add(long value) {
      long bits = ~value;
      while (bits != 0) {
        int index = Long.numberOfTrailingZeros(bits);
        writers[index].add(rid);
        bits &= (bits - 1);
      }
      rid++;
    }

    public Base2RangeIndex seal() {
      if (writers == null) {
        return new Base2RangeIndex(new RoaringBitmap[0], 0);
      }
      RoaringBitmap[] bitmaps = new RoaringBitmap[writers.length];
      for (int i = 0; i < writers.length; ++i) {
         bitmaps[i] = writers[i].get();
      }
      return new Base2RangeIndex(bitmaps, rid);
    }
  }
}
