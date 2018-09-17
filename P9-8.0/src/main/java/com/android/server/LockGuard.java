package com.android.server;

import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class LockGuard {
    public static final boolean ENABLED = false;
    public static final int INDEX_ACTIVITY = 6;
    public static final int INDEX_APP_OPS = 0;
    public static final int INDEX_PACKAGES = 3;
    public static final int INDEX_POWER = 1;
    public static final int INDEX_STORAGE = 4;
    public static final int INDEX_USER = 2;
    public static final int INDEX_WINDOW = 5;
    private static final String TAG = "LockGuard";
    private static ArrayMap<Object, LockInfo> sKnown = new ArrayMap(0, true);
    private static Object[] sKnownFixed = new Object[7];

    private static class LockInfo {
        public ArraySet<Object> children;
        public String label;

        /* synthetic */ LockInfo(LockInfo -this0) {
            this();
        }

        private LockInfo() {
            this.children = new ArraySet(0, true);
        }
    }

    private static LockInfo findOrCreateLockInfo(Object lock) {
        LockInfo info = (LockInfo) sKnown.get(lock);
        if (info != null) {
            return info;
        }
        info = new LockInfo();
        info.label = "0x" + Integer.toHexString(System.identityHashCode(lock)) + " [" + new Throwable().getStackTrace()[2].toString() + "]";
        sKnown.put(lock, info);
        return info;
    }

    public static Object guard(Object lock) {
        if (lock == null || Thread.holdsLock(lock)) {
            return lock;
        }
        int i;
        boolean triggered = false;
        LockInfo info = findOrCreateLockInfo(lock);
        for (i = 0; i < info.children.size(); i++) {
            Object child = info.children.valueAt(i);
            if (child != null && Thread.holdsLock(child)) {
                Slog.w(TAG, "Calling thread " + Thread.currentThread().getName() + " is holding " + lockToString(child) + " while trying to acquire " + lockToString(lock), new Throwable());
                triggered = true;
            }
        }
        if (!triggered) {
            for (i = 0; i < sKnown.size(); i++) {
                Object test = sKnown.keyAt(i);
                if (!(test == null || test == lock || !Thread.holdsLock(test))) {
                    ((LockInfo) sKnown.valueAt(i)).children.add(lock);
                }
            }
        }
        return lock;
    }

    public static void guard(int index) {
        for (int i = 0; i < index; i++) {
            Object lock = sKnownFixed[i];
            if (lock != null && Thread.holdsLock(lock)) {
                Slog.w(TAG, "Calling thread " + Thread.currentThread().getName() + " is holding " + lockToString(i) + " while trying to acquire " + lockToString(index), new Throwable());
            }
        }
    }

    public static Object installLock(Object lock, String label) {
        findOrCreateLockInfo(lock).label = label;
        return lock;
    }

    public static Object installLock(Object lock, int index) {
        sKnownFixed[index] = lock;
        return lock;
    }

    public static Object installNewLock(int index) {
        Object lock = new Object();
        installLock(lock, index);
        return lock;
    }

    private static String lockToString(Object lock) {
        LockInfo info = (LockInfo) sKnown.get(lock);
        if (info != null) {
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
            default:
                return Integer.toString(index);
        }
    }

    public static void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        for (int i = 0; i < sKnown.size(); i++) {
            LockInfo info = (LockInfo) sKnown.valueAt(i);
            pw.println("Lock " + lockToString(sKnown.keyAt(i)) + ":");
            for (int j = 0; j < info.children.size(); j++) {
                pw.println("  Child " + lockToString(info.children.valueAt(j)));
            }
            pw.println();
        }
    }
}
