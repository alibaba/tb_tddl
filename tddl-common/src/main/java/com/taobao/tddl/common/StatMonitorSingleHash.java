/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//package com.taobao.tddl.common;
//
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.SortedSet;
//import java.util.TreeSet;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicLong;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.taobao.tddl.common.util.NagiosUtils;
//
///*
// * @author guangxia
// * @since 1.0, 2010-2-8 下午04:18:39
// */
//public class StatMonitorSingleHash implements StatMonitorMBean {
//	
//	private static final Log logger = LogFactory.getLog(StatMonitorSingleHash.class);
//	
//	//单位是毫秒
//    private volatile long statInterval = 5 * 60 * 1000;
//    private Set<String> blackList = new HashSet<String>(0);
//    private int limit = 1000;
//	
//    private static final StatMonitorSingleHash instance = new StatMonitorSingleHash();
//	private StatMonitorSingleHash(){
//	}
//	public static StatMonitorSingleHash getInstance() {
//		return instance;
//	}	
//
//	static class State {
//	    final ConcurrentHashMap<String, StatCounter> currentStatMap = new ConcurrentHashMap<String, StatCounter>();
//	    final ConcurrentHashMap<String, StatCounter> lastStatMap;
//	    final AtomicInteger size = new AtomicInteger(0);
//	    final long lastResetTime = System.currentTimeMillis();
//	    State() {
//	    	lastStatMap = currentStatMap;
//	    }
//		State(final State lastState) {
//			lastStatMap = lastState.currentStatMap;
//		}
//		public String toString() {
//			return lastStatMap.toString();
//		}
//	}
//	private volatile State state = new State();
//	
//	public String toString() {
//		return state.toString();
//	}
//
//    static class StatCounter {
//        private final AtomicLong count = new AtomicLong(0L);
//        private final AtomicLong value = new AtomicLong(0L);
//
//
//        public void incrementCount() {
//            this.count.incrementAndGet();
//        }
//
//        public void addValue(long value) {
//            this.value.addAndGet(value);
//        }
//
//        public synchronized void reset() {
//            this.count.set(0L);
//            this.value.set(0L);
//        }
//        
//        public String toString() {
//        	return "{StatCounter, "+count.get()+", "+value.get()+"}";
//        }
//    }
//
//    private Thread restTask = new Thread() {
//    	@Override
//        public void run() {
//            while (!Thread.currentThread().isInterrupted()) {
//                try {
//                    Thread.sleep(statInterval);
//                }
//                catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//                StatMonitorSingleHash.State old = resetState();
//				writeMonitor(old);
//            }
//        }
//    };
//
//    private void writeMonitor(State old) {
//		try {
//			for (Map.Entry<String, StatCounter> e2 : old.currentStatMap.entrySet()) {
//				String key3 = e2.getKey();
//
//				StatCounter counter = e2.getValue();
//				if (counter == null) {
//					continue;
//				}
//				long count = counter.count.get();
//				long values = counter.value.get();
//				String averageValueStr = "invalid";
//				if (count != 0) {
//					double averageValue = (double) values / count;
//					averageValueStr = String.valueOf(averageValue);
//				}
//				NagiosUtils.addNagiosLog(key3, averageValueStr);
//			}
//		} catch (Exception e) {
//			logger.warn("", e);
//		}
//	}
//    
//	private boolean started = false;
//
//	public synchronized void start() {
//		if (started) {
//			return;
//		}
//		restTask.start();
//		started = true;
//		logger.warn("StatMonitor start...");
//	}
//
//    public void stop() {
//    	restTask.interrupt();
//        while (restTask.isAlive()) {
//            try {
//            	restTask.join();
//            }
//            catch (InterruptedException e) {
//
//            }
//        }
//    }
//
//    private synchronized final State resetState() {
//    	State old = state;
//    	state = new State(state);
//    	return old;
//    }
//    
//    private final String buildKey(String key1, String key2, String key3) {
//		//return "{key1: '"+key1+"', key2: '"+key2+"', key3: '"+key3+"'}";
//		return key1 + "|" + key2 + "|" + key3;
//	}
//
//    private final String getLastStatResult(String key1, String key2, String key3) {
//    	State local_state = state;
//        String key = buildKey(key1, key2, key3);
//        StatCounter counter = state.lastStatMap.get(key);
//        if(counter == null) {
//        	return null;
//        }
//        long count = counter.count.get();
//        long values = counter.value.get();
//        String averageValueStr = "invalid";
//        String averageCountStr = "invalid";
//        if (count != 0) {
//            double averageValue = (double)values / count;
//            averageValueStr = String.valueOf(averageValue);
//        }
//        long duration;
//        if(local_state.lastStatMap != local_state.currentStatMap) {
//        	duration = statInterval;
//        } else {
//        	duration = System.currentTimeMillis() - local_state.lastResetTime;
//        }
//        if (duration != 0) {
//            double averageCount = (double)(count * 1000) / duration;
//            averageCountStr = String.valueOf(averageCount);
//        }
//        return "count: "+count+", value: "+values+", average: "+averageValueStr+", Count/Duration: "+averageCountStr;
//    }
//    
//    static class Item {
//    	final String key1;
//    	final String key2;
//    	final String key3;
//    	final long count;
//    	final long value;
//    	public Item(String key1, String key2, String key3, long count, long value) {
//    		this.key1 = key1;
//    		this.key2 = key2;
//    		this.key3 = key3;
//    		this.count = count;
//    		this.value = value;
//    	}
//    	public String toString() {
//    		return "{("+key1+", "+key2+", "+key3+"), count: "+count+", value: "+value+"}";
//    	}
//    }
//    
//    static class CountComparator implements Comparator<Item> {
//		public int compare(Item o1, Item o2) {
//			int ret = - ((Long)o1.count).compareTo(o2.count);
//			if(ret != 0) {
//			 	return ret;
//			}
//			if(o1 == o2) {
//				return ret;
//			}
//			return ((Integer)System.identityHashCode(o1)).compareTo(System.identityHashCode(o2));
//		}
//    }
//    
//    static class ValueComparator implements Comparator<Item> {
//    	public int compare(Item o1, Item o2) {
//    		int ret = - ((Long)o1.value).compareTo(o2.value);
//    		if(ret != 0) {
//    			return ret;
//    		}
//    		if(o1 == o2) {
//    			return ret;
//    		}
//    		return ((Integer)System.identityHashCode(o1)).compareTo(System.identityHashCode(o2));
//    	}
//    }
//    
//    final CountComparator countComparator = new CountComparator();
//    final ValueComparator valueComparator = new ValueComparator();
//    
//    public SortedSet<Item> getSortedSetByCountOrValue(String key1, String key2, String key3, boolean byCount) {
//    	State local_state = state;
//    	boolean star1 = false, star2 = false, star3 = false;
//    	if("*".equals(key1)) {
//    		star1 = true;
//    	}
//    	if("*".equals(key2)) {
//    		star2 = true;
//    	}
//    	if("*".equals(key3)) {
//    		star3 = true;
//    	}
//    	SortedSet<Item> sortedSet = new TreeSet<Item>(byCount ? countComparator : valueComparator);
//    	for(Map.Entry<String, StatCounter> entry : local_state.lastStatMap.entrySet()) {
//    		String key = entry.getKey();
//    		if(!star1) {
//    			if(key.indexOf("{key1: '"+key1+"',") == -1) {
//    				continue;
//    			}
//    		}
//    		if(!star2) {
//    			if(key.indexOf("key2: '"+key2+"',") == -1) {
//    				continue;
//    			}
//    		}
//    		if(!star3) {
//    			if(key.indexOf("key3: '"+key3+"'}") == -1) {
//    				continue;
//    			}
//    		}
//    		sortedSet.add(new Item(key1, key2, key3, entry.getValue().count.get(), entry.getValue().value.get()));
//    	}
//    	return sortedSet;
//    }
//    
//    public SortedSet<Item> getSortedSetByCount(String key1, String key2, String key3) {
//    	return getSortedSetByCountOrValue(key1, key2, key3, true);
//    }
//    
//    public SortedSet<Item> getSortedSetByValue(String key1, String key2, String key3) {
//    	return getSortedSetByCountOrValue(key1, key2, key3, false);
//    }
//    
//    public long getDuration() {
//    	State local_state = state;
//        return (System.currentTimeMillis() - local_state.lastResetTime) / 1000;
//    }
//
//    public final boolean addStat(String keyOne, String keyTwo, String keyThree) {
//        return realTimeStat(keyOne, keyTwo, keyThree, 0);
//    }
//
//    private final boolean realTimeStat(String key1, String key2, String key3, long value) {
//        if(blackList.contains(key1)) {
//        	return false;
//        }
//        return processMap2(key1, key2, key3, value);
//    }
//
//    private boolean processMap2(String key1, String key2, String key3, long value) {
//    	State local_state = state;
//    	String key = buildKey(key1, key2, key3);
//    	StatCounter counter = local_state.currentStatMap.get(key);
//    	if(counter == null) {
//    		if(local_state.size.get() >= limit) {
//    			return false;
//    		}
//    		counter = new StatCounter();
//    		StatCounter oldCounter = local_state.currentStatMap.putIfAbsent(key, counter);
//    		if(oldCounter != null) {
//    			counter = oldCounter;
//    		} else {
//    			local_state.size.incrementAndGet();
//    		}
//    	}
//    	counter.incrementCount();
//    	counter.addValue(value);
//        return true;
//    }
//
//    public final boolean addStat(String keyOne, String keyTwo, String keyThree, long value) {
//        return realTimeStat(keyOne, keyTwo, keyThree, value);
//    }
//    
//    public void setStatInterval(long statInterval) {
//		this.statInterval = statInterval;
//	}
//	
//    public long getStatInterval() {
//		return statInterval;
//	}
//	
//	public long getStatDuration() {
//		State local_state = state;
//		return local_state.lastResetTime;
//	}
//	
//	public String getStatResult(String key1, String key2, String key3) {
//		return getLastStatResult(key1, key2, key3);
//	}
//	
//	public void resetStat() {
//		resetState();
//	}
//	public void setLimit(int limit) {
//		this.limit = limit;
//	}
//	public int getLimit() {
//		return limit;
//	}
//	@SuppressWarnings("unchecked")
//	public void setBlackList(Set<String> blackList) {
//		if(!(blackList instanceof HashSet)) {
//			blackList = new HashSet<String>(blackList);
//		}
//		this.blackList = blackList;
//	}
//	public Set<String> getBlackList() {
//		return blackList;
//	}
//
//}
