
package EDU.oswego.cs.dl.util.concurrent.misc;
import  EDU.oswego.cs.dl.util.concurrent.*;


public class CVBuffer implements BoundedChannel {
  private final Mutex mutex;
  private final CondVar notFull;
  private final CondVar notEmpty;
  private int count = 0;
  private int takePtr = 0;     
  private int putPtr = 0;
  private final Object[] array;

  public CVBuffer(int cap) { 
    array = new Object[cap];
    mutex = new Mutex();
    notFull = new CondVar(mutex);
    notEmpty = new CondVar(mutex);
  }

  public CVBuffer() { 
    this(DefaultChannelCapacity.get()); 
  }

  public int capacity() { return array.length; }

  public void put(Object x) throws InterruptedException {
    mutex.acquire();
    try {
      while (count == array.length) {
        notFull.await();
      }
      array[putPtr] = x;
      putPtr = (putPtr + 1) % array.length;
      ++count;
      notEmpty.signal();
    }
    finally {
      mutex.release();
    }
  }

  public Object take() throws InterruptedException {
    Object x = null;
    mutex.acquire();
    try {
      while (count == 0) {
        notEmpty.await();
      }
      x = array[takePtr];
      array[takePtr] = null;
      takePtr = (takePtr + 1) % array.length;
      --count;
      notFull.signal();
    }
    finally {
      mutex.release();
    }
    return x;
  }
    
  public boolean offer(Object x, long msecs) throws InterruptedException {
    mutex.acquire();
    try {
      if (count == array.length) {
        notFull.timedwait(msecs);
        if (count == array.length)
          return false;
      }
      array[putPtr] = x;
      putPtr = (putPtr + 1) % array.length;
      ++count;
      notEmpty.signal();
      return true;
    }
    finally {
      mutex.release();
    }
  }
  
  public Object poll(long msecs) throws InterruptedException {
    Object x = null;
    mutex.acquire();
    try {
      if (count == 0) {
        notEmpty.timedwait(msecs);
        if (count == 0)
          return null;
      }
      x = array[takePtr];
      array[takePtr] = null;
      takePtr = (takePtr + 1) % array.length;
      --count;
      notFull.signal();
    }
    finally {
      mutex.release();
    }
    return x;
  }

  public Object peek() {
    try {
      mutex.acquire();
      try {
        if (count == 0) 
          return null;
        else
          return array[takePtr];
      }
      finally {
        mutex.release();
      }
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return null;
    }
  }

}

