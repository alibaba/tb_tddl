/*
  File: SwingWorker.java

  Originally written by Joseph Bowbeer and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
 
  Originally part of jozart.swingutils.
  Adapted for util.concurrent by Joseph Bowbeer.

*/

package EDU.oswego.cs.dl.util.concurrent.misc;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

import EDU.oswego.cs.dl.util.concurrent.*;

/**
 * An abstract class that you subclass to perform GUI-related work
 * in a dedicated thread.
 * <p>
 * This class was adapted from the SwingWorker written by Hans Muller
 * and presented in "Using a Swing Worker Thread" in the Swing Connection
 * - http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html
 * <p>
 * A closely related version of this class is described in
 * "The Last Word in Swing Threads" in the Swing Connection
 * - http://java.sun.com/products/jfc/tsc/articles/threads/threads3.html
 * <p>
 * This SwingWorker is a ThreadFactoryUser and implements Runnable. The
 * default thread factory creates low-priority worker threads. A special
 * constructor is provided for enabling a timeout. When the timeout
 * expires, the worker thread is interrupted.
 * <p>
 * Note: Using a timeout of <code>Long.MAX_VALUE</code> will not impose a
 * timeout but will create an additional thread of control that will respond
 * to an interrupt even if the <code>construct</code> implementation ignores
 * them.
 * <p>
 * <b>Sample Usage</b> <p>
 * <pre>
 * import EDU.oswego.cs.dl.util.concurrent.TimeoutException;
 * import EDU.oswego.cs.dl.util.concurrent.misc.SwingWorker;
 *
 * public class SwingWorkerDemo extends javax.swing.JApplet {
 *
 *   private static final int TIMEOUT = 5000; // 5 seconds
 *   private javax.swing.JLabel status;
 *   private javax.swing.JButton start;
 *   private SwingWorker worker;
 *
 *   public SwingWorkerDemo() {
 *     status = new javax.swing.JLabel("Ready");
 *     status.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 *     getContentPane().add(status, java.awt.BorderLayout.CENTER);
 *     start = new javax.swing.JButton("Start");
 *     getContentPane().add(start, java.awt.BorderLayout.SOUTH);
 *
 *     start.addActionListener(new java.awt.event.ActionListener() {
 *       public void actionPerformed(java.awt.event.ActionEvent evt) {
 *         if (start.getText().equals("Start")) {
 *           start.setText("Stop");
 *           status.setText("Working...");
 *           worker = new DemoSwingWorker(TIMEOUT);
 *           worker.start();
 *         } else {
 *           worker.interrupt();
 *         }
 *       }
 *     });
 *   }
 *
 *   private class DemoSwingWorker extends SwingWorker {
 *     private static final java.util.Random RAND = new java.util.Random();
 *     public DemoSwingWorker(long msecs) {
 *       super(msecs);
 *     }
 *     protected Object construct() throws InterruptedException {
 *       // Take a random nap. If we oversleep, the worker times out.
 *       Thread.sleep(RAND.nextInt(2*TIMEOUT));
 *       return "Success";
 *     }
 *     protected void finished() {
 *       start.setText("Start");
 *       try {
 *         Object result = get();
 *         status.setText((String) result);
 *       }
 *       catch (java.lang.reflect.InvocationTargetException e) {
 *         Throwable ex = e.getTargetException();
 *         if (ex instanceof TimeoutException) {
 *           status.setText("Timed out.");
 *         } else if (ex instanceof InterruptedException) {
 *           status.setText("Interrupted.");
 *         } else {
 *           status.setText("Exception: " + ex);
 *         }
 *       }
 *       catch (InterruptedException ex) {
 *         // event-dispatch thread won't be interrupted 
 *         throw new IllegalStateException(ex+"");
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * @author  Joseph Bowbeer
 * @author  Hans Muller
 * @version 3.0
 *
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 */
public abstract class SwingWorker extends ThreadFactoryUser
implements Runnable {

    /** Default thread factory. Creates low priority worker threads. */
    private static final ThreadFactory FACTORY = new ThreadFactory() {
        public Thread newThread(Runnable command) {
            Thread t = new Thread(command);
            t.setPriority(Thread.MIN_PRIORITY+1);
            return t;
        }
    };

    /** Holds the value to be returned by the <code>get</code> method. */
    private final FutureResult result = new FutureResult();

    /** Maximum time to wait for worker to complete. */
    private final long timeout;
    
    /** Worker thread. */
    private Thread thread;

    /** Creates new SwingWorker with no timeout. */
    public SwingWorker() {
        this(FACTORY, 0);
    }
    
    /**
     * Creates new SwingWorker with specified timeout.
     * @param msecs timeout in milliseconds, or <code>0</code>
     * for no time limit.
     */
    public SwingWorker(long msecs) {
        this(FACTORY, msecs);
    }
    
    /**
     * Creates new SwingWorker with specified thread factory and timeout.
     * @param factory factory for worker threads.
     * @param msecs timeout in milliseconds, or <code>0</code>
     * for no time limit.
     */
    protected SwingWorker(ThreadFactory factory, long msecs) {
        setThreadFactory(factory);
        if (msecs < 0) {
            throw new IllegalArgumentException("timeout="+msecs);
        }
        timeout = msecs;
    }

    /**
     * Computes the value to be returned by the <code>get</code> method.
     */
    protected abstract Object construct() throws Exception;

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    protected void finished() { }

    /**
     * Returns timeout period in milliseconds. Timeout is the
     * maximum time to wait for worker to complete. There is
     * no time limit if timeout is <code>0</code> (default).
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Calls the <code>construct</code> method to compute the result,
     * and then invokes the <code>finished</code> method on the event
     * dispatch thread.
     */
    public void run() {

        Callable function = new Callable() {
            public Object call() throws Exception {
                return construct();
            }
        };

        Runnable doFinished = new Runnable() {
            public void run() {
                finished();
            }
        };

        /* Convert to TimedCallable if timeout is specified. */
        long msecs = getTimeout();
        if (msecs != 0) {
            TimedCallable tc = new TimedCallable(function, msecs);
            tc.setThreadFactory(getThreadFactory());
            function = tc;
        }

        result.setter(function).run();
        SwingUtilities.invokeLater(doFinished);
    }

    /**
     * Starts the worker thread.
     */
    public synchronized void start() {
        if (thread == null) {
            thread = getThreadFactory().newThread(this);
        }
        thread.start();
    }

    /**
     * Stops the worker and sets the exception to InterruptedException.
     */
    public synchronized void interrupt() {
        if (thread != null) {
            /* Try-catch is workaround for JDK1.2 applet security bug.
               On some platforms, a security exception is thrown if an
               applet interrupts a thread that is no longer alive. */
            try { thread.interrupt(); } catch (Exception ex) { }
        }
        result.setException(new InterruptedException());
    }

    /**
     * Return the value created by the <code>construct</code> method,
     * waiting if necessary until it is ready.
     *
     * @return the value created by the <code>construct</code> method
     * @exception InterruptedException if current thread was interrupted
     * @exception InvocationTargetException if the constructing thread
     * encountered an exception or was interrupted.
     */
    public Object get()
    throws InterruptedException, InvocationTargetException {
        return result.get();
    }

    /**
     * Wait at most msecs to access the constructed result.
     * @return current value
     * @exception TimeoutException if not ready after msecs
     * @exception InterruptedException if current thread has been interrupted
     * @exception InvocationTargetException if the constructing thread
     * encountered an exception or was interrupted.
     */
    public Object timedGet(long msecs) 
    throws TimeoutException, InterruptedException, InvocationTargetException {
        return result.timedGet(msecs);
    }

    /**
     * Get the exception, or null if there isn't one (yet).
     * This does not wait until the worker is ready, so should
     * ordinarily only be called if you know it is.
     * @return the exception encountered by the <code>construct</code>
     * method wrapped in an InvocationTargetException
     */
    public InvocationTargetException getException() {
        return result.getException();
    }

    /**
     * Return whether the <code>get</code> method is ready to
     * return a value.
     *
     * @return true if a value or exception has been set. else false
     */
    public boolean isReady() {
        return result.isReady();
    }

}
