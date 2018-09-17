package android_maps_conflict_avoidance.com.google.common.task;

import java.util.Vector;

public abstract class AbstractTask {
    private static final AbstractTask[] EMPTY_TASK_ARRAY = null;
    private final String name;
    private int runCounter;
    private Object runCounterLock;
    protected Runnable runnable;
    protected TaskRunner runner;
    private int state;
    protected Vector tasks;
    private final String varzInsideQueue;
    private final String varzOutsideQueue;
    private final String varzTime;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.task.AbstractTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.task.AbstractTask.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.task.AbstractTask.<clinit>():void");
    }

    abstract int cancelInternal();

    abstract void scheduleInternal();

    public AbstractTask(TaskRunner runner, Runnable runnable, String name) {
        this.runCounterLock = new Object();
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
        if (this.name != null) {
        }
    }

    void updateRunnableTimestamp() {
        if (this.name != null) {
        }
    }

    void updateStartTimestamp() {
        if (this.name != null) {
        }
    }

    void updateFinishTimestamp() {
        if (this.name != null) {
        }
    }
}
