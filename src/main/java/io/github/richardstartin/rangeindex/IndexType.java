package io.github.richardstartin.rangeindex;

public enum IndexType {
  SPECIALIZED {
    @Override
    public Accumulator<? extends RangeIndex> accumulator(int base) {
      switch (base) {
        case 2:
          return Base2RangeIndex.accumulator();
        case 4:
          return Base4RangeIndex.accumulator();
        case 10:
          return Base10RangeIndex.accumulator();
        case 16:
          return Base16RangeIndex.accumulator();
        default:
          throw new IllegalStateException();
      }
    }
  },
  UNCOMPRESSED {
    @Override
    public Accumulator<? extends RangeIndex> accumulator(int base) {
      switch (base) {
        case 2:
          return UncompressedBase2RangeIndex.accumulator();
        case 4:
          return UncompressedBase4RangeIndex.accumulator();
        case 10:
          return UncompressedBase10RangeIndex.accumulator();
        case 16:
          return UncompressedBase16RangeIndex.accumulator();
        default:
          throw new IllegalStateException();
      }
    }
  },
  GENERIC {
    @Override
    public Accumulator<? extends RangeIndex> accumulator(int base) {
      return GenericBaseRangeIndex.accumulator(base);
    }
  },
  TRIMMED {
    @Override
    public Accumulator<? extends RangeIndex> accumulator(int base) {
      switch (base) {
        case 2:
          return TrimmedBase2RangeIndex.accumulator();
        case 4:
          return TrimmedBase4RangeIndex.accumulator();
        case 10:
          return TrimmedBase10RangeIndex.accumulator();
        case 16:
          return TrimmedBase16RangeIndex.accumulator();
        default:
          throw new IllegalStateException();
      }
    }
  },
  RELATIVE {
    @Override
    public Accumulator<? extends RangeIndex> accumulator(int base) {
      switch (base) {
        case 2:
          return TrimmedBase2RangeIndex.accumulator();
        case 4:
          return TrimmedBase4RangeIndex.accumulator();
        case 10:
          return TrimmedBase10RangeIndex.accumulator();
        case 16:
          return TrimmedBase16RangeIndex.accumulator();
        default:
          throw new IllegalStateException();
      }
    }
  },
  BUCKETED {
    @Override
    public Accumulator<? extends RangeIndex> accumulator(int base) {
      return BucketedRangeIndex.accumulator();
    }
  },
  BUCKETED_RANGE_SCAN {
    @Override
    public Accumulator<? extends RangeIndex> accumulator(int base) {
      return BucketedRangeScan.accumulator();
    }
  },
  SCAN {
    @Override
    public Accumulator<? extends RangeIndex> accumulator(int base) {
      return Scan.accumulator();
    }
  };
  public abstract Accumulator<? extends RangeIndex> accumulator(int base);
}
