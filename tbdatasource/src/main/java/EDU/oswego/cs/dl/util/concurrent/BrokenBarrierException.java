/*
  File: BrokenBarrierException.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  29Jun1998  dl               Create public version
*/

package EDU.oswego.cs.dl.util.concurrent;

/**
 * Thrown by Barrier upon interruption of participant threads
 **/

public class BrokenBarrierException extends RuntimeException {

  /** 
   * The index that barrier would have returned upon
   * normal return;
   **/

  public final int index;
  /**
   * Constructs a BrokenBarrierException with given index
   **/
  public BrokenBarrierException(int idx) {
    index = idx;
  }

  /**
     * Constructs a BrokenBarrierException with the
     * specified index and detail message.
     */
  public BrokenBarrierException(int idx, String message) {
    super(message);
    index = idx;
  }
}
