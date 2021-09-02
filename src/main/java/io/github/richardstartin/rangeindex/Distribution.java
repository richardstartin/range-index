package io.github.richardstartin.rangeindex;

import java.util.Arrays;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.function.LongSupplier;

public enum Distribution {
  UNIFORM {
    LongSupplier of(long seed, double... params) {
      long min = (long)params[0];
      long max = (long)params[1];
      SplittableRandom random = new SplittableRandom(seed);
      return () -> random.nextLong(min, max);
    }
  },
  NORMAL {
    LongSupplier of(long seed, double... params) {
      double mean = params[0];
      double stddev = params[1];
      Random random = new Random(seed);
      return () -> (long)(stddev * random.nextGaussian() + mean);
    }
  },
  EXP {
    LongSupplier of(long seed, double... params) {
      double rate = params[0];
      SplittableRandom random = new SplittableRandom(seed);
      return () -> (long)-(Math.log(random.nextDouble()) / rate);
    }
  };
  abstract LongSupplier of(long seed, double... params);
  public static LongSupplier parse(long seed, String spec) {
    int paramsStart = spec.indexOf('(');
    int paramsEnd = spec.indexOf(')');
    double[] params = Arrays.stream(spec.substring(paramsStart + 1, paramsEnd).split(","))
            .mapToDouble(s -> Double.parseDouble(s.trim()))
            .toArray();
    String dist = spec.substring(0, paramsStart).toUpperCase();
    return Distribution.valueOf(dist).of(seed, params);
  }
}
