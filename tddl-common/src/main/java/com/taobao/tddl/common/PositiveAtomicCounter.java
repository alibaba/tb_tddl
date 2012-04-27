package com.taobao.tddl.common;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * 正数的原子递增器，主要用于实现轮询
 * 
 * @author apple
 * 
 */
public class PositiveAtomicCounter {
    private final AtomicInteger atom;
    private static final int mask = 0x7FFFFFFF;


    public PositiveAtomicCounter() {
        atom = new AtomicInteger(0);
    }


    public final int incrementAndGet() {
        final int rt = atom.incrementAndGet();
        return rt & mask;
    }


    public int intValue() {
        return atom.intValue();
    }

}
