package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;

import java.util.BitSet;

public interface RangeIndex<T> {

  T lessThanOrEqual(long max);

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

  static long serializedSizeInBytes(BitSet[] bitmaps) {
    long size = 0;
    for (BitSet bitmap : bitmaps) {
      if (null != bitmap) {
        size += (bitmap.length() >>> 3);
      }
    }
    return size;
  }
}
