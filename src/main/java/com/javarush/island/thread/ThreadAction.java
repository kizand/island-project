package com.javarush.island.thread;

public abstract class ThreadAction implements Runnable {

    private Throwable throwable;

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public void run() {
        try {
            doAction();
        } catch (Throwable e) {
            throwable = e;
        }
    }

    protected abstract void doAction() throws Throwable;
}
