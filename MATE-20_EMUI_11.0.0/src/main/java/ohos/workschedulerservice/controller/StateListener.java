package ohos.workschedulerservice.controller;

import java.io.PrintWriter;
import ohos.workschedulerservice.WorkQueueManager;

public abstract class StateListener {
    public static final long APP_LISTENER = 3;
    public static final long BATTERY_LISTENER = 5;
    public static final long DEFAULT_LISTENER = 0;
    public static final long IDLE_LISTENER = 7;
    public static final long NETWORK_LISTENER = 4;
    public static final long STORAGE_LISTENER = 1;
    public static final long TIMER_LISTENER = 6;
    public static final long USER_LISTENER = 2;
    protected final WorkQueueManager workQueueMgr;

    public abstract void dumpStateListenerStatus(PrintWriter printWriter, String str);

    public abstract void tryStartSignWork(WorkStatus workStatus);

    public abstract void tryStopSignWork(WorkStatus workStatus);

    public StateListener(WorkQueueManager workQueueManager) {
        this.workQueueMgr = workQueueManager;
    }
}
