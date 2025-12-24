package com.rtk.interview;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadService {

    private static final Random RANDOM = new Random();

    private final List<ThreadConfiguration> threadConfigurations;
    private final List<ReentrantLock> reentrantLocks;

    public ThreadService() {
        threadConfigurations = List.of(
                new ThreadConfiguration(1, 10, 3, 12, 2),
                new ThreadConfiguration(2, 5, 2, 7, 2));

        reentrantLocks = List.of(
                new ReentrantLock(),
                new ReentrantLock(),
                new ReentrantLock(),
                new ReentrantLock(),
                new ReentrantLock());
    }

    public void runTest() {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadConfigurations.size());
        threadConfigurations.forEach(threadConfiguration ->
                threadPool.execute(new ThreadTask(threadConfiguration, getRandomLocks(threadConfiguration.getNumberOfResources()))));
    }

    private List<ReentrantLock> getRandomLocks (int numberOfLocks) {
        List <ReentrantLock> locks = new ArrayList<>(numberOfLocks);
        for (int i = 0; i < numberOfLocks; i++) {
            locks.add(getRandomLock());
        }
        return locks;
    }

    private ReentrantLock getRandomLock() {
        return reentrantLocks.get(RANDOM.nextInt(reentrantLocks.size()));
    }
}
