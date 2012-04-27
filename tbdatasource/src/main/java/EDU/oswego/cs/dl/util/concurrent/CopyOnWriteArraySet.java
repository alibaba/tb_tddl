/*
  File: CopyOnWriteArraySet.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  22Jun1998  dl               Create public version
*/

package EDU.oswego.cs.dl.util.concurrent;
import java.util.*;

/**
 * This class implements a java.util.Set that uses a
 * CopyOnWriteArrayList for all of its operations.
 * Thus, it shares the same basic properties:
 * <ul>
 *  <li> It is best suited for applications in which set sizes generally 
 *       stay small, read-only operations
 *       vastly outnumber mutative operations, and you need
 *       to prevent interference among threads during traversal.
 *  <li> Mutative operations(add, set, remove, etc) are fairly expensive
 *      since they usually entail copying the entire underlying array.
 *  <li> Loops involving repeated element-by-element mutative operations
 *      are so expensive that they should generally be avoided.
 *  <li> Iterators do not support the mutative remove operation
 *  <li> Traversal via iterators is very fast and cannot ever encounter
 *      interference from other threads. Iterators rely on
 *      unchanging snapshots of the array at the time the iterators were 
 *     constructed
 * </ul>
 * <p>
 * <b>Sample Usage.</b> Probably the main application 
 * of copy-on-write sets are classes that maintain
 * sets of Handler objects
 * that must be multicasted to upon an update command. This
 * is a classic case where you do not want to be holding a synch
 * lock while sending a message, and where traversals normally
 * vastly overwhelm additions.
 * <pre>
 * class  Handler { void handle(); ... }
 *
 * class X {
 *    private final CopyOnWriteArraySet handlers = new CopyOnWriteArraySet();
 *    public void addHandler(Handler h) { handlers.add(h); }
 *   
 *    private long internalState;
 *    private synchronized void changeState() { internalState = ...; }
 * 
 *    public void update() {
 *       changeState();
 *       Iterator it = handlers.iterator();
 *       while (it.hasNext())
 *          ((Handler)(it.next()).handle();
 *    }
 * }
 * </pre>    
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * @see CopyOnWriteArrayList
 **/


public class CopyOnWriteArraySet extends AbstractSet implements Cloneable, java.io.Serializable {

  protected final CopyOnWriteArrayList al;

  /**
   * Constructs an empty set
   */
  public CopyOnWriteArraySet() {
    al = new CopyOnWriteArrayList();
  }

  /**
   * Constructs a set containing all of the elements of the specified
   * Collection.
   */
  public CopyOnWriteArraySet(Collection c) { 
    al = new CopyOnWriteArrayList();
    al.addAllAbsent(c);
  }


  public int      size()                    { return al.size(); }
  public boolean  isEmpty()                 { return al.isEmpty(); }
  public boolean  contains(Object o)        { return al.contains(o); }
  public Object[] toArray()                 { return al.toArray(); }
  public Object[] toArray(Object[] a)       { return al.toArray(a); }
  public void     clear()                   {        al.clear(); }  
  public Iterator iterator()                { return al.iterator(); }
  public boolean  remove(Object o)          { return al.remove(o); }
  public boolean  containsAll(Collection c) { return al.containsAll(c); }
  public boolean  addAll(Collection c)      { return al.addAllAbsent(c) > 0; }
  public boolean  removeAll(Collection c)   { return al.removeAll(c); }
  public boolean  retainAll(Collection c)   { return al.retainAll(c); }
  public boolean  add(Object o)             { return al.addIfAbsent(o); }

}
