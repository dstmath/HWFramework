package android_maps_conflict_avoidance.com.google.common.task;

import java.util.Vector;

public abstract class AbstractTask {
    private static final AbstractTask[] EMPTY_TASK_ARRAY = new AbstractTask[0];
    private final String name;
    private int runCounter;
    private Object runCounterLock = new Object();
    protected Runnable runnable;
    protected TaskRunner runner;
    private int state;
    protected Vector tasks;
    private final String varzInsideQueue;
    private final String varzOutsideQueue;
    private final String varzTime;

    abstract int cancelInternal();

    abstract void scheduleInternal();

    public AbstractTask(TaskRunner runner, Runnable runnable, String name) {
        this.runner = runner;
        this.runnable = runnable;
        this.name = name;
        this.varzOutsideQueue = null;
        this.varzInsideQueue = null;
        this.varzTime = null;
    }

    protected AbstractTask[] getTasks() {
        AbstractTask[] taskArray;
        synchronized (this) {
            if (this.tasks == null) {
                taskArray = EMPTY_TASK_ARRAY;
            } else {
                taskArray = new AbstractTask[this.tasks.size()];
                this.tasks.copyInto(taskArray);
            }
        }
        return taskArray;
    }

    protected int getState() {
        return this.state;
    }

    protected void setState(int state) {
        this.state = state;
    }

    public void schedule() {
        synchronized (this.runCounterLock) {
            this.runCounter = 0;
        }
        this.runner.scheduleTask(this);
    }

    protected void run() {
        if (this.runnable != null) {
            this.runnable.run();
        }
    }

    void runInternal() {
        try {
            run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        synchronized (this.runCounterLock) {
            this.runCounter++;
            this.runCounterLock.notifyAll();
        }
        AbstractTask[] taskArray = getTasks();
        for (AbstractTask schedule : taskArray) {
            schedule.schedule();
        }
    }

    void updateScheduleTimestamp() {
        String str = this.name;
    }

    void updateRunnableTimestamp() {
        String str = this.name;
    }

    void updateStartTimestamp() {
        String str = this.name;
    }

    void updateFinishTimestamp() {
        String str = this.name;
    }
}
