package com.android.server.usage;

import android.app.PendingIntent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class AppTimeLimitController {
    private static final boolean DEBUG = false;
    private static final long MAX_OBSERVER_PER_UID = 1000;
    private static final long ONE_MINUTE = 60000;
    private static final String TAG = "AppTimeLimitController";
    private final MyHandler mHandler;
    private OnLimitReachedListener mListener;
    private final Lock mLock = new Lock();
    @GuardedBy("mLock")
    private final SparseArray<UserData> mUsers = new SparseArray<>();

    private static class Lock {
        private Lock() {
        }
    }

    private class MyHandler extends Handler {
        static final int MSG_CHECK_TIMEOUT = 1;
        static final int MSG_INFORM_LISTENER = 2;

        MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AppTimeLimitController.this.checkTimeout((TimeLimitGroup) msg.obj);
                    return;
                case 2:
                    AppTimeLimitController.this.informListener((TimeLimitGroup) msg.obj);
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    }

    public interface OnLimitReachedListener {
        void onLimitReached(int i, int i2, long j, long j2, PendingIntent pendingIntent);
    }

    static class TimeLimitGroup {
        PendingIntent callbackIntent;
        String currentPackage;
        int observerId;
        String[] packages;
        int requestingUid;
        long timeCurrentPackageStarted;
        long timeLimit;
        long timeRemaining;
        long timeRequested;
        int userId;

        TimeLimitGroup() {
        }
    }

    private static class UserData {
        /* access modifiers changed from: private */
        public String currentForegroundedPackage;
        /* access modifiers changed from: private */
        public long currentForegroundedTime;
        /* access modifiers changed from: private */
        public SparseArray<TimeLimitGroup> groups;
        /* access modifiers changed from: private */
        public SparseIntArray observerIdCounts;
        /* access modifiers changed from: private */
        public ArrayMap<String, ArrayList<TimeLimitGroup>> packageMap;
        /* access modifiers changed from: private */
        public int userId;

        private UserData(int userId2) {
            this.packageMap = new ArrayMap<>();
            this.groups = new SparseArray<>();
            this.observerIdCounts = new SparseIntArray();
            this.userId = userId2;
        }
    }

    public AppTimeLimitController(OnLimitReachedListener listener, Looper looper) {
        this.mHandler = new MyHandler(looper);
        this.mListener = listener;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public long getUptimeMillis() {
        return SystemClock.uptimeMillis();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public long getObserverPerUidLimit() {
        return 1000;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public long getMinTimeLimit() {
        return 60000;
    }

    private UserData getOrCreateUserDataLocked(int userId) {
        UserData userData = this.mUsers.get(userId);
        if (userData != null) {
            return userData;
        }
        UserData userData2 = new UserData(userId);
        this.mUsers.put(userId, userData2);
        return userData2;
    }

    public void onUserRemoved(int userId) {
        synchronized (this.mLock) {
            this.mUsers.remove(userId);
        }
    }

    public void addObserver(int requestingUid, int observerId, String[] packages, long timeLimit, PendingIntent callbackIntent, int userId) {
        if (timeLimit >= getMinTimeLimit()) {
            synchronized (this.mLock) {
                UserData user = getOrCreateUserDataLocked(userId);
                removeObserverLocked(user, requestingUid, observerId, true);
                int observerIdCount = user.observerIdCounts.get(requestingUid, 0);
                if (((long) observerIdCount) < getObserverPerUidLimit()) {
                    user.observerIdCounts.put(requestingUid, observerIdCount + 1);
                    TimeLimitGroup group = new TimeLimitGroup();
                    group.observerId = observerId;
                    group.callbackIntent = callbackIntent;
                    group.packages = packages;
                    group.timeLimit = timeLimit;
                    group.timeRemaining = group.timeLimit;
                    group.timeRequested = getUptimeMillis();
                    group.requestingUid = requestingUid;
                    group.timeCurrentPackageStarted = -1;
                    group.userId = userId;
                    user.groups.append(observerId, group);
                    addGroupToPackageMapLocked(user, packages, group);
                    if (user.currentForegroundedPackage != null && inPackageList(group.packages, user.currentForegroundedPackage)) {
                        group.timeCurrentPackageStarted = group.timeRequested;
                        group.currentPackage = user.currentForegroundedPackage;
                        if (group.timeRemaining > 0) {
                            postCheckTimeoutLocked(group, group.timeRemaining);
                        }
                    }
                } else {
                    throw new IllegalStateException("Too many observers added by uid " + requestingUid);
                }
            }
            return;
        }
        throw new IllegalArgumentException("Time limit must be >= " + getMinTimeLimit());
    }

    public void removeObserver(int requestingUid, int observerId, int userId) {
        synchronized (this.mLock) {
            removeObserverLocked(getOrCreateUserDataLocked(userId), requestingUid, observerId, false);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public TimeLimitGroup getObserverGroup(int observerId, int userId) {
        TimeLimitGroup timeLimitGroup;
        synchronized (this.mLock) {
            timeLimitGroup = (TimeLimitGroup) getOrCreateUserDataLocked(userId).groups.get(observerId);
        }
        return timeLimitGroup;
    }

    private static boolean inPackageList(String[] packages, String packageName) {
        return ArrayUtils.contains(packages, packageName);
    }

    @GuardedBy("mLock")
    private void removeObserverLocked(UserData user, int requestingUid, int observerId, boolean readding) {
        TimeLimitGroup group = (TimeLimitGroup) user.groups.get(observerId);
        if (group != null && group.requestingUid == requestingUid) {
            removeGroupFromPackageMapLocked(user, group);
            user.groups.remove(observerId);
            this.mHandler.removeMessages(1, group);
            int observerIdCount = user.observerIdCounts.get(requestingUid);
            if (observerIdCount > 1 || readding) {
                user.observerIdCounts.put(requestingUid, observerIdCount - 1);
            } else {
                user.observerIdCounts.delete(requestingUid);
            }
        }
    }

    public void moveToForeground(String packageName, String className, int userId) {
        synchronized (this.mLock) {
            UserData user = getOrCreateUserDataLocked(userId);
            String unused = user.currentForegroundedPackage = packageName;
            long unused2 = user.currentForegroundedTime = getUptimeMillis();
            maybeWatchForPackageLocked(user, packageName, user.currentForegroundedTime);
        }
    }

    public void moveToBackground(String packageName, String className, int userId) {
        int size;
        String str = packageName;
        synchronized (this.mLock) {
            UserData user = getOrCreateUserDataLocked(userId);
            if (!TextUtils.equals(user.currentForegroundedPackage, str)) {
                Slog.w(TAG, "Eh? Last foregrounded package = " + user.currentForegroundedPackage + " and now backgrounded = " + str);
                return;
            }
            long stopTime = getUptimeMillis();
            ArrayList<TimeLimitGroup> groups = (ArrayList) user.packageMap.get(str);
            if (groups != null) {
                int size2 = groups.size();
                int i = 0;
                while (i < size2) {
                    TimeLimitGroup group = groups.get(i);
                    if (group.timeRemaining <= 0) {
                        size = size2;
                    } else {
                        size = size2;
                        group.timeRemaining -= stopTime - Math.max(user.currentForegroundedTime, group.timeRequested);
                        if (group.timeRemaining <= 0) {
                            postInformListenerLocked(group);
                        }
                        group.currentPackage = null;
                        group.timeCurrentPackageStarted = -1;
                        this.mHandler.removeMessages(1, group);
                    }
                    i++;
                    size2 = size;
                }
            }
            String unused = user.currentForegroundedPackage = null;
        }
    }

    private void postInformListenerLocked(TimeLimitGroup group) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, group));
    }

    /* access modifiers changed from: private */
    public void informListener(TimeLimitGroup group) {
        if (this.mListener != null) {
            this.mListener.onLimitReached(group.observerId, group.userId, group.timeLimit, group.timeLimit - group.timeRemaining, group.callbackIntent);
        }
        synchronized (this.mLock) {
            removeObserverLocked(getOrCreateUserDataLocked(group.userId), group.requestingUid, group.observerId, false);
        }
    }

    @GuardedBy("mLock")
    private void maybeWatchForPackageLocked(UserData user, String packageName, long uptimeMillis) {
        ArrayList<TimeLimitGroup> groups = (ArrayList) user.packageMap.get(packageName);
        if (groups != null) {
            int size = groups.size();
            for (int i = 0; i < size; i++) {
                TimeLimitGroup group = groups.get(i);
                if (group.timeRemaining > 0) {
                    group.timeCurrentPackageStarted = uptimeMillis;
                    group.currentPackage = packageName;
                    postCheckTimeoutLocked(group, group.timeRemaining);
                }
            }
        }
    }

    private void addGroupToPackageMapLocked(UserData user, String[] packages, TimeLimitGroup group) {
        for (int i = 0; i < packages.length; i++) {
            ArrayList<TimeLimitGroup> list = (ArrayList) user.packageMap.get(packages[i]);
            if (list == null) {
                list = new ArrayList<>();
                user.packageMap.put(packages[i], list);
            }
            list.add(group);
        }
    }

    private void removeGroupFromPackageMapLocked(UserData user, TimeLimitGroup group) {
        int mapSize = user.packageMap.size();
        for (int i = 0; i < mapSize; i++) {
            ((ArrayList) user.packageMap.valueAt(i)).remove(group);
        }
    }

    private void postCheckTimeoutLocked(TimeLimitGroup group, long timeout) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, group), timeout);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006e, code lost:
        return;
     */
    public void checkTimeout(TimeLimitGroup group) {
        synchronized (this.mLock) {
            UserData user = getOrCreateUserDataLocked(group.userId);
            if (user.groups.get(group.observerId) == group) {
                if (group.timeRemaining > 0) {
                    if (inPackageList(group.packages, user.currentForegroundedPackage)) {
                        if (group.timeCurrentPackageStarted < 0) {
                            Slog.w(TAG, "startTime was not set correctly for " + group);
                        }
                        long timeInForeground = getUptimeMillis() - group.timeCurrentPackageStarted;
                        if (group.timeRemaining <= timeInForeground) {
                            group.timeRemaining -= timeInForeground;
                            postInformListenerLocked(group);
                            group.timeCurrentPackageStarted = -1;
                            group.currentPackage = null;
                        } else {
                            postCheckTimeoutLocked(group, group.timeRemaining - timeInForeground);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("\n  App Time Limits");
            int nUsers = this.mUsers.size();
            for (int i = 0; i < nUsers; i++) {
                UserData user = this.mUsers.valueAt(i);
                pw.print("   User ");
                pw.println(user.userId);
                int nGroups = user.groups.size();
                for (int j = 0; j < nGroups; j++) {
                    TimeLimitGroup group = (TimeLimitGroup) user.groups.valueAt(j);
                    pw.print("    Group id=");
                    pw.print(group.observerId);
                    pw.print(" timeLimit=");
                    pw.print(group.timeLimit);
                    pw.print(" remaining=");
                    pw.print(group.timeRemaining);
                    pw.print(" currentPackage=");
                    pw.print(group.currentPackage);
                    pw.print(" timeCurrentPkgStarted=");
                    pw.print(group.timeCurrentPackageStarted);
                    pw.print(" packages=");
                    pw.println(Arrays.toString(group.packages));
                }
                pw.println();
                pw.print("    currentForegroundedPackage=");
                pw.println(user.currentForegroundedPackage);
            }
        }
    }
}
