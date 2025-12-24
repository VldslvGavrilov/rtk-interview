package com.rtk.interview;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
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
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(threadConfigurations.size());
        //Запускаю выполнение потоков со случайной задежкой до 150 мс во избежании одновременного обращения к локам при первом запуске
        //Не полностью доволен таким решением, так как результат зависит от конфигурации железа и объема данных, другого пока не вижу
        threadConfigurations.forEach(
                threadConfiguration ->
                        executorService.scheduleAtFixedRate(() -> runThread(threadConfiguration),
                                RANDOM.nextInt(150), threadConfiguration.getWakeIntervalInMillis(), TimeUnit.MILLISECONDS)
        );
    }

    private void runThread(ThreadConfiguration threadConfiguration) {
        int threadId = threadConfiguration.getThreadId();
        System.out.println("Thread " + threadId + " started...");
        List<ReentrantLock> locks = getRandomLocks(threadConfiguration.getNumberOfResources());
        try {
            //Здесь решил пойти по пути предварительной проверки блокировок и брать их в работу только при условии всех свободных объектов блокировок
            //Таким образом, ситуация с дедлоком здесь не должна произойти
            boolean allLocksFree = locks.stream().noneMatch(ReentrantLock::isLocked);
            if (allLocksFree) {
                int threadSleepTimeInMillis = calculateIntervalInMillis(threadConfiguration.getDurationMinInSec(), threadConfiguration.getDurationMaxInSec());
                System.out.println(MessageFormat.format("ThreadId: {0}. All random locks are free. Locking them... Delay in millis: {1}", threadId, threadSleepTimeInMillis));
                locks.forEach(ReentrantLock::lock);
                Thread.sleep(threadSleepTimeInMillis);
                locks.forEach(ReentrantLock::unlock);
                System.out.println(MessageFormat.format("ThreadId: {0}. Locks released.", threadId));
            } else {
                System.out.println(MessageFormat.format("ThreadId: {0}. Some random locks are busy. Skip iteration.", threadId));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    private static int calculateIntervalInMillis(int minInSec, int maxInSec) {
        return ThreadLocalRandom.current().nextInt(minInSec, maxInSec + 1) * 1000;
    }
}
