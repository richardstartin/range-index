package io.github.richardstartin.rangeindex;

public enum IndexType {
  BASE_2 {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return Base2RangeIndex.accumulator();
    }
  },
  BASE_2_TRIMMED {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return TrimmedBase2RangeIndex.accumulator();
    }
  },
  BASE_2_GENERIC {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return GenericBaseRangeIndex.accumulator(2);
    }
  },
  BASE_4 {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return Base4RangeIndex.accumulator();
    }
  },
  BASE_4_TRIMMED {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return TrimmedBase4RangeIndex.accumulator();
    }
  },
  BASE_4_GENERIC {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return GenericBaseRangeIndex.accumulator(4);
    }
  },
  BASE_10 {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return Base10RangeIndex.accumulator();
    }
  },
  BASE_10_TRIMMED {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return TrimmedBase10RangeIndex.accumulator();
    }
  },
  BASE_10_GENERIC {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return GenericBaseRangeIndex.accumulator(10);
    }
  },
  BASE_16 {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return Base16RangeIndex.accumulator();
    }
  },
  BASE_16_TRIMMED {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return TrimmedBase16RangeIndex.accumulator();
    }
  },
  BASE_16_GENERIC {
    @Override
    public Accumulator<? extends RangeIndex> accumulator() {
      return GenericBaseRangeIndex.accumulator(16);
    }
  };

  public abstract Accumulator<? extends RangeIndex> accumulator();
}
