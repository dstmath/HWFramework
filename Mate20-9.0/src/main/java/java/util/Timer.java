package java.util;

import java.util.concurrent.atomic.AtomicInteger;

public class Timer {
    private static final AtomicInteger nextSerialNumber = new AtomicInteger(0);
    /* access modifiers changed from: private */
    public final TaskQueue queue;
    /* access modifiers changed from: private */
    public final TimerThread thread;
    private final Object threadReaper;

    private static int serialNumber() {
        return nextSerialNumber.getAndIncrement();
    }

    public Timer() {
        this("Timer-" + serialNumber());
    }

    public Timer(boolean isDaemon) {
        this("Timer-" + serialNumber(), isDaemon);
    }

    public Timer(String name) {
        this.queue = new TaskQueue();
        this.thread = new TimerThread(this.queue);
        this.threadReaper = new Object() {
            /* access modifiers changed from: protected */
            public void finalize() throws Throwable {
                synchronized (Timer.this.queue) {
                    Timer.this.thread.newTasksMayBeScheduled = false;
                    Timer.this.queue.notify();
                }
            }
        };
        this.thread.setName(name);
        this.thread.start();
    }

    public Timer(String name, boolean isDaemon) {
        this.queue = new TaskQueue();
        this.thread = new TimerThread(this.queue);
        this.threadReaper = new Object() {
            /* access modifiers changed from: protected */
            public void finalize() throws Throwable {
                synchronized (Timer.this.queue) {
                    Timer.this.thread.newTasksMayBeScheduled = false;
                    Timer.this.queue.notify();
                }
            }
        };
        this.thread.setName(name);
        this.thread.setDaemon(isDaemon);
        this.thread.start();
    }

    public void schedule(TimerTask task, long delay) {
        if (delay >= 0) {
            sched(task, System.currentTimeMillis() + delay, 0);
            return;
        }
        throw new IllegalArgumentException("Negative delay.");
    }

    public void schedule(TimerTask task, Date time) {
        sched(task, time.getTime(), 0);
    }

    public void schedule(TimerTask task, long delay, long period) {
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay.");
        } else if (period > 0) {
            sched(task, System.currentTimeMillis() + delay, -period);
        } else {
            throw new IllegalArgumentException("Non-positive period.");
        }
    }

    public void schedule(TimerTask task, Date firstTime, long period) {
        if (period > 0) {
            sched(task, firstTime.getTime(), -period);
            return;
        }
        throw new IllegalArgumentException("Non-positive period.");
    }

    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay.");
        } else if (period > 0) {
            sched(task, System.currentTimeMillis() + delay, period);
        } else {
            throw new IllegalArgumentException("Non-positive period.");
        }
    }

    public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
        if (period > 0) {
            sched(task, firstTime.getTime(), period);
            return;
        }
        throw new IllegalArgumentException("Non-positive period.");
    }

    private void sched(TimerTask task, long time, long period) {
        if (time >= 0) {
            if (Math.abs(period) > 4611686018427387903L) {
                period >>= 1;
            }
            long period2 = period;
            synchronized (this.queue) {
                if (this.thread.newTasksMayBeScheduled) {
                    synchronized (task.lock) {
                        if (task.state == 0) {
                            task.nextExecutionTime = time;
                            task.period = period2;
                            task.state = 1;
                        } else {
                            throw new IllegalStateException("Task already scheduled or cancelled");
                        }
                    }
                    this.queue.add(task);
                    if (this.queue.getMin() == task) {
                        this.queue.notify();
                    }
                } else {
                    throw new IllegalStateException("Timer already cancelled.");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Illegal execution time.");
    }

    public void cancel() {
        synchronized (this.queue) {
            this.thread.newTasksMayBeScheduled = false;
            this.queue.clear();
            this.queue.notify();
        }
    }

    public int purge() {
        int result = 0;
        synchronized (this.queue) {
            for (int i = this.queue.size(); i > 0; i--) {
                if (this.queue.get(i).state == 3) {
                    this.queue.quickRemove(i);
                    result++;
                }
            }
            if (result != 0) {
                this.queue.heapify();
            }
        }
        return result;
    }
}
