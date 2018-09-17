package android_maps_conflict_avoidance.com.google.common.task;

import android_maps_conflict_avoidance.com.google.common.Clock;
import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.lang.ThreadFactory;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import java.util.Hashtable;
import java.util.Vector;

public class TaskRunner implements Runnable {
    protected final Clock clock;
    private int defaultPriority;
    protected ThreadFactory factory;
    private int maxPriority;
    private int minPriority;
    protected Object mutex;
    protected String name;
    protected Vector priorityTaskQueue;
    protected boolean running;
    private final Hashtable runningTaskMap;
    private final Hashtable taskMap;
    protected Vector timerTaskQueue;
    protected Thread[] workers;

    public TaskRunner(ThreadFactory factory) {
        this(factory, "TaskRunner", 1);
    }

    public TaskRunner(ThreadFactory factory, String name, int count) {
        this.priorityTaskQueue = new Vector();
        this.timerTaskQueue = new Vector();
        this.mutex = new Object();
        this.defaultPriority = 127;
        this.maxPriority = 255;
        this.minPriority = 0;
        this.running = false;
        this.factory = factory;
        this.name = name;
        this.workers = new Thread[count];
        this.clock = Config.getInstance().getClock();
        this.taskMap = null;
        this.runningTaskMap = null;
    }

    int getDefaultPriority() {
        return this.defaultPriority;
    }

    void scheduleTask(AbstractTask task) {
        task.updateScheduleTimestamp();
        synchronized (this.mutex) {
            switch (task.getState()) {
                case LayoutParams.MODE_MAP /*0*/:
                    task.scheduleInternal();
                    break;
                case LayoutParams.LEFT /*3*/:
                    task.setState(4);
                    break;
            }
        }
    }

    void schedulePriorityTaskInternal(Task task) {
        synchronized (this.mutex) {
            int priority = task.getPriority();
            int lower = 0;
            int upper = this.priorityTaskQueue.size();
            while (lower < upper) {
                int midway = (lower + upper) / 2;
                if (priority > ((Task) this.priorityTaskQueue.elementAt(midway)).getPriority()) {
                    upper = midway;
                } else {
                    lower = midway + 1;
                }
            }
            this.priorityTaskQueue.insertElementAt(task, lower);
            task.updateRunnableTimestamp();
            task.setState(2);
            this.mutex.notifyAll();
        }
    }

    void scheduleTimerTaskInternal(TimerTask task) {
        synchronized (this.mutex) {
            long scheduled = task.getScheduledTime();
            int lower = 0;
            int upper = this.timerTaskQueue.size();
            while (lower < upper) {
                int midway = (lower + upper) / 2;
                if ((scheduled < ((TimerTask) this.timerTaskQueue.elementAt(midway)).getScheduledTime() ? 1 : null) == null) {
                    lower = midway + 1;
                } else {
                    upper = midway;
                }
            }
            this.timerTaskQueue.insertElementAt(task, lower);
            task.setState(1);
            this.mutex.notifyAll();
        }
    }

    public int cancelTask(AbstractTask task) {
        int cancelInternal;
        synchronized (this.mutex) {
            cancelInternal = task.cancelInternal();
        }
        return cancelInternal;
    }

    boolean cancelTaskInternal(AbstractTask task) {
        synchronized (this.mutex) {
            switch (task.getState()) {
                case 1:
                    this.timerTaskQueue.removeElement(task);
                    task.setState(0);
                    this.mutex.notifyAll();
                    return true;
                case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                    this.priorityTaskQueue.removeElement(task);
                    task.setState(0);
                    this.mutex.notifyAll();
                    return true;
                case LayoutParams.LEFT /*3*/:
                case OverlayItem.ITEM_STATE_FOCUSED_MASK /*4*/:
                    task.setState(0);
                    return false;
                default:
                    return false;
            }
        }
    }

    protected boolean waitForSomethingToDo() {
        Object obj = null;
        if (this.priorityTaskQueue.isEmpty()) {
            try {
                if (this.timerTaskQueue.isEmpty()) {
                    this.mutex.wait();
                } else {
                    long delta = ((TimerTask) this.timerTaskQueue.elementAt(0)).getScheduledTime() - this.clock.currentTimeMillis();
                    if (delta <= 0) {
                        obj = 1;
                    }
                    if (obj == null) {
                        this.mutex.wait(delta);
                    }
                }
            } catch (InterruptedException e) {
            }
        }
        return this.running;
    }

    protected Task getNextTaskToRun() {
        while (!this.timerTaskQueue.isEmpty()) {
            int i;
            TimerTask timerTask = (TimerTask) this.timerTaskQueue.elementAt(0);
            if (timerTask.getScheduledTime() - this.clock.currentTimeMillis() > 0) {
                i = 1;
            } else {
                i = 0;
            }
            if (i != 0) {
                break;
            }
            this.timerTaskQueue.removeElementAt(0);
            schedulePriorityTaskInternal(timerTask);
        }
        if (this.priorityTaskQueue.isEmpty()) {
            return null;
        }
        Task task = (Task) this.priorityTaskQueue.elementAt(0);
        task.setState(3);
        this.priorityTaskQueue.removeElementAt(0);
        return task;
    }

    /* JADX WARNING: Missing block: B:9:0x0011, code:
            if (r2 == null) goto L_0x0001;
     */
    /* JADX WARNING: Missing block: B:10:0x0013, code:
            r2.updateStartTimestamp();
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            r2.runInternal();
     */
    /* JADX WARNING: Missing block: B:29:0x003f, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:30:0x0040, code:
            android_maps_conflict_avoidance.com.google.debug.Log.logThrowable("runtime exception thrown by task [" + r2 + "]", r3);
            r3.printStackTrace();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        Task task = null;
        while (true) {
            synchronized (this.mutex) {
                if (task != null) {
                    if (task.getState() == 4) {
                        task.setState(0);
                        task.scheduleInternal();
                    } else if (task.getState() == 3) {
                        task.setState(0);
                    }
                }
                if (waitForSomethingToDo()) {
                    task = getNextTaskToRun();
                } else {
                    return;
                }
            }
        }
        task.updateFinishTimestamp();
    }
}
