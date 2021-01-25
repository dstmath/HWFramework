package com.android.server.am;

import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import com.android.server.AlarmManagerInternal;
import com.android.server.LocalServices;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class BroadcastDispatcher {
    private static final String TAG = "BroadcastDispatcher";
    private AlarmManagerInternal mAlarm;
    private final ArrayList<Deferrals> mAlarmBroadcasts = new ArrayList<>();
    final AlarmManagerInternal.InFlightListener mAlarmListener = new AlarmManagerInternal.InFlightListener() {
        /* class com.android.server.am.BroadcastDispatcher.AnonymousClass1 */

        @Override // com.android.server.AlarmManagerInternal.InFlightListener
        public void broadcastAlarmPending(int recipientUid) {
            synchronized (BroadcastDispatcher.this.mLock) {
                BroadcastDispatcher.this.mAlarmUids.put(recipientUid, BroadcastDispatcher.this.mAlarmUids.get(recipientUid, 0) + 1);
                int numEntries = BroadcastDispatcher.this.mDeferredBroadcasts.size();
                int i = 0;
                while (true) {
                    if (i >= numEntries) {
                        break;
                    } else if (recipientUid == ((Deferrals) BroadcastDispatcher.this.mDeferredBroadcasts.get(i)).uid) {
                        BroadcastDispatcher.this.mAlarmBroadcasts.add((Deferrals) BroadcastDispatcher.this.mDeferredBroadcasts.remove(i));
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }

        @Override // com.android.server.AlarmManagerInternal.InFlightListener
        public void broadcastAlarmComplete(int recipientUid) {
            synchronized (BroadcastDispatcher.this.mLock) {
                int newCount = BroadcastDispatcher.this.mAlarmUids.get(recipientUid, 0) - 1;
                if (newCount >= 0) {
                    BroadcastDispatcher.this.mAlarmUids.put(recipientUid, newCount);
                } else {
                    Slog.wtf(BroadcastDispatcher.TAG, "Undercount of broadcast alarms in flight for " + recipientUid);
                    BroadcastDispatcher.this.mAlarmUids.put(recipientUid, 0);
                }
                if (newCount <= 0) {
                    int numEntries = BroadcastDispatcher.this.mAlarmBroadcasts.size();
                    int i = 0;
                    while (true) {
                        if (i >= numEntries) {
                            break;
                        } else if (recipientUid == ((Deferrals) BroadcastDispatcher.this.mAlarmBroadcasts.get(i)).uid) {
                            BroadcastDispatcher.insertLocked(BroadcastDispatcher.this.mDeferredBroadcasts, (Deferrals) BroadcastDispatcher.this.mAlarmBroadcasts.remove(i));
                            break;
                        } else {
                            i++;
                        }
                    }
                }
            }
        }
    };
    final SparseIntArray mAlarmUids = new SparseIntArray();
    private final BroadcastConstants mConstants;
    private BroadcastRecord mCurrentBroadcast;
    private final ArrayList<Deferrals> mDeferredBroadcasts = new ArrayList<>();
    private final Handler mHandler;
    private final Object mLock;
    private final ArrayList<BroadcastRecord> mOrderedBroadcasts = new ArrayList<>();
    private final BroadcastQueue mQueue;
    private boolean mRecheckScheduled = false;
    final Runnable mScheduleRunnable = new Runnable() {
        /* class com.android.server.am.BroadcastDispatcher.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            synchronized (BroadcastDispatcher.this.mLock) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                    Slog.v(BroadcastDispatcher.TAG, "Deferral recheck of pending broadcasts");
                }
                BroadcastDispatcher.this.mQueue.scheduleBroadcastsLocked();
                BroadcastDispatcher.this.mRecheckScheduled = false;
            }
        }
    };

    /* access modifiers changed from: package-private */
    public static class Deferrals {
        int alarmCount;
        final ArrayList<BroadcastRecord> broadcasts = new ArrayList<>();
        long deferUntil;
        long deferredAt;
        long deferredBy;
        final int uid;

        Deferrals(int uid2, long now, long backoff, int count) {
            this.uid = uid2;
            this.deferredAt = now;
            this.deferredBy = backoff;
            this.deferUntil = now + backoff;
            this.alarmCount = count;
        }

        /* access modifiers changed from: package-private */
        public void add(BroadcastRecord br) {
            this.broadcasts.add(br);
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.broadcasts.size();
        }

        /* access modifiers changed from: package-private */
        public boolean isEmpty() {
            return this.broadcasts.isEmpty();
        }

        /* access modifiers changed from: package-private */
        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            Iterator<BroadcastRecord> it = this.broadcasts.iterator();
            while (it.hasNext()) {
                it.next().writeToProto(proto, fieldId);
            }
        }

        /* access modifiers changed from: package-private */
        public void dumpLocked(Dumper d) {
            Iterator<BroadcastRecord> it = this.broadcasts.iterator();
            while (it.hasNext()) {
                d.dump(it.next());
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Deferrals{uid=");
            sb.append(this.uid);
            sb.append(", deferUntil=");
            sb.append(this.deferUntil);
            sb.append(", #broadcasts=");
            sb.append(this.broadcasts.size());
            sb.append("}");
            return sb.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public class Dumper {
        final String mDumpPackage;
        String mHeading;
        String mLabel;
        boolean mNeedSep = true;
        int mOrdinal;
        boolean mPrinted = false;
        final PrintWriter mPw;
        final String mQueueName;
        final SimpleDateFormat mSdf;

        Dumper(PrintWriter pw, String queueName, String dumpPackage, SimpleDateFormat sdf) {
            this.mPw = pw;
            this.mQueueName = queueName;
            this.mDumpPackage = dumpPackage;
            this.mSdf = sdf;
        }

        /* access modifiers changed from: package-private */
        public void setHeading(String heading) {
            this.mHeading = heading;
            this.mPrinted = false;
        }

        /* access modifiers changed from: package-private */
        public void setLabel(String label) {
            this.mLabel = "  " + label + " " + this.mQueueName + " #";
            this.mOrdinal = 0;
        }

        /* access modifiers changed from: package-private */
        public boolean didPrint() {
            return this.mPrinted;
        }

        /* access modifiers changed from: package-private */
        public void dump(BroadcastRecord br) {
            String str = this.mDumpPackage;
            if (str == null || str.equals(br.callerPackage)) {
                if (!this.mPrinted) {
                    if (this.mNeedSep) {
                        this.mPw.println();
                    }
                    this.mPrinted = true;
                    this.mNeedSep = true;
                    PrintWriter printWriter = this.mPw;
                    printWriter.println("  " + this.mHeading + " [" + this.mQueueName + "]:");
                }
                PrintWriter printWriter2 = this.mPw;
                printWriter2.println(this.mLabel + this.mOrdinal + ":");
                this.mOrdinal = this.mOrdinal + 1;
                br.dump(this.mPw, "    ", this.mSdf);
            }
        }
    }

    public BroadcastDispatcher(BroadcastQueue queue, BroadcastConstants constants, Handler handler, Object lock) {
        this.mQueue = queue;
        this.mConstants = constants;
        this.mHandler = handler;
        this.mLock = lock;
    }

    public void start() {
        this.mAlarm = (AlarmManagerInternal) LocalServices.getService(AlarmManagerInternal.class);
        this.mAlarm.registerInFlightListener(this.mAlarmListener);
    }

    public boolean isEmpty() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mCurrentBroadcast == null && this.mOrderedBroadcasts.isEmpty() && isDeferralsListEmpty(this.mDeferredBroadcasts) && isDeferralsListEmpty(this.mAlarmBroadcasts);
        }
        return z;
    }

    private static int pendingInDeferralsList(ArrayList<Deferrals> list) {
        int pending = 0;
        int numEntries = list.size();
        for (int i = 0; i < numEntries; i++) {
            pending += list.get(i).size();
        }
        return pending;
    }

    private static boolean isDeferralsListEmpty(ArrayList<Deferrals> list) {
        return pendingInDeferralsList(list) == 0;
    }

    public String describeStateLocked() {
        StringBuilder sb = new StringBuilder(128);
        if (this.mCurrentBroadcast != null) {
            sb.append("1 in flight, ");
        }
        sb.append(this.mOrderedBroadcasts.size());
        sb.append(" ordered");
        int n = pendingInDeferralsList(this.mAlarmBroadcasts);
        if (n > 0) {
            sb.append(", ");
            sb.append(n);
            sb.append(" deferrals in alarm recipients");
        }
        int n2 = pendingInDeferralsList(this.mDeferredBroadcasts);
        if (n2 > 0) {
            sb.append(", ");
            sb.append(n2);
            sb.append(" deferred");
        }
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void enqueueOrderedBroadcastLocked(BroadcastRecord r) {
        this.mOrderedBroadcasts.add(r);
    }

    /* access modifiers changed from: package-private */
    public void enqueueOrderedBroadcastLocked(int index, BroadcastRecord r) {
        this.mOrderedBroadcasts.add(index, r);
    }

    /* access modifiers changed from: package-private */
    public BroadcastRecord replaceBroadcastLocked(BroadcastRecord r, String typeForLogging) {
        BroadcastRecord old = replaceBroadcastLocked(this.mOrderedBroadcasts, r, typeForLogging);
        if (old == null) {
            old = replaceDeferredBroadcastLocked(this.mAlarmBroadcasts, r, typeForLogging);
        }
        if (old == null) {
            return replaceDeferredBroadcastLocked(this.mDeferredBroadcasts, r, typeForLogging);
        }
        return old;
    }

    private BroadcastRecord replaceDeferredBroadcastLocked(ArrayList<Deferrals> list, BroadcastRecord r, String typeForLogging) {
        int numEntries = list.size();
        for (int i = 0; i < numEntries; i++) {
            BroadcastRecord old = replaceBroadcastLocked(list.get(i).broadcasts, r, typeForLogging);
            if (old != null) {
                return old;
            }
        }
        return null;
    }

    private BroadcastRecord replaceBroadcastLocked(ArrayList<BroadcastRecord> list, BroadcastRecord r, String typeForLogging) {
        Intent intent = r.intent;
        for (int i = list.size() - 1; i >= 0; i--) {
            BroadcastRecord old = list.get(i);
            if (old.userId == r.userId && intent.filterEquals(old.intent)) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG, "***** Replacing " + typeForLogging + " [" + this.mQueue.mQueueName + "]: " + intent);
                }
                r.deferred = old.deferred;
                list.set(i, r);
                return old;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean cleanupDisabledPackageReceiversLocked(String packageName, Set<String> filterByClasses, int userId, boolean doit) {
        BroadcastRecord broadcastRecord;
        boolean didSomething = cleanupBroadcastListDisabledReceiversLocked(this.mOrderedBroadcasts, packageName, filterByClasses, userId, doit);
        if (doit || !didSomething) {
            didSomething |= cleanupDeferralsListDisabledReceiversLocked(this.mAlarmBroadcasts, packageName, filterByClasses, userId, doit);
        }
        if (doit || !didSomething) {
            didSomething |= cleanupDeferralsListDisabledReceiversLocked(this.mDeferredBroadcasts, packageName, filterByClasses, userId, doit);
        }
        if ((doit || !didSomething) && (broadcastRecord = this.mCurrentBroadcast) != null) {
            return didSomething | broadcastRecord.cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
        }
        return didSomething;
    }

    private boolean cleanupDeferralsListDisabledReceiversLocked(ArrayList<Deferrals> list, String packageName, Set<String> filterByClasses, int userId, boolean doit) {
        boolean didSomething = false;
        Iterator<Deferrals> it = list.iterator();
        while (it.hasNext()) {
            didSomething = cleanupBroadcastListDisabledReceiversLocked(it.next().broadcasts, packageName, filterByClasses, userId, doit);
            if (!doit && didSomething) {
                return true;
            }
        }
        return didSomething;
    }

    private boolean cleanupBroadcastListDisabledReceiversLocked(ArrayList<BroadcastRecord> list, String packageName, Set<String> filterByClasses, int userId, boolean doit) {
        boolean didSomething = false;
        Iterator<BroadcastRecord> it = list.iterator();
        while (it.hasNext()) {
            didSomething |= it.next().cleanupDisabledPackageReceiversLocked(packageName, filterByClasses, userId, doit);
            if (!doit && didSomething) {
                return true;
            }
        }
        return didSomething;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        BroadcastRecord broadcastRecord = this.mCurrentBroadcast;
        if (broadcastRecord != null) {
            broadcastRecord.writeToProto(proto, fieldId);
        }
        Iterator<Deferrals> it = this.mAlarmBroadcasts.iterator();
        while (it.hasNext()) {
            it.next().writeToProto(proto, fieldId);
        }
        Iterator<BroadcastRecord> it2 = this.mOrderedBroadcasts.iterator();
        while (it2.hasNext()) {
            it2.next().writeToProto(proto, fieldId);
        }
        Iterator<Deferrals> it3 = this.mDeferredBroadcasts.iterator();
        while (it3.hasNext()) {
            it3.next().writeToProto(proto, fieldId);
        }
    }

    public BroadcastRecord getActiveBroadcastLocked() {
        return this.mCurrentBroadcast;
    }

    public BroadcastRecord getNextBroadcastLocked(long now) {
        BroadcastRecord broadcastRecord = this.mCurrentBroadcast;
        if (broadcastRecord != null) {
            return broadcastRecord;
        }
        boolean someQueued = !this.mOrderedBroadcasts.isEmpty();
        BroadcastRecord next = null;
        if (!this.mAlarmBroadcasts.isEmpty()) {
            next = popLocked(this.mAlarmBroadcasts);
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL && next != null) {
                Slog.i(TAG, "Next broadcast from alarm targets: " + next);
            }
        }
        if (next == null && !this.mDeferredBroadcasts.isEmpty()) {
            int i = 0;
            while (true) {
                if (i >= this.mDeferredBroadcasts.size()) {
                    break;
                }
                Deferrals d = this.mDeferredBroadcasts.get(i);
                if (now < d.deferUntil && someQueued) {
                    break;
                } else if (d.broadcasts.size() > 0) {
                    next = d.broadcasts.remove(0);
                    this.mDeferredBroadcasts.remove(i);
                    d.deferredBy = calculateDeferral(d.deferredBy);
                    d.deferUntil += d.deferredBy;
                    insertLocked(this.mDeferredBroadcasts, d);
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                        Slog.i(TAG, "Next broadcast from deferrals " + next + ", deferUntil now " + d.deferUntil);
                    }
                } else {
                    i++;
                }
            }
        }
        if (next == null && someQueued) {
            next = this.mOrderedBroadcasts.remove(0);
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                Slog.i(TAG, "Next broadcast from main queue: " + next);
            }
        }
        this.mCurrentBroadcast = next;
        return next;
    }

    public void retireBroadcastLocked(BroadcastRecord r) {
        if (r != this.mCurrentBroadcast) {
            Slog.wtf(TAG, "Retiring broadcast " + r + " doesn't match current outgoing " + this.mCurrentBroadcast);
        }
        this.mCurrentBroadcast = null;
    }

    public boolean isDeferringLocked(int uid) {
        Deferrals d = findUidLocked(uid);
        if (d != null && d.broadcasts.isEmpty() && SystemClock.uptimeMillis() >= d.deferUntil) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                Slog.i(TAG, "No longer deferring broadcasts to uid " + d.uid);
            }
            removeDeferral(d);
            return false;
        } else if (d != null) {
            return true;
        } else {
            return false;
        }
    }

    public void startDeferring(int uid) {
        synchronized (this.mLock) {
            Deferrals d = findUidLocked(uid);
            if (d == null) {
                Deferrals d2 = new Deferrals(uid, SystemClock.uptimeMillis(), this.mConstants.DEFERRAL, this.mAlarmUids.get(uid, 0));
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                    Slog.i(TAG, "Now deferring broadcasts to " + uid + " until " + d2.deferUntil);
                }
                if (d2.alarmCount == 0) {
                    insertLocked(this.mDeferredBroadcasts, d2);
                    scheduleDeferralCheckLocked(true);
                } else {
                    this.mAlarmBroadcasts.add(d2);
                }
            } else {
                d.deferredBy = this.mConstants.DEFERRAL;
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                    Slog.i(TAG, "Uid " + uid + " slow again, deferral interval reset to " + d.deferredBy);
                }
            }
        }
    }

    public void addDeferredBroadcast(int uid, BroadcastRecord br) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
            Slog.i(TAG, "Enqueuing deferred broadcast " + br);
        }
        synchronized (this.mLock) {
            Deferrals d = findUidLocked(uid);
            if (d == null) {
                Slog.wtf(TAG, "Adding deferred broadcast but not tracking " + uid);
            } else if (br == null) {
                Slog.wtf(TAG, "Deferring null broadcast to " + uid);
            } else {
                br.deferred = true;
                d.add(br);
            }
        }
    }

    public void scheduleDeferralCheckLocked(boolean force) {
        if ((force || !this.mRecheckScheduled) && !this.mDeferredBroadcasts.isEmpty()) {
            Deferrals d = this.mDeferredBroadcasts.get(0);
            if (!d.broadcasts.isEmpty()) {
                this.mHandler.removeCallbacks(this.mScheduleRunnable);
                this.mHandler.postAtTime(this.mScheduleRunnable, d.deferUntil);
                this.mRecheckScheduled = true;
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL) {
                    Slog.i(TAG, "Scheduling deferred broadcast recheck at " + d.deferUntil);
                }
            }
        }
    }

    public void cancelDeferralsLocked() {
        zeroDeferralTimes(this.mAlarmBroadcasts);
        zeroDeferralTimes(this.mDeferredBroadcasts);
    }

    private static void zeroDeferralTimes(ArrayList<Deferrals> list) {
        int num = list.size();
        for (int i = 0; i < num; i++) {
            Deferrals d = list.get(i);
            d.deferredBy = 0;
            d.deferUntil = 0;
        }
    }

    private Deferrals findUidLocked(int uid) {
        Deferrals d = findUidLocked(uid, this.mDeferredBroadcasts);
        if (d == null) {
            return findUidLocked(uid, this.mAlarmBroadcasts);
        }
        return d;
    }

    private boolean removeDeferral(Deferrals d) {
        boolean didRemove = this.mDeferredBroadcasts.remove(d);
        if (!didRemove) {
            return this.mAlarmBroadcasts.remove(d);
        }
        return didRemove;
    }

    private static Deferrals findUidLocked(int uid, ArrayList<Deferrals> list) {
        int numElements = list.size();
        for (int i = 0; i < numElements; i++) {
            Deferrals d = list.get(i);
            if (uid == d.uid) {
                return d;
            }
        }
        return null;
    }

    private static BroadcastRecord popLocked(ArrayList<Deferrals> list) {
        Deferrals d = list.get(0);
        if (d.broadcasts.isEmpty()) {
            return null;
        }
        return d.broadcasts.remove(0);
    }

    /* access modifiers changed from: private */
    public static void insertLocked(ArrayList<Deferrals> list, Deferrals d) {
        int numElements = list.size();
        int i = 0;
        while (i < numElements && d.deferUntil >= list.get(i).deferUntil) {
            i++;
        }
        list.add(i, d);
    }

    private long calculateDeferral(long previous) {
        return Math.max(this.mConstants.DEFERRAL_FLOOR, (long) (((float) previous) * this.mConstants.DEFERRAL_DECAY_FACTOR));
    }

    /* access modifiers changed from: package-private */
    public boolean dumpLocked(PrintWriter pw, String dumpPackage, String queueName, SimpleDateFormat sdf) {
        Dumper dumper = new Dumper(pw, queueName, dumpPackage, sdf);
        dumper.setHeading("Currently in flight");
        dumper.setLabel("In-Flight Ordered Broadcast");
        BroadcastRecord broadcastRecord = this.mCurrentBroadcast;
        if (broadcastRecord != null) {
            dumper.dump(broadcastRecord);
        } else {
            pw.println("  (null)");
        }
        dumper.setHeading("Active ordered broadcasts");
        dumper.setLabel("Active Ordered Broadcast");
        Iterator<Deferrals> it = this.mAlarmBroadcasts.iterator();
        while (it.hasNext()) {
            it.next().dumpLocked(dumper);
        }
        boolean printed = false | dumper.didPrint();
        Iterator<BroadcastRecord> it2 = this.mOrderedBroadcasts.iterator();
        while (it2.hasNext()) {
            dumper.dump(it2.next());
        }
        boolean printed2 = printed | dumper.didPrint();
        dumper.setHeading("Deferred ordered broadcasts");
        dumper.setLabel("Deferred Ordered Broadcast");
        Iterator<Deferrals> it3 = this.mDeferredBroadcasts.iterator();
        while (it3.hasNext()) {
            it3.next().dumpLocked(dumper);
        }
        return printed2 | dumper.didPrint();
    }

    /* access modifiers changed from: package-private */
    public int getOrderedBroadcastsSize() {
        return this.mOrderedBroadcasts.size();
    }
}
