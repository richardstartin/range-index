package io.github.richardstartin.rangeindex;

import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class BucketedRangeScan implements RangeIndex<RoaringBitmap> {

  public static Accumulator<BucketedRangeScan> accumulator() {
    return new Accumulator<>() {
      private int count = 0;
      private long min = Long.MAX_VALUE;
      private long max = Long.MIN_VALUE;
      private long[] data = new long[1024];

      @Override
      public void add(long value) {
        if (count == data.length) {
          data = Arrays.copyOf(data, data.length * 2);
        }
        data[count++] = value;
        min = Math.min(min, value);
        max = Math.max(max, value);
      }

      @Override
      public BucketedRangeScan seal() {
        long range = max - min;
        long[] buckets = new long[20];
        buckets[0] = min;
        for (int i = 1; i < buckets.length; ++i) {
          buckets[i] = buckets[i-1] + (range / 20);
        }
        RoaringBitmapWriter<RoaringBitmap>[] writers = new RoaringBitmapWriter[20];
        for (int i = 0; i < writers.length; ++i) {
          writers[i] = RoaringBitmapWriter.writer().runCompress(true).get();
        }
        for (int i = 0; i < count; ++i) {
          long value = data[i];
          for (int b = 0; b < buckets.length; ++b) {
            if (value < buckets[b]) {
              writers[b - 1].add(i);
              break;
            } else if (b == buckets.length - 1) {
              writers[b].add(i);
            }
          }
        }
        RoaringBitmap[] bitmaps = new RoaringBitmap[writers.length];
        for (int i = 0; i < bitmaps.length; ++i) {
          bitmaps[i] = writers[i].get();
        }
        return new BucketedRangeScan(buckets, bitmaps, Arrays.copyOf(data, count));
      }
    };
  }

  private final long[] buckets;
  private final RoaringBitmap[] bitmaps;
  private final long[] data;

  public BucketedRangeScan(long[] buckets, RoaringBitmap[] bitmaps, long[] data) {
    this.buckets = buckets;
    this.bitmaps = bitmaps;
    this.data = data;
  }

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    RoaringBitmap covered = new RoaringBitmap();
    if (max < buckets[0]) {
      return covered;
    }
    int end = 0;
    while (end < buckets.length && buckets[end] < max) {
      end++;
    }
    for (int i = 0; i < end - 1; ++i) {
      covered.or(bitmaps[i]);
    }
    if (end < buckets.length) {
      RoaringBitmapWriter<RoaringBitmap> writer = RoaringBitmapWriter.writer().runCompress(true).get();
      bitmaps[end == 0 ? 0 : end - 1].forEach((IntConsumer) i -> {
        if (data[i] <= max) {
          writer.add(i);
        }
      });
      covered.or(writer.get());
    } else {
      covered.or(bitmaps[end - 1]);
    }
    return covered;
  }

  @Override
  public int bitmapCount() {
    return bitmaps.length;
  }

  @Override
  public long serializedSizeInBytes() {
    return RangeIndex.serializedSizeInBytes(bitmaps);
  }
}
