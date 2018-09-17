package com.android.okhttp.internal;

public abstract class NamedRunnable implements Runnable {
    protected final String name;

    protected abstract void execute();

    public NamedRunnable(String format, Object... args) {
        this.name = String.format(format, args);
    }

    public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(this.name);
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }
}
