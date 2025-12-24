package com.rtk.interview;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadTask implements Runnable {

    private final int threadId;
    private final ThreadConfiguration threadConfiguration;
    private final List<ReentrantLock> locks;

    public ThreadTask(ThreadConfiguration threadConfiguration, List<ReentrantLock> locks) {
        this.threadId = threadConfiguration.getThreadId();
        this.threadConfiguration = threadConfiguration;
        this.locks = locks;
    }

    @Override
    public void run() {
        while (true) {
            if (lockAvailableResources()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        int threadSleepTime = calculateIntervalInSec(threadConfiguration.getDurationMaxInSec(), threadConfiguration.getDurationMaxInSec());
                        System.out.println(MessageFormat.format("ThreadId: {0}. All random locks are free. Locking them... Delay in sec: {1}", threadId, threadSleepTime));
                        //Производим вычисления...(выставляем задержку)
                        TimeUnit.SECONDS.sleep(threadSleepTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        unlockCurrentThreadLocks();
                    }
                });
            }
            try {
                //Засыпаем в ожидании следующей итерации
                TimeUnit.SECONDS.sleep(threadConfiguration.getWakeIntervalInSec());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean lockAvailableResources() {
        for (ReentrantLock reentrantLock : locks) {
            if (!reentrantLock.isLocked()) {
                reentrantLock.lock();
            } else {
                System.out.println(MessageFormat.format("ThreadId: {0}. Some random locks are busy. Skip iteration.", threadId));
                unlockCurrentThreadLocks();
                return false;
            }
        }
        return true;
    }

    private void unlockCurrentThreadLocks() {
        locks.stream()
                .filter(lock -> lock.isHeldByCurrentThread() && lock.isLocked())
                .forEach(ReentrantLock::unlock);
        System.out.println(MessageFormat.format("ThreadId: {0}. Locks released.", threadId));
    }

    private int calculateIntervalInSec(int minInSec, int maxInSec) {
        return (int) ((Math.random() * (maxInSec - minInSec)) + minInSec);
    }
}
