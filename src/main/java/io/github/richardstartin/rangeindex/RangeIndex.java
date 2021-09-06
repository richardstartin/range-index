package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;

public interface RangeIndex {

  RoaringBitmap lessThanOrEqual(long max);

  default RoaringBitmap between(long min, long max) {
    RoaringBitmap all = lessThanOrEqual(max);
    RoaringBitmap tooSmall = lessThanOrEqual(min - 1);
    all.andNot(tooSmall);
    return all;
  }

  default RoaringBitmap range(long max) {
    RoaringBitmap bitmap = new RoaringBitmap();
    bitmap.add(0L, max);
    return bitmap;
  }

  int bitmapCount();

  long serializedSizeInBytes();

  static long serializedSizeInBytes(RoaringBitmap[] bitmaps) {
    long size = 0;
    for (RoaringBitmap bitmap : bitmaps) {
      if (null != bitmap) {
        size += bitmap.serializedSizeInBytes();
      }
    }
    return size;
  }
}
