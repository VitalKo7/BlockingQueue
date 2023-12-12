package ait.mediation;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    private final int maxSize;    // max size of a queue //! number of messages in queue <= maxSize
    private final List<T> messages; // messages in a queue, instead of 1 in Message Box
    Lock mutex = new ReentrantLock();
    Condition producerWaitingCondition = mutex.newCondition();
    Condition consumerWaitingCondition = mutex.newCondition();

    public BlkQueueImpl(int maxSize) {
        this.maxSize = maxSize;
        this.messages = new LinkedList<>();
    }

    @Override
    public void push(T message) {   //! add <T> message in the end
        mutex.lock();
        try {
            while (messages.size() >= maxSize) {  // !  while it is full (has maxSize elements)
                try {
                    producerWaitingCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            messages.add(message);
            consumerWaitingCondition.signal();
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public T pop() {    //! get <T> message from the beginning
        mutex.lock();
        try {
            while (messages.isEmpty()) {
                try {
                    consumerWaitingCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            T res = messages.removeFirst();
            producerWaitingCondition.signal();
            return res;
        } finally {
            mutex.unlock();
        }
    }
}