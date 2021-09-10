package io.github.richardstartin.rangeindex;

import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
@AuxCounters(AuxCounters.Type.EVENTS)
public class IndexSizeCounter {

  long size;
  int count;

  public long size() {
    return size;
  }

  public int count() {
    return count;
  }

  @Setup(Level.Iteration)
  public void reset() {
    size = 0;
    count = 0;
  }
}
