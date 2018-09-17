package com.android.server.security.trustcircle.task;

import com.android.server.security.trustcircle.utils.LogHelper;
import java.util.ArrayList;

public class HwSecurityTaskThread extends Thread {
    public static final int PRIORITY_HIGH = 0;
    public static final int PRIORITY_LOW = 2;
    public static final int PRIORITY_NORMAL = 1;
    private static final String TAG = null;
    private static HwSecurityTaskThread gInstance;
    private static Object mInstanceLock;
    private ArrayList<HwSecurityTaskBase> mHighPriorityTasks;
    private ArrayList<HwSecurityTaskBase> mLowPriorityTasks;
    private ArrayList<HwSecurityTaskBase> mNormalPriorityTasks;
    private boolean mNotified;
    private Object mSignal;
    private boolean mStop;
    private Object mTaskLock;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.task.HwSecurityTaskThread.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.task.HwSecurityTaskThread.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.task.HwSecurityTaskThread.<clinit>():void");
    }

    public HwSecurityTaskThread() {
        this.mHighPriorityTasks = new ArrayList();
        this.mNormalPriorityTasks = new ArrayList();
        this.mLowPriorityTasks = new ArrayList();
        this.mSignal = new Object();
        this.mTaskLock = new Object();
        this.mStop = false;
        this.mNotified = false;
    }

    public void startThread() {
        this.mStop = false;
        this.mNotified = false;
        start();
    }

    public void stopThread() {
        synchronized (this.mSignal) {
            this.mStop = true;
            this.mNotified = true;
            this.mSignal.notify();
        }
        try {
            join(0);
        } catch (InterruptedException e) {
        }
    }

    public void notifyThread() {
        synchronized (this.mSignal) {
            this.mNotified = true;
            this.mSignal.notify();
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean pushTask(HwSecurityTaskBase task, int priority) {
        boolean pushSucceed;
        synchronized (this.mTaskLock) {
            LogHelper.i(TAG, "pushTask: " + task.getClass().getSimpleName());
            switch (priority) {
                case PRIORITY_HIGH /*0*/:
                    pushSucceed = this.mHighPriorityTasks.add(task);
                    break;
                case PRIORITY_NORMAL /*1*/:
                    pushSucceed = this.mNormalPriorityTasks.add(task);
                    break;
                case PRIORITY_LOW /*2*/:
                    pushSucceed = this.mLowPriorityTasks.add(task);
                    break;
                default:
                    pushSucceed = this.mNormalPriorityTasks.add(task);
                    break;
            }
        }
        return pushSucceed;
    }

    protected HwSecurityTaskBase getNextTask() {
        synchronized (this.mTaskLock) {
            HwSecurityTaskBase hwSecurityTaskBase;
            if (this.mHighPriorityTasks != null && !this.mHighPriorityTasks.isEmpty()) {
                hwSecurityTaskBase = (HwSecurityTaskBase) this.mHighPriorityTasks.remove(PRIORITY_HIGH);
                return hwSecurityTaskBase;
            } else if (this.mNormalPriorityTasks != null && !this.mNormalPriorityTasks.isEmpty()) {
                hwSecurityTaskBase = (HwSecurityTaskBase) this.mNormalPriorityTasks.remove(PRIORITY_HIGH);
                return hwSecurityTaskBase;
            } else if (this.mLowPriorityTasks == null || this.mLowPriorityTasks.isEmpty()) {
                return null;
            } else {
                hwSecurityTaskBase = (HwSecurityTaskBase) this.mLowPriorityTasks.remove(PRIORITY_HIGH);
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
