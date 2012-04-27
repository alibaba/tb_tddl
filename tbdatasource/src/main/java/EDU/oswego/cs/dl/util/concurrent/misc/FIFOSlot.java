package EDU.oswego.cs.dl.util.concurrent.misc;
import  EDU.oswego.cs.dl.util.concurrent.*;


// demo showing one way to make special channels

public class FIFOSlot implements BoundedChannel {
  private final Slot slot_;

  public FIFOSlot() {
    try {
      slot_ = new Slot(FIFOSemaphore.class);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw new Error("Cannot make Slot?");
    }
  }

  public void put(Object item) throws InterruptedException { 
    slot_.put(item); 
  }

  public boolean offer(Object item, long msecs) throws InterruptedException {
    return slot_.offer(item, msecs);
  }

  public Object take() throws InterruptedException { 
    return slot_.take(); 
  }

  public Object poll(long msecs) throws InterruptedException {
    return slot_.poll(msecs);
  }

  public int capacity() { return 1; }

  public Object peek() {
    return slot_.peek();
  }
}


