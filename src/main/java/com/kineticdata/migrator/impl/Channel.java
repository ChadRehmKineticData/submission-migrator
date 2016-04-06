package com.kineticdata.migrator.impl;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Channel<E> {

    private E[] items;
    private final int capacity; // maximum size of the queue
    private int count;          // total number of items added to queue
    private int size;           // current size of the queue
    private final Lock lock;
    private final Condition notEmpty;
    private final Condition notFull;

    public Channel(int capacity) {
        this.capacity = capacity;
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }

    private void enqueue(E item) {
    }

    private E dequeue() {
        return null;
    }

    public void put(E item) throws InterruptedException {
        if (item == null)
            throw new IllegalArgumentException("Cannot put null into channel.");

        lock.lockInterruptibly();
        try {
            while(size == capacity)
                notFull.await();
            enqueue(item);
        } finally {
            lock.unlock();
        }
    }

    public E take() {
        return null;
    }

    public void close() {

    }

    public int count() {
        return count;
    }

    public int size() {
        return size;
    }

}
