package org.clojurewerkz.titanium.pipes;

import clojure.lang.IFn;
import com.tinkerpop.pipes.PipeFunction;

/**
 * Wraps a Clojure function in a PipeFunction.
 *
 * @author Michael S. Klishin
 */

public class ClojurePipeFunction implements PipeFunction {
  private final IFn fn;

  public ClojurePipeFunction(final IFn fn) {
    this.fn = fn;
  }

  @Override
  public Object compute(Object o) {
    return fn.invoke(o);
  }
}
