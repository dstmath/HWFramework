package com.android.server.usage;

import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public class AppTimeLimitController {
    private static final boolean DEBUG = false;
    private static final long MAX_OBSERVER_PER_UID = 1000;
    private static final Integer ONE = new Integer(1);
    private static final long ONE_MINUTE = 60000;
    private static final String TAG = "AppTimeLimitController";
    private final MyHandler mHandler;
    private TimeLimitCallbackListener mListener;
    private final Lock mLock = new Lock();
    @GuardedBy({"mLock"})
    private final SparseArray<ObserverAppData> mObserverApps = new SparseArray<>();
    @GuardedBy({"mLock"})
    private final SparseArray<UserData> mUsers = new SparseArray<>();

    public interface TimeLimitCallbackListener {
        void onLimitReached(int i, int i2, long j, long j2, PendingIntent pendingIntent);

        void onSessionEnd(int i, int i2, long j, PendingIntent pendingIntent);
    }

    /* access modifiers changed from: private */
    public static class Lock {
        private Lock() {
        }
    }

    /* access modifiers changed from: private */
    public class UserData {
        public final ArrayMap<String, Integer> currentlyActive;
        public final ArrayMap<String, ArrayList<UsageGroup>> observedMap;
        private int userId;

        private UserData(int userId2) {
            this.currentlyActive = new ArrayMap<>();
            this.observedMap = new ArrayMap<>();
            this.userId = userId2;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public boolean isActive(String[] entities) {
            for (String str : entities) {
                if (this.currentlyActive.containsKey(str)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void addUsageGroup(UsageGroup group) {
            int size = group.mObserved.length;
            for (int i = 0; i < size; i++) {
                ArrayList<UsageGroup> list = this.observedMap.get(group.mObserved[i]);
                if (list == null) {
                    list = new ArrayList<>();
                    this.observedMap.put(group.mObserved[i], list);
                }
                list.add(group);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void removeUsageGroup(UsageGroup group) {
            int size = group.mObserved.length;
            for (int i = 0; i < size; i++) {
                String observed = group.mObserved[i];
                ArrayList<UsageGroup> list = this.observedMap.get(observed);
                if (list != null) {
                    list.remove(group);
                    if (list.isEmpty()) {
                        this.observedMap.remove(observed);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void dump(PrintWriter pw) {
            pw.print(" userId=");
            pw.println(this.userId);
            pw.print(" Currently Active:");
            int nActive = this.currentlyActive.size();
            for (int i = 0; i < nActive; i++) {
                pw.print(this.currentlyActive.keyAt(i));
                pw.print(", ");
            }
            pw.println();
            pw.print(" Observed Entities:");
            int nEntities = this.observedMap.size();
            for (int i2 = 0; i2 < nEntities; i2++) {
                pw.print(this.observedMap.keyAt(i2));
                pw.print(", ");
            }
            pw.println();
        }
    }

    /* access modifiers changed from: private */
    public class ObserverAppData {
        SparseArray<AppUsageGroup> appUsageGroups;
        SparseArray<AppUsageLimitGroup> appUsageLimitGroups;
        SparseArray<SessionUsageGroup> sessionUsageGroups;
        private int uid;

        private ObserverAppData(int uid2) {
            this.appUsageGroups = new SparseArray<>();
            this.sessionUsageGroups = new SparseArray<>();
            this.appUsageLimitGroups = new SparseArray<>();
            this.uid = uid2;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void removeAppUsageGroup(int observerId) {
            this.appUsageGroups.remove(observerId);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void removeSessionUsageGroup(int observerId) {
            this.sessionUsageGroups.remove(observerId);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void removeAppUsageLimitGroup(int observerId) {
            this.appUsageLimitGroups.remove(observerId);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void dump(PrintWriter pw) {
            pw.print(" uid=");
            pw.println(this.uid);
            pw.println("    App Usage Groups:");
            int nAppUsageGroups = this.appUsageGroups.size();
            for (int i = 0; i < nAppUsageGroups; i++) {
                this.appUsageGroups.valueAt(i).dump(pw);
                pw.println();
            }
            pw.println("    Session Usage Groups:");
            int nSessionUsageGroups = this.sessionUsageGroups.size();
            for (int i2 = 0; i2 < nSessionUsageGroups; i2++) {
                this.sessionUsageGroups.valueAt(i2).dump(pw);
                pw.println();
            }
            pw.println("    App Usage Limit Groups:");
            int nAppUsageLimitGroups = this.appUsageLimitGroups.size();
            for (int i3 = 0; i3 < nAppUsageLimitGroups; i3++) {
                this.appUsageLimitGroups.valueAt(i3).dump(pw);
                pw.println();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public abstract class UsageGroup {
        protected int mActives;
        protected long mLastKnownUsageTimeMs;
        protected long mLastUsageEndTimeMs;
        protected PendingIntent mLimitReachedCallback;
        protected String[] mObserved;
        protected WeakReference<ObserverAppData> mObserverAppRef;
        protected int mObserverId;
        protected long mTimeLimitMs;
        protected long mUsageTimeMs;
        protected WeakReference<UserData> mUserRef;

        UsageGroup(UserData user, ObserverAppData observerApp, int observerId, String[] observed, long timeLimitMs, PendingIntent limitReachedCallback) {
            this.mUserRef = new WeakReference<>(user);
            this.mObserverAppRef = new WeakReference<>(observerApp);
            this.mObserverId = observerId;
            this.mObserved = observed;
            this.mTimeLimitMs = timeLimitMs;
            this.mLimitReachedCallback = limitReachedCallback;
        }

        @GuardedBy({"mLock"})
        public long getTimeLimitMs() {
            return this.mTimeLimitMs;
        }

        @GuardedBy({"mLock"})
        public long getUsageTimeMs() {
            return this.mUsageTimeMs;
        }

        @GuardedBy({"mLock"})
        public void remove() {
            UserData user = this.mUserRef.get();
            if (user != null) {
                user.removeUsageGroup(this);
            }
            this.mLimitReachedCallback = null;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void noteUsageStart(long startTimeMs) {
            noteUsageStart(startTimeMs, startTimeMs);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void noteUsageStart(long startTimeMs, long currentTimeMs) {
            int i = this.mActives;
            this.mActives = i + 1;
            if (i == 0) {
                long startTimeMs2 = this.mLastUsageEndTimeMs;
                if (startTimeMs2 <= startTimeMs) {
                    startTimeMs2 = startTimeMs;
                }
                this.mLastKnownUsageTimeMs = startTimeMs2;
                long timeRemaining = ((this.mTimeLimitMs - this.mUsageTimeMs) - currentTimeMs) + startTimeMs2;
                if (timeRemaining > 0) {
                    AppTimeLimitController.this.postCheckTimeoutLocked(this, timeRemaining);
                    return;
                }
                return;
            }
            int i2 = this.mActives;
            String[] strArr = this.mObserved;
            if (i2 > strArr.length) {
                this.mActives = strArr.length;
                UserData user = this.mUserRef.get();
                if (user != null) {
                    Object[] array = user.currentlyActive.keySet().toArray();
                    Slog.e(AppTimeLimitController.TAG, "Too many noted usage starts! Observed entities: " + Arrays.toString(this.mObserved) + "   Active Entities: " + Arrays.toString(array));
                }
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void noteUsageStop(long stopTimeMs) {
            boolean limitNotCrossed = true;
            int i = this.mActives - 1;
            this.mActives = i;
            if (i == 0) {
                if (this.mUsageTimeMs >= this.mTimeLimitMs) {
                    limitNotCrossed = false;
                }
                this.mUsageTimeMs += stopTimeMs - this.mLastKnownUsageTimeMs;
                this.mLastUsageEndTimeMs = stopTimeMs;
                if (limitNotCrossed && this.mUsageTimeMs >= this.mTimeLimitMs) {
                    AppTimeLimitController.this.postInformLimitReachedListenerLocked(this);
                }
                AppTimeLimitController.this.cancelCheckTimeoutLocked(this);
            } else if (this.mActives < 0) {
                this.mActives = 0;
                UserData user = this.mUserRef.get();
                if (user != null) {
                    Object[] array = user.currentlyActive.keySet().toArray();
                    Slog.e(AppTimeLimitController.TAG, "Too many noted usage stops! Observed entities: " + Arrays.toString(this.mObserved) + "   Active Entities: " + Arrays.toString(array));
                }
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void checkTimeout(long currentTimeMs) {
            UserData user = this.mUserRef.get();
            if (user != null) {
                long timeRemainingMs = this.mTimeLimitMs - this.mUsageTimeMs;
                if (timeRemainingMs > 0 && user.isActive(this.mObserved)) {
                    long timeUsedMs = currentTimeMs - this.mLastKnownUsageTimeMs;
                    if (timeRemainingMs <= timeUsedMs) {
                        this.mUsageTimeMs += timeUsedMs;
                        this.mLastKnownUsageTimeMs = currentTimeMs;
                        AppTimeLimitController.this.postInformLimitReachedListenerLocked(this);
                        return;
                    }
                    AppTimeLimitController.this.postCheckTimeoutLocked(this, timeRemainingMs - timeUsedMs);
                }
            }
        }

        @GuardedBy({"mLock"})
        public void onLimitReached() {
            UserData user = this.mUserRef.get();
            if (user != null && AppTimeLimitController.this.mListener != null) {
                AppTimeLimitController.this.mListener.onLimitReached(this.mObserverId, user.userId, this.mTimeLimitMs, this.mUsageTimeMs, this.mLimitReachedCallback);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void dump(PrintWriter pw) {
            pw.print("        Group id=");
            pw.print(this.mObserverId);
            pw.print(" timeLimit=");
            pw.print(this.mTimeLimitMs);
            pw.print(" used=");
            pw.print(this.mUsageTimeMs);
            pw.print(" lastKnownUsage=");
            pw.print(this.mLastKnownUsageTimeMs);
            pw.print(" mActives=");
            pw.print(this.mActives);
            pw.print(" observed=");
            pw.print(Arrays.toString(this.mObserved));
        }
    }

    /* access modifiers changed from: package-private */
    public class AppUsageGroup extends UsageGroup {
        public AppUsageGroup(UserData user, ObserverAppData observerApp, int observerId, String[] observed, long timeLimitMs, PendingIntent limitReachedCallback) {
            super(user, observerApp, observerId, observed, timeLimitMs, limitReachedCallback);
        }

        @Override // com.android.server.usage.AppTimeLimitController.UsageGroup
        @GuardedBy({"mLock"})
        public void remove() {
            super.remove();
            ObserverAppData observerApp = (ObserverAppData) this.mObserverAppRef.get();
            if (observerApp != null) {
                observerApp.removeAppUsageGroup(this.mObserverId);
            }
        }

        @Override // com.android.server.usage.AppTimeLimitController.UsageGroup
        @GuardedBy({"mLock"})
        public void onLimitReached() {
            super.onLimitReached();
            remove();
        }
    }

    /* access modifiers changed from: package-private */
    public class SessionUsageGroup extends UsageGroup {
        private long mNewSessionThresholdMs;
        private PendingIntent mSessionEndCallback;

        public SessionUsageGroup(UserData user, ObserverAppData observerApp, int observerId, String[] observed, long timeLimitMs, PendingIntent limitReachedCallback, long newSessionThresholdMs, PendingIntent sessionEndCallback) {
            super(user, observerApp, observerId, observed, timeLimitMs, limitReachedCallback);
            this.mNewSessionThresholdMs = newSessionThresholdMs;
            this.mSessionEndCallback = sessionEndCallback;
        }

        @Override // com.android.server.usage.AppTimeLimitController.UsageGroup
        @GuardedBy({"mLock"})
        public void remove() {
            super.remove();
            ObserverAppData observerApp = (ObserverAppData) this.mObserverAppRef.get();
            if (observerApp != null) {
                observerApp.removeSessionUsageGroup(this.mObserverId);
            }
            this.mSessionEndCallback = null;
        }

        @Override // com.android.server.usage.AppTimeLimitController.UsageGroup
        @GuardedBy({"mLock"})
        public void noteUsageStart(long startTimeMs, long currentTimeMs) {
            if (this.mActives == 0) {
                if (startTimeMs - this.mLastUsageEndTimeMs > this.mNewSessionThresholdMs) {
                    this.mUsageTimeMs = 0;
                }
                AppTimeLimitController.this.cancelInformSessionEndListener(this);
            }
            super.noteUsageStart(startTimeMs, currentTimeMs);
        }

        @Override // com.android.server.usage.AppTimeLimitController.UsageGroup
        @GuardedBy({"mLock"})
        public void noteUsageStop(long stopTimeMs) {
            super.noteUsageStop(stopTimeMs);
            if (this.mActives == 0 && this.mUsageTimeMs >= this.mTimeLimitMs) {
                AppTimeLimitController.this.postInformSessionEndListenerLocked(this, this.mNewSessionThresholdMs);
            }
        }

        @GuardedBy({"mLock"})
        public void onSessionEnd() {
            UserData user = (UserData) this.mUserRef.get();
            if (user != null && AppTimeLimitController.this.mListener != null) {
                AppTimeLimitController.this.mListener.onSessionEnd(this.mObserverId, user.userId, this.mUsageTimeMs, this.mSessionEndCallback);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.usage.AppTimeLimitController.UsageGroup
        @GuardedBy({"mLock"})
        public void dump(PrintWriter pw) {
            super.dump(pw);
            pw.print(" lastUsageEndTime=");
            pw.print(this.mLastUsageEndTimeMs);
            pw.print(" newSessionThreshold=");
            pw.print(this.mNewSessionThresholdMs);
        }
    }

    /* access modifiers changed from: package-private */
    public class AppUsageLimitGroup extends UsageGroup {
        public AppUsageLimitGroup(UserData user, ObserverAppData observerApp, int observerId, String[] observed, long timeLimitMs, long timeUsedMs, PendingIntent limitReachedCallback) {
            super(user, observerApp, observerId, observed, timeLimitMs, limitReachedCallback);
            this.mUsageTimeMs = timeUsedMs;
        }

        @Override // com.android.server.usage.AppTimeLimitController.UsageGroup
        @GuardedBy({"mLock"})
        public void remove() {
            super.remove();
            ObserverAppData observerApp = (ObserverAppData) this.mObserverAppRef.get();
            if (observerApp != null) {
                observerApp.removeAppUsageLimitGroup(this.mObserverId);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public long getTotaUsageLimit() {
            return this.mTimeLimitMs;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public long getUsageRemaining() {
            if (this.mActives > 0) {
                return (this.mTimeLimitMs - this.mUsageTimeMs) - (AppTimeLimitController.this.getUptimeMillis() - this.mLastKnownUsageTimeMs);
            }
            return this.mTimeLimitMs - this.mUsageTimeMs;
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        static final int MSG_CHECK_TIMEOUT = 1;
        static final int MSG_INFORM_LIMIT_REACHED_LISTENER = 2;
        static final int MSG_INFORM_SESSION_END = 3;

        MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                synchronized (AppTimeLimitController.this.mLock) {
                    ((UsageGroup) msg.obj).checkTimeout(AppTimeLimitController.this.getUptimeMillis());
                }
            } else if (i == 2) {
                synchronized (AppTimeLimitController.this.mLock) {
                    ((UsageGroup) msg.obj).onLimitReached();
                }
            } else if (i != 3) {
                super.handleMessage(msg);
            } else {
                synchronized (AppTimeLimitController.this.mLock) {
                    ((SessionUsageGroup) msg.obj).onSessionEnd();
                }
            }
        }
    }

    public AppTimeLimitController(TimeLimitCallbackListener listener, Looper looper) {
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
    public long getAppUsageObserverPerUidLimit() {
        return 1000;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public long getUsageSessionObserverPerUidLimit() {
        return 1000;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public long getAppUsageLimitObserverPerUidLimit() {
        return 1000;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public long getMinTimeLimit() {
        return 60000;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public AppUsageGroup getAppUsageGroup(int observerAppUid, int observerId) {
        AppUsageGroup appUsageGroup;
        synchronized (this.mLock) {
            appUsageGroup = getOrCreateObserverAppDataLocked(observerAppUid).appUsageGroups.get(observerId);
        }
        return appUsageGroup;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public SessionUsageGroup getSessionUsageGroup(int observerAppUid, int observerId) {
        SessionUsageGroup sessionUsageGroup;
        synchronized (this.mLock) {
            sessionUsageGroup = getOrCreateObserverAppDataLocked(observerAppUid).sessionUsageGroups.get(observerId);
        }
        return sessionUsageGroup;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public AppUsageLimitGroup getAppUsageLimitGroup(int observerAppUid, int observerId) {
        AppUsageLimitGroup appUsageLimitGroup;
        synchronized (this.mLock) {
            appUsageLimitGroup = getOrCreateObserverAppDataLocked(observerAppUid).appUsageLimitGroups.get(observerId);
        }
        return appUsageLimitGroup;
    }

    public UsageStatsManagerInternal.AppUsageLimitData getAppUsageLimit(String packageName, UserHandle user) {
        synchronized (this.mLock) {
            UserData userData = getOrCreateUserDataLocked(user.getIdentifier());
            if (userData == null) {
                return null;
            }
            ArrayList<UsageGroup> usageGroups = userData.observedMap.get(packageName);
            if (usageGroups != null) {
                if (!usageGroups.isEmpty()) {
                    ArraySet<AppUsageLimitGroup> usageLimitGroups = new ArraySet<>();
                    for (int i = 0; i < usageGroups.size(); i++) {
                        if (usageGroups.get(i) instanceof AppUsageLimitGroup) {
                            AppUsageLimitGroup group = (AppUsageLimitGroup) usageGroups.get(i);
                            int j = 0;
                            while (true) {
                                if (j >= group.mObserved.length) {
                                    break;
                                } else if (group.mObserved[j].equals(packageName)) {
                                    usageLimitGroups.add(group);
                                    break;
                                } else {
                                    j++;
                                }
                            }
                        }
                    }
                    if (usageLimitGroups.isEmpty()) {
                        return null;
                    }
                    AppUsageLimitGroup smallestGroup = usageLimitGroups.valueAt(0);
                    for (int i2 = 1; i2 < usageLimitGroups.size(); i2++) {
                        AppUsageLimitGroup otherGroup = usageLimitGroups.valueAt(i2);
                        if (otherGroup.getUsageRemaining() < smallestGroup.getUsageRemaining()) {
                            smallestGroup = otherGroup;
                        }
                    }
                    return new UsageStatsManagerInternal.AppUsageLimitData(smallestGroup.getTotaUsageLimit(), smallestGroup.getUsageRemaining());
                }
            }
            return null;
        }
    }

    @GuardedBy({"mLock"})
    private UserData getOrCreateUserDataLocked(int userId) {
        UserData userData = this.mUsers.get(userId);
        if (userData != null) {
            return userData;
        }
        UserData userData2 = new UserData(userId);
        this.mUsers.put(userId, userData2);
        return userData2;
    }

    @GuardedBy({"mLock"})
    private ObserverAppData getOrCreateObserverAppDataLocked(int uid) {
        ObserverAppData appData = this.mObserverApps.get(uid);
        if (appData != null) {
            return appData;
        }
        ObserverAppData appData2 = new ObserverAppData(uid);
        this.mObserverApps.put(uid, appData2);
        return appData2;
    }

    public void onUserRemoved(int userId) {
        synchronized (this.mLock) {
            this.mUsers.remove(userId);
        }
    }

    @GuardedBy({"mLock"})
    private void noteActiveLocked(UserData user, UsageGroup group, long currentTimeMs) {
        int size = group.mObserved.length;
        for (int i = 0; i < size; i++) {
            if (user.currentlyActive.containsKey(group.mObserved[i])) {
                group.noteUsageStart(currentTimeMs);
            }
        }
    }

    public void addAppUsageObserver(int requestingUid, int observerId, String[] observed, long timeLimit, PendingIntent callbackIntent, int userId) {
        if (timeLimit >= getMinTimeLimit()) {
            synchronized (this.mLock) {
                try {
                    UserData user = getOrCreateUserDataLocked(userId);
                    ObserverAppData observerApp = getOrCreateObserverAppDataLocked(requestingUid);
                    AppUsageGroup group = observerApp.appUsageGroups.get(observerId);
                    if (group != null) {
                        group.remove();
                    }
                    if (((long) observerApp.appUsageGroups.size()) < getAppUsageObserverPerUidLimit()) {
                        AppUsageGroup group2 = new AppUsageGroup(user, observerApp, observerId, observed, timeLimit, callbackIntent);
                        observerApp.appUsageGroups.append(observerId, group2);
                        user.addUsageGroup(group2);
                        noteActiveLocked(user, group2, getUptimeMillis());
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("Too many app usage observers added by uid ");
                    sb.append(requestingUid);
                    throw new IllegalStateException(sb.toString());
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("Time limit must be >= " + getMinTimeLimit());
        }
    }

    public void removeAppUsageObserver(int requestingUid, int observerId, int userId) {
        synchronized (this.mLock) {
            AppUsageGroup group = getOrCreateObserverAppDataLocked(requestingUid).appUsageGroups.get(observerId);
            if (group != null) {
                group.remove();
            }
        }
    }

    public void addUsageSessionObserver(int requestingUid, int observerId, String[] observed, long timeLimit, long sessionThresholdTime, PendingIntent limitReachedCallbackIntent, PendingIntent sessionEndCallbackIntent, int userId) {
        if (timeLimit >= getMinTimeLimit()) {
            synchronized (this.mLock) {
                try {
                    UserData user = getOrCreateUserDataLocked(userId);
                    ObserverAppData observerApp = getOrCreateObserverAppDataLocked(requestingUid);
                    SessionUsageGroup group = observerApp.sessionUsageGroups.get(observerId);
                    if (group != null) {
                        group.remove();
                    }
                    if (((long) observerApp.sessionUsageGroups.size()) < getUsageSessionObserverPerUidLimit()) {
                        try {
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                        try {
                            SessionUsageGroup group2 = new SessionUsageGroup(user, observerApp, observerId, observed, timeLimit, limitReachedCallbackIntent, sessionThresholdTime, sessionEndCallbackIntent);
                            observerApp.sessionUsageGroups.append(observerId, group2);
                            user.addUsageGroup(group2);
                            noteActiveLocked(user, group2, getUptimeMillis());
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Too many app usage observers added by uid ");
                        sb.append(requestingUid);
                        throw new IllegalStateException(sb.toString());
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("Time limit must be >= " + getMinTimeLimit());
        }
    }

    public void removeUsageSessionObserver(int requestingUid, int observerId, int userId) {
        synchronized (this.mLock) {
            SessionUsageGroup group = getOrCreateObserverAppDataLocked(requestingUid).sessionUsageGroups.get(observerId);
            if (group != null) {
                group.remove();
            }
        }
    }

    public void addAppUsageLimitObserver(int requestingUid, int observerId, String[] observed, long timeLimit, long timeUsed, PendingIntent callbackIntent, int userId) {
        if (timeLimit >= getMinTimeLimit()) {
            synchronized (this.mLock) {
                try {
                    UserData user = getOrCreateUserDataLocked(userId);
                    ObserverAppData observerApp = getOrCreateObserverAppDataLocked(requestingUid);
                    AppUsageLimitGroup group = observerApp.appUsageLimitGroups.get(observerId);
                    if (group != null) {
                        group.remove();
                    }
                    if (((long) observerApp.appUsageLimitGroups.size()) < getAppUsageLimitObserverPerUidLimit()) {
                        AppUsageLimitGroup group2 = new AppUsageLimitGroup(user, observerApp, observerId, observed, timeLimit, timeUsed, timeUsed >= timeLimit ? null : callbackIntent);
                        observerApp.appUsageLimitGroups.append(observerId, group2);
                        user.addUsageGroup(group2);
                        noteActiveLocked(user, group2, getUptimeMillis());
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("Too many app usage observers added by uid ");
                    sb.append(requestingUid);
                    throw new IllegalStateException(sb.toString());
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("Time limit must be >= " + getMinTimeLimit());
        }
    }

    public void removeAppUsageLimitObserver(int requestingUid, int observerId, int userId) {
        synchronized (this.mLock) {
            AppUsageLimitGroup group = getOrCreateObserverAppDataLocked(requestingUid).appUsageLimitGroups.get(observerId);
            if (group != null) {
                group.remove();
            }
        }
    }

    public void noteUsageStart(String name, int userId, long timeAgoMs) throws IllegalArgumentException {
        Integer count;
        synchronized (this.mLock) {
            UserData user = getOrCreateUserDataLocked(userId);
            int index = user.currentlyActive.indexOfKey(name);
            if (index < 0 || (count = user.currentlyActive.valueAt(index)) == null) {
                long currentTime = getUptimeMillis();
                user.currentlyActive.put(name, ONE);
                ArrayList<UsageGroup> groups = user.observedMap.get(name);
                if (groups != null) {
                    int size = groups.size();
                    for (int i = 0; i < size; i++) {
                        groups.get(i).noteUsageStart(currentTime - timeAgoMs, currentTime);
                    }
                    return;
                }
                return;
            }
            user.currentlyActive.setValueAt(index, Integer.valueOf(count.intValue() + 1));
        }
    }

    public void noteUsageStart(String name, int userId) throws IllegalArgumentException {
        noteUsageStart(name, userId, 0);
    }

    public void noteUsageStop(String name, int userId) throws IllegalArgumentException {
        synchronized (this.mLock) {
            UserData user = getOrCreateUserDataLocked(userId);
            int index = user.currentlyActive.indexOfKey(name);
            if (index >= 0) {
                Integer count = user.currentlyActive.valueAt(index);
                if (!count.equals(ONE)) {
                    user.currentlyActive.setValueAt(index, Integer.valueOf(count.intValue() - 1));
                    return;
                }
                user.currentlyActive.removeAt(index);
                long currentTime = getUptimeMillis();
                ArrayList<UsageGroup> groups = user.observedMap.get(name);
                if (groups != null) {
                    int size = groups.size();
                    for (int i = 0; i < size; i++) {
                        groups.get(i).noteUsageStop(currentTime);
                    }
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("Unable to stop usage for " + name + ", not in use");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void postInformLimitReachedListenerLocked(UsageGroup group) {
        MyHandler myHandler = this.mHandler;
        myHandler.sendMessage(myHandler.obtainMessage(2, group));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void postInformSessionEndListenerLocked(SessionUsageGroup group, long timeout) {
        MyHandler myHandler = this.mHandler;
        myHandler.sendMessageDelayed(myHandler.obtainMessage(3, group), timeout);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void cancelInformSessionEndListener(SessionUsageGroup group) {
        this.mHandler.removeMessages(3, group);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void postCheckTimeoutLocked(UsageGroup group, long timeout) {
        MyHandler myHandler = this.mHandler;
        myHandler.sendMessageDelayed(myHandler.obtainMessage(1, group), timeout);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void cancelCheckTimeoutLocked(UsageGroup group) {
        this.mHandler.removeMessages(1, group);
    }

    /* access modifiers changed from: package-private */
    public void dump(String[] args, PrintWriter pw) {
        if (args != null) {
            for (String arg : args) {
                if ("actives".equals(arg)) {
                    synchronized (this.mLock) {
                        int nUsers = this.mUsers.size();
                        for (int user = 0; user < nUsers; user++) {
                            ArrayMap<String, Integer> actives = this.mUsers.valueAt(user).currentlyActive;
                            int nActive = actives.size();
                            for (int active = 0; active < nActive; active++) {
                                pw.println(actives.keyAt(active));
                            }
                        }
                    }
                    return;
                }
            }
        }
        synchronized (this.mLock) {
            pw.println("\n  App Time Limits");
            int nUsers2 = this.mUsers.size();
            for (int i = 0; i < nUsers2; i++) {
                pw.print("   User ");
                this.mUsers.valueAt(i).dump(pw);
            }
            pw.println();
            int nObserverApps = this.mObserverApps.size();
            for (int i2 = 0; i2 < nObserverApps; i2++) {
                pw.print("   Observer App ");
                this.mObserverApps.valueAt(i2).dump(pw);
            }
        }
    }
}
