package com.javarush.island.component;

import com.javarush.island.config.Settings;
import com.javarush.island.island.Island;
import com.javarush.island.thread.ThreadAction;
import com.javarush.island.thread.ThreadWorker;
import com.javarush.island.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Application {

    private static final long TICK_BARRIER_TIMEOUT = 5;
    private final List<ThreadWorker> workers = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();
    private final long tickPeriodNanos;
    private final OnStartedTick onStartedTick;
    private CyclicBarrier tickBarrier;
    private long startNanos;
    private long tickCount;

    public Application(View view, Island island) {
        tickPeriodNanos = Settings
                .get()
                .getApplicationConfig()
                .getTickPeriodMillis() * 1_000_000;
        onStartedTick = new OnStartedTick(view);
        island.populate();
        startThreads(island);
    }

    public void run() throws Throwable {
        Thread currentThread = Thread.currentThread();
        startNanos = System.nanoTime();
        while (!currentThread.isInterrupted()) {
            try {
                tickBarrier.await(TICK_BARRIER_TIMEOUT, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
            }
            Throwable throwable = onStartedTick.getThrowable();
            if (throwable != null) {
                throw throwable;
            }
            checkThreads();
            tickCount++;
            sleepUntilNextTick();
        }
    }

    private void startThreads(Island island) {
        int threadsNum = Utils.AVAILABLE_PROCESSORS;
        tickBarrier = new CyclicBarrier((threadsNum + 1), onStartedTick);
        for (int i = 0; i < threadsNum; i++) {
            ThreadWorker threadWorker = new ThreadWorker(island, tickBarrier, threadsNum);
            Thread thread = new Thread(threadWorker);
            thread.setDaemon(true);
            thread.start();
            workers.add(threadWorker);
            threads.add(thread);
        }
    }

    private void checkThreads() {
        for (int i = 0; i < threads.size(); i++) {
            Thread thread = threads.get(i);
            if (!thread.isAlive()) {
                ThreadWorker threadWorker = workers.get(i);
                Throwable throwable = threadWorker.getThrowable();
                if (throwable != null) {
                    throw new RuntimeException(throwable);
                } else {
                    throw new RuntimeException("Thread " + thread.getName() + " has been interrupted");
                }
            }
        }
    }

    private void sleepUntilNextTick() throws InterruptedException {
        long nextTickNanos = startNanos + tickCount * tickPeriodNanos;
        long nowNanos = System.nanoTime();
        long sleepDurationNanos = nextTickNanos - nowNanos;
        if (sleepDurationNanos > 0) {
            TimeUnit.NANOSECONDS.sleep(sleepDurationNanos);
        } else if (sleepDurationNanos < 0) {
            startNanos = System.nanoTime();
            tickCount = 0;
        }
    }

    private static class OnStartedTick extends ThreadAction {

        private final View view;

        private OnStartedTick(View view) {
            this.view = view;
        }

        @Override
        protected void doAction() {
            view.show();
        }
    }
}
