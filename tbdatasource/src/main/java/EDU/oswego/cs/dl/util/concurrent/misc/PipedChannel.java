
package EDU.oswego.cs.dl.util.concurrent.misc;
import EDU.oswego.cs.dl.util.concurrent.*;

import java.io.*;

/**
 * A channel based on a java.io.PipedInputStream and
 * java.io.PipedOutputStream. Elements are serialized
 * using ObjectInputStreams and ObjectOutputStreams
 * upon insertion and extraction from the pipe.
 * <p>
 * IO Exceptions are transformed into Errors. This is
 * in general not a good idea, but seems to be the most
 * reasonable compromise for the intended usage contexts.
 * <p>
 * <b>Status</b> Uncertain. There are enough 
 * conceptual and implementation snags surrounding use
 * of pipes as Channels to downplay use. However,
 * without such bridges, people would have to
 * duplicate code that should work the same way in both cases.
 *
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 **/

public class PipedChannel extends SemaphoreControlledChannel {
  protected ObjectInputStream in_;
  protected ObjectOutputStream out_;

  protected final PipedOutputStream outp_;
  protected final PipedInputStream inp_;


  public PipedChannel() {
    super(1);

    try {
      outp_ = new PipedOutputStream();
      inp_ = new PipedInputStream();
      inp_.connect(outp_);
    }
    catch (IOException ex) {
      ex.printStackTrace();
      throw new Error("Cannot construct Pipe?");
    }
  }


  /**
   * Return input stream, first constructing if necessary.
   * Needed because Object streams can block on open.
   **/

  protected synchronized ObjectInputStream in() {
    try {
      if (in_ == null) in_ = new ObjectInputStream(inp_);
      return in_;
    }
    catch (IOException ex) { 
      ex.printStackTrace();
      throw new Error("IO exception during open");
    }
  }

  /**
   * Return output stream, first constructing if necessary.
   * Needed because Object streams can block on open.
   **/
  protected synchronized ObjectOutputStream out() {
    try {
      if (out_ == null)  out_ = new ObjectOutputStream(outp_);
      return out_;
    }
    catch (IOException ex) { 
      ex.printStackTrace();
      throw new Error("IO exception during open");
    }
  }


  /** Shared mechanics for put-based methods **/
  protected void insert(Object x) {
    try {
      out().writeObject(x);
    }
    catch (InterruptedIOException ex) {
      Thread.currentThread().interrupt();
    }
    catch (IOException ex) {
      ex.printStackTrace();
      throw new Error("IO exception during put");
    }
  }

  /** Shared mechanics for take-based methods **/
  protected Object extract() {
    try {
      return in().readObject();
    }
    catch (InterruptedIOException ex) {
      Thread.currentThread().interrupt();
      return null;
    }
    catch (IOException ex) {
      ex.printStackTrace();
      throw new Error("IO exception during take");
    }
    catch (ClassNotFoundException ex) {
      ex.printStackTrace();
      throw new Error("Serialization exception during take");
    }
  }

  /** Stubbed out for now **/
  public Object peek() { return null; }
}

