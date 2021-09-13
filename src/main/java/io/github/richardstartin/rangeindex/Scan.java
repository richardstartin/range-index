package io.github.richardstartin.rangeindex;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.RoaringBitmapWriter;

import java.util.Arrays;

public class Scan implements RangeIndex {

  public static Accumulator<Scan> accumulator() {
    return new Accumulator<Scan>() {
      private int count = 0;
      private long[] data = new long[1024];
      @Override
      public void add(long value) {
        if (count == data.length) {
          data = Arrays.copyOf(data, data.length * 2);
        }
        data[count++] = value;
      }

      @Override
      public Scan seal() {
        return new Scan(Arrays.copyOf(data, count));
      }
    };
  }

  private final long[] data;

  public Scan(long[] data) {
    this.data = data;
  }

  @Override
  public RoaringBitmap lessThanOrEqual(long max) {
    RoaringBitmapWriter<RoaringBitmap> writer = RoaringBitmapWriter.writer().runCompress(true).get();
    for (int i = 0; i < data.length; ++i) {
      if (data[i] <= max) {
        writer.add(i);
      }
    }
    return writer.get();
  }

  @Override
  public int bitmapCount() {
    return 0;
  }

  @Override
  public long serializedSizeInBytes() {
    return 0;
  }
}
