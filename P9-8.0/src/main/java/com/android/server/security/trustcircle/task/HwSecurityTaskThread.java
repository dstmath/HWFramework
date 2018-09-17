package com.android.server.security.trustcircle.task;

import com.android.server.security.trustcircle.utils.LogHelper;
import java.util.ArrayList;

public class HwSecurityTaskThread extends Thread {
    public static final int PRIORITY_HIGH = 0;
    public static final int PRIORITY_LOW = 2;
    public static final int PRIORITY_NORMAL = 1;
    private static final String TAG = HwSecurityTaskThread.class.getSimpleName();
    private static HwSecurityTaskThread gInstance = null;
    private static Object mInstanceLock = new Object();
    private ArrayList<HwSecurityTaskBase> mHighPriorityTasks = new ArrayList();
    private ArrayList<HwSecurityTaskBase> mLowPriorityTasks = new ArrayList();
    private ArrayList<HwSecurityTaskBase> mNormalPriorityTasks = new ArrayList();
    private boolean mNotified = false;
    private Object mSignal = new Object();
    private boolean mStop = false;
    private Object mTaskLock = new Object();

    public void startThread() {
        synchronized (this.mSignal) {
            this.mStop = false;
            this.mNotified = false;
            start();
        }
    }

    public void stopThread() {
        synchronized (this.mSignal) {
            this.mStop = true;
            this.mNotified = true;
            this.mSignal.notifyAll();
        }
        try {
            join(0);
        } catch (InterruptedException e) {
        }
    }

    public void notifyThread() {
        synchronized (this.mSignal) {
            this.mNotified = true;
            this.mSignal.notifyAll();
        }
    }

    public void waitThread() {
        try {
            synchronized (this.mSignal) {
                if (!this.mNotified) {
                    this.mSignal.wait();
                }
                this.mNotified = false;
            }
        } catch (InterruptedException e) {
            LogHelper.e(TAG, "security taskthread wait failed.");
        }
    }

    public boolean checkQuit() {
        boolean z;
        synchronized (this.mSignal) {
            z = this.mStop;
        }
        return z;
    }

    public void run() {
        while (!checkQuit()) {
            HwSecurityTaskBase task = getNextTask();
            if (task != null) {
                LogHelper.i(TAG, "run: " + task.getClass().getSimpleName() + ", status: " + task.getTaskStatus());
                if (task.getTaskStatus() == 0) {
                    task.execute();
                }
            } else {
                waitThread();
            }
        }
    }

    public boolean pushTask(HwSecurityTaskBase task, int priority) {
        boolean pushSucceed;
        synchronized (this.mTaskLock) {
            LogHelper.i(TAG, "pushTask: " + task.getClass().getSimpleName());
            switch (priority) {
                case 0:
                    pushSucceed = this.mHighPriorityTasks.add(task);
                    break;
                case 1:
                    pushSucceed = this.mNormalPriorityTasks.add(task);
                    break;
                case 2:
                    pushSucceed = this.mLowPriorityTasks.add(task);
                    break;
                default:
                    pushSucceed = this.mNormalPriorityTasks.add(task);
                    break;
            }
            if (pushSucceed) {
                task.onStart();
                notifyThread();
            } else {
                LogHelper.w(TAG, "pushTask: " + task.getClass().getSimpleName() + " failed, priority: " + priority);
            }
        }
        return pushSucceed;
    }

    /* JADX WARNING: Missing block: B:27:0x004a, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected HwSecurityTaskBase getNextTask() {
        synchronized (this.mTaskLock) {
            HwSecurityTaskBase hwSecurityTaskBase;
            if (this.mHighPriorityTasks != null && !this.mHighPriorityTasks.isEmpty()) {
                hwSecurityTaskBase = (HwSecurityTaskBase) this.mHighPriorityTasks.remove(0);
                return hwSecurityTaskBase;
            } else if (this.mNormalPriorityTasks != null && !this.mNormalPriorityTasks.isEmpty()) {
                hwSecurityTaskBase = (HwSecurityTaskBase) this.mNormalPriorityTasks.remove(0);
                return hwSecurityTaskBase;
            } else if (this.mLowPriorityTasks == null || this.mLowPriorityTasks.isEmpty()) {
            } else {
                hwSecurityTaskBase = (HwSecurityTaskBase) this.mLowPriorityTasks.remove(0);
                return hwSecurityTaskBase;
            }
        }
    }

    public static void staticPushTask(HwSecurityTaskBase task, int priority) {
        HwSecurityTaskThread gThread = getInstance();
        if (gThread != null) {
            gThread.pushTask(task, priority);
        }
    }

    public static void createInstance() {
        synchronized (mInstanceLock) {
            if (gInstance == null) {
                gInstance = new HwSecurityTaskThread();
            }
        }
    }

    public static HwSecurityTaskThread getInstance() {
        HwSecurityTaskThread hwSecurityTaskThread;
        synchronized (mInstanceLock) {
            hwSecurityTaskThread = gInstance;
        }
        return hwSecurityTaskThread;
    }

    public static void destroyInstance() {
        synchronized (mInstanceLock) {
            if (gInstance != null) {
                gInstance = null;
            }
        }
    }
}
