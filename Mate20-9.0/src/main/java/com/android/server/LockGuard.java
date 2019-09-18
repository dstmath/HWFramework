package com.android.server;

import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class LockGuard {
    public static final int INDEX_ACTIVITY = 6;
    public static final int INDEX_APP_OPS = 0;
    public static final int INDEX_DPMS = 7;
    public static final int INDEX_PACKAGES = 3;
    public static final int INDEX_POWER = 1;
    public static final int INDEX_STORAGE = 4;
    public static final int INDEX_USER = 2;
    public static final int INDEX_WINDOW = 5;
    private static final String TAG = "LockGuard";
    private static ArrayMap<Object, LockInfo> sKnown = new ArrayMap<>(0, true);
    private static Object[] sKnownFixed = new Object[8];

    private static class LockInfo {
        public ArraySet<Object> children;
        public boolean doWtf;
        public String label;

        private LockInfo() {
            this.children = new ArraySet<>(0, true);
        }
    }

    private static LockInfo findOrCreateLockInfo(Object lock) {
        LockInfo info = sKnown.get(lock);
        if (info != null) {
            return info;
        }
        LockInfo info2 = new LockInfo();
        info2.label = "0x" + Integer.toHexString(System.identityHashCode(lock)) + " [" + new Throwable().getStackTrace()[2].toString() + "]";
        sKnown.put(lock, info2);
        return info2;
    }

    public static Object guard(Object lock) {
        if (lock == null || Thread.holdsLock(lock)) {
            return lock;
        }
        LockInfo info = findOrCreateLockInfo(lock);
        int i = 0;
        boolean triggered = false;
        for (int i2 = 0; i2 < info.children.size(); i2++) {
            Object child = info.children.valueAt(i2);
            if (child != null && Thread.holdsLock(child)) {
                doLog(lock, "Calling thread " + Thread.currentThread().getName() + " is holding " + lockToString(child) + " while trying to acquire " + lockToString(lock));
                triggered = true;
            }
        }
        if (!triggered) {
            while (true) {
                int i3 = i;
                if (i3 >= sKnown.size()) {
                    break;
                }
                Object test = sKnown.keyAt(i3);
                if (!(test == null || test == lock || !Thread.holdsLock(test))) {
                    sKnown.valueAt(i3).children.add(lock);
                }
                i = i3 + 1;
            }
        }
        return lock;
    }

    public static void guard(int index) {
        for (int i = 0; i < index; i++) {
            Object lock = sKnownFixed[i];
            if (lock != null && Thread.holdsLock(lock)) {
                Object targetMayBeNull = sKnownFixed[index];
                doLog(targetMayBeNull, "Calling thread " + Thread.currentThread().getName() + " is holding " + lockToString(i) + " while trying to acquire " + lockToString(index));
            }
        }
    }

    private static void doLog(Object lock, String message) {
        if (lock == null || !findOrCreateLockInfo(lock).doWtf) {
            Slog.w(TAG, message, new Throwable());
        } else {
            new Thread(new Runnable(new RuntimeException(message)) {
                private final /* synthetic */ Throwable f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    Slog.wtf(LockGuard.TAG, this.f$0);
                }
            }).start();
        }
    }

    public static Object installLock(Object lock, String label) {
        findOrCreateLockInfo(lock).label = label;
        return lock;
    }

    public static Object installLock(Object lock, int index) {
        return installLock(lock, index, false);
    }

    public static Object installLock(Object lock, int index, boolean doWtf) {
        sKnownFixed[index] = lock;
        LockInfo info = findOrCreateLockInfo(lock);
        info.doWtf = doWtf;
        info.label = "Lock-" + lockToString(index);
        return lock;
    }

    public static Object installNewLock(int index) {
        return installNewLock(index, false);
    }

    public static Object installNewLock(int index, boolean doWtf) {
        Object lock = new Object();
        installLock(lock, index, doWtf);
        return lock;
    }

    private static String lockToString(Object lock) {
        LockInfo info = sKnown.get(lock);
        if (info != null && !TextUtils.isEmpty(info.label)) {
            return info.label;
        }
        return "0x" + Integer.toHexString(System.identityHashCode(lock));
    }

    private static String lockToString(int index) {
        switch (index) {
            case 0:
                return "APP_OPS";
            case 1:
                return "POWER";
            case 2:
                return "USER";
            case 3:
                return "PACKAGES";
            case 4:
                return "STORAGE";
            case 5:
                return "WINDOW";
            case 6:
                return "ACTIVITY";
            case 7:
                return "DPMS";
            default:
                return Integer.toString(index);
        }
    }

    public static void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        for (int i = 0; i < sKnown.size(); i++) {
            Object lock = sKnown.keyAt(i);
            LockInfo info = sKnown.valueAt(i);
            pw.println("Lock " + lockToString(lock) + ":");
            for (int j = 0; j < info.children.size(); j++) {
                pw.println("  Child " + lockToString(info.children.valueAt(j)));
            }
            pw.println();
        }
    }
}
