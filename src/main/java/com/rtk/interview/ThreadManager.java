package com.rtk.interview;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class ThreadManager {

    private static final List<ThreadConfiguration> THREADS_CONFIGURATIONS = List.of(
            new ThreadConfiguration(1, 10, 3, 12, 2),
            new ThreadConfiguration(2, 5, 2, 7, 2)
    ) ;

    private static final Random RANDOM = new Random();

    private static final List<ReentrantLock> LOCKS = List.of(
            new ReentrantLock(),
            new ReentrantLock(),
            new ReentrantLock(),
            new ReentrantLock(),
            new ReentrantLock());


    public void runTest() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(THREADS_CONFIGURATIONS.size());
        THREADS_CONFIGURATIONS.forEach(
                threadConfiguration ->
                        executorService.scheduleAtFixedRate(() -> runThread(threadConfiguration),
                                RANDOM.nextInt(500), threadConfiguration.getWakeIntervalInMillis(), TimeUnit.MILLISECONDS)
        );
    }

    private void runThread(ThreadConfiguration threadConfiguration) {
        int threadId = threadConfiguration.getThreadId();
        System.out.println("Thread " + threadId + " started...");
        List<ReentrantLock> locks = getRandomLocks(threadConfiguration.getNumberOfResources());
        try {
            //Здесь решил пойти по пути предварительной проверки блокировок и брать их в работу только при условии всех свободных объектов блокировок
            //Таким образом, ситуация с дедлоком тоже не должна произойти
            boolean allLocksFree = locks.stream().noneMatch(ReentrantLock::isLocked);
            if (allLocksFree) {
                System.out.println(MessageFormat.format("ThreadId: {0}. All random locks are free. Locking them...", threadId));
                locks.forEach(ReentrantLock::lock);
                Thread.sleep(calculateIntervalInMillis(threadConfiguration.getDurationMinInSec(), threadConfiguration.getDurationMaxInSec()));
                locks.forEach(ReentrantLock::unlock);
                System.out.println(MessageFormat.format("ThreadId: {0}. Random locks released.", threadId));
            } else {
                System.out.println(MessageFormat.format("ThreadId: {0}. Some random locks are busy. Skip iteration.", threadId));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<ReentrantLock> getRandomLocks (int numberOfLocks) {
        List <ReentrantLock> locks = new ArrayList<>(numberOfLocks);
        for (int i = 0; i < numberOfLocks; i++) {
            locks.add(getRandomLock());
        }
        return locks;
    }

    private static ReentrantLock getRandomLock() {
        return LOCKS.get(RANDOM.nextInt(LOCKS.size()));
    }

    private static int calculateIntervalInMillis(int durationMinInSec, int durationMaxInSec) {
        return (RANDOM.nextInt(durationMaxInSec) + durationMinInSec) * 1000;
    }
}
