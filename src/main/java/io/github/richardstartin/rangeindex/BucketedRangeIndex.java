package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class BucketedRangeIndex implements RangeIndex<RoaringBitmap> {


  public static Accumulator<BucketedRangeIndex> accumulator() {
    return new Builder();
  }

  private final int bucketSize;
  private final long[] buckets;
  private final RoaringBitmap[] bitmaps;

  public BucketedRangeIndex(int bucketSize, long[] buckets, RoaringBitmap[] bitmaps) {
    this.bucketSize = bucketSize;
    this.buckets = buckets;
    this.bitmaps = bitmaps;
  }

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    RoaringBitmap bitmap = new RoaringBitmap();
    int bucket = Arrays.binarySearch(buckets, max);
    int last = bucket < 0 ? -bucket : bucket;
    for (int i = 0; i <= last && i < bitmaps.length; ++i) {
      bitmap.or(bitmaps[i]);
    }
    return bitmap;
  }

  @Override
  public int bitmapCount() {
    return bitmaps.length;
  }

  @Override
  public long serializedSizeInBytes() {
    return RangeIndex.serializedSizeInBytes(bitmaps);
  }

  @SuppressWarnings("unchecked")
  private static final class Builder implements Accumulator<BucketedRangeIndex> {

    private int rid = 0;
    private int bucketCount = 0;
    private long[] buckets = new long[16];
    private RoaringBitmapWriter<RoaringBitmap>[] writers = new RoaringBitmapWriter[16];

    @Override
    public void add(long value) {
      value = Math.floorDiv(value, 20);
      int bucket = Arrays.binarySearch(buckets, 0, bucketCount, value);
      if (bucket < 0) {
        bucket = -(bucket + 1);
        if (bucketCount - bucket > 0) {
          System.arraycopy(buckets, bucket, buckets, bucket + 1, bucketCount - bucket);
          System.arraycopy(writers, bucket, writers, bucket + 1, bucketCount - bucket);
        }
        buckets[bucket] = value;
        writers[bucket] = RoaringBitmapWriter.writer().runCompress(true).get();
        bucketCount++;
        if (bucketCount == buckets.length) {
          buckets = Arrays.copyOf(buckets, buckets.length * 2);
          writers = Arrays.copyOf(writers, buckets.length * 2);
        }
      }
      writers[bucket].add(rid++);
    }

    @Override
    public BucketedRangeIndex seal() {
      RoaringBitmap[] bitmaps = new RoaringBitmap[bucketCount];
      buckets = Arrays.copyOf(buckets, bucketCount);
      for (int i = 0; i < bucketCount; ++i) {
        buckets[i] *= 20;
        bitmaps[i] = writers[i].get();
      }
      return new BucketedRangeIndex(20, buckets, bitmaps);
    }
  }


}
