package java.util;

import java.util.concurrent.atomic.AtomicInteger;

public class Timer {
    private static final AtomicInteger nextSerialNumber = null;
    private final TaskQueue queue;
    private final TimerThread thread;
    private final Object threadReaper;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.Timer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.Timer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.Timer.<clinit>():void");
    }

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
            protected void finalize() throws Throwable {
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
            protected void finalize() throws Throwable {
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
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay.");
        }
        sched(task, System.currentTimeMillis() + delay, 0);
    }

    public void schedule(TimerTask task, Date time) {
        sched(task, time.getTime(), 0);
    }

    public void schedule(TimerTask task, long delay, long period) {
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay.");
        } else if (period <= 0) {
            throw new IllegalArgumentException("Non-positive period.");
        } else {
            sched(task, System.currentTimeMillis() + delay, -period);
        }
    }

    public void schedule(TimerTask task, Date firstTime, long period) {
        if (period <= 0) {
            throw new IllegalArgumentException("Non-positive period.");
        }
        sched(task, firstTime.getTime(), -period);
    }

    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay.");
        } else if (period <= 0) {
            throw new IllegalArgumentException("Non-positive period.");
        } else {
            sched(task, System.currentTimeMillis() + delay, period);
        }
    }

    public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
        if (period <= 0) {
            throw new IllegalArgumentException("Non-positive period.");
        }
        sched(task, firstTime.getTime(), period);
    }

    private void sched(TimerTask task, long time, long period) {
        if (time < 0) {
            throw new IllegalArgumentException("Illegal execution time.");
        }
        if (Math.abs(period) > 4611686018427387903L) {
            period >>= 1;
        }
        synchronized (this.queue) {
            if (this.thread.newTasksMayBeScheduled) {
                synchronized (task.lock) {
                    if (task.state != 0) {
                        throw new IllegalStateException("Task already scheduled or cancelled");
                    }
                    task.nextExecutionTime = time;
                    task.period = period;
                    task.state = 1;
                }
                this.queue.add(task);
                if (this.queue.getMin() == task) {
                    this.queue.notify();
                }
            } else {
                throw new IllegalStateException("Timer already cancelled.");
            }
        }
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
