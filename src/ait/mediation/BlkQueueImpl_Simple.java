package ait.mediation;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl_Simple<T> implements BlkQueue<T> {

    Lock mutex = new ReentrantLock();
    Condition producerWaitingCondition = mutex.newCondition();
    Condition consumerWaitingCondition = mutex.newCondition();
    int maxSize;
    private T message;

    //! Используйте LinkedList как внутреннюю реализацию очереди в классе BlkQueue.
    //! number of messages in queue vs. maxSize (instead of 1 in Message Box)

    public BlkQueueImpl_Simple(int maxSize) {
        // TODO
//        throw new UnsupportedOperationException("Not implemented");
        this.maxSize = maxSize;
    }

    @Override
    public void push(T message) {   //! post || add <T> message OR Producers (writers)
        mutex.lock();
        try {
            while (this.message != null) {  // !  while it is full (has maxSize elements)
                try {
                    producerWaitingCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            this.message = message;
            consumerWaitingCondition.signal();
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public T pop() {    //! get <T> message OR Consumers (readers)
        mutex.lock();
        try {
            while (this.message == null) {
                try {
                    consumerWaitingCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            T res = message;
            message = null;

            producerWaitingCondition.signal();

            return res;

        } finally {
            mutex.unlock();
        }
    }
}