package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.os.IResultReceiver;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.pm.DumpState;
import com.android.server.wm.SafeActivityOptions;
import com.android.server.wm.TaskRecord;
import com.huawei.android.app.HwActivityTaskManager;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Objects;

public final class PendingIntentRecord extends IIntentSender.Stub {
    public static final int FLAG_ACTIVITY_SENDER = 1;
    public static final int FLAG_BROADCAST_SENDER = 2;
    public static final int FLAG_SERVICE_SENDER = 4;
    private static final int GET_CONTROLLER_LOCK_TIMEOUT = 5000;
    private static final String TAG = "ActivityManager";
    boolean canceled = false;
    final PendingIntentController controller;
    final Key key;
    String lastTag;
    String lastTagPrefix;
    private ArraySet<IBinder> mAllowBgActivityStartsForActivitySender = new ArraySet<>();
    private ArraySet<IBinder> mAllowBgActivityStartsForBroadcastSender = new ArraySet<>();
    private ArraySet<IBinder> mAllowBgActivityStartsForServiceSender = new ArraySet<>();
    private RemoteCallbackList<IResultReceiver> mCancelCallbacks;
    public final WeakReference<PendingIntentRecord> ref;
    long sendTime = 0;
    boolean sent = false;
    String stringName;
    final int uid;
    private ArrayMap<IBinder, Long> whitelistDuration;

    /* access modifiers changed from: package-private */
    public static final class Key {
        private static final int ODD_PRIME_NUMBER = 37;
        final IBinder activity;
        Intent[] allIntents;
        String[] allResolvedTypes;
        final int flags;
        final int hashCode;
        final SafeActivityOptions options;
        final String packageName;
        final int requestCode;
        final Intent requestIntent;
        final String requestResolvedType;
        final int type;
        final int userId;
        final String who;

        Key(int _t, String _p, IBinder _a, String _w, int _r, Intent[] _i, String[] _it, int _f, SafeActivityOptions _o, int _userId) {
            this.type = _t;
            this.packageName = _p;
            this.activity = _a;
            this.who = _w;
            this.requestCode = _r;
            String str = null;
            this.requestIntent = _i != null ? _i[_i.length - 1] : null;
            this.requestResolvedType = _it != null ? _it[_it.length - 1] : str;
            this.allIntents = _i;
            this.allResolvedTypes = _it;
            this.flags = _f;
            this.options = _o;
            this.userId = _userId;
            int hash = (((((23 * 37) + _f) * 37) + _r) * 37) + _userId;
            hash = _w != null ? (hash * 37) + _w.hashCode() : hash;
            hash = _a != null ? (hash * 37) + _a.hashCode() : hash;
            Intent intent = this.requestIntent;
            hash = intent != null ? (hash * 37) + intent.filterHashCode() : hash;
            String str2 = this.requestResolvedType;
            this.hashCode = ((((str2 != null ? (hash * 37) + str2.hashCode() : hash) * 37) + (_p != null ? _p.hashCode() : 0)) * 37) + _t;
        }

        public boolean equals(Object otherObj) {
            if (otherObj == null) {
                return false;
            }
            try {
                Key other = (Key) otherObj;
                if (this.type != other.type || this.userId != other.userId || !Objects.equals(this.packageName, other.packageName) || this.activity != other.activity || !Objects.equals(this.who, other.who) || this.requestCode != other.requestCode) {
                    return false;
                }
                Intent intent = this.requestIntent;
                Intent intent2 = other.requestIntent;
                if (intent != intent2) {
                    if (this.requestIntent != null) {
                        if (!this.requestIntent.filterEquals(intent2) || this.requestIntent.getHwFlags() != other.requestIntent.getHwFlags()) {
                            return false;
                        }
                    } else if (intent2 != null) {
                        return false;
                    }
                }
                if (Objects.equals(this.requestResolvedType, other.requestResolvedType) && this.flags == other.flags) {
                    return true;
                }
                return false;
            } catch (ClassCastException e) {
                return false;
            }
        }

        public int hashCode() {
            return this.hashCode;
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("Key{");
            sb.append(typeName());
            sb.append(" pkg=");
            sb.append(this.packageName);
            sb.append(" intent=");
            Intent intent = this.requestIntent;
            if (intent != null) {
                str = intent.toShortStringWithoutClip(true, true, false);
            } else {
                str = "<null>";
            }
            sb.append(str);
            sb.append(" flags=0x");
            sb.append(Integer.toHexString(this.flags));
            sb.append(" u=");
            sb.append(this.userId);
            sb.append("}");
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public String typeName() {
            int i = this.type;
            if (i == 1) {
                return "broadcastIntent";
            }
            if (i == 2) {
                return "startActivity";
            }
            if (i == 3) {
                return "activityResult";
            }
            if (i == 4) {
                return "startService";
            }
            if (i != 5) {
                return Integer.toString(i);
            }
            return "startForegroundService";
        }
    }

    PendingIntentRecord(PendingIntentController _controller, Key _k, int _u) {
        this.controller = _controller;
        this.key = _k;
        this.uid = _u;
        this.ref = new WeakReference<>(this);
    }

    /* access modifiers changed from: package-private */
    public void setWhitelistDurationLocked(IBinder whitelistToken, long duration) {
        if (duration > 0) {
            if (this.whitelistDuration == null) {
                this.whitelistDuration = new ArrayMap<>();
            }
            this.whitelistDuration.put(whitelistToken, Long.valueOf(duration));
        } else {
            ArrayMap<IBinder, Long> arrayMap = this.whitelistDuration;
            if (arrayMap != null) {
                arrayMap.remove(whitelistToken);
                if (this.whitelistDuration.size() <= 0) {
                    this.whitelistDuration = null;
                }
            }
        }
        this.stringName = null;
    }

    /* access modifiers changed from: package-private */
    public void setAllowBgActivityStarts(IBinder token, int flags) {
        if (token != null) {
            if ((flags & 1) != 0) {
                this.mAllowBgActivityStartsForActivitySender.add(token);
            }
            if ((flags & 2) != 0) {
                this.mAllowBgActivityStartsForBroadcastSender.add(token);
            }
            if ((flags & 4) != 0) {
                this.mAllowBgActivityStartsForServiceSender.add(token);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAllowBgActivityStarts(IBinder token) {
        if (token != null) {
            this.mAllowBgActivityStartsForActivitySender.remove(token);
            this.mAllowBgActivityStartsForBroadcastSender.remove(token);
            this.mAllowBgActivityStartsForServiceSender.remove(token);
        }
    }

    public void registerCancelListenerLocked(IResultReceiver receiver) {
        if (this.mCancelCallbacks == null) {
            this.mCancelCallbacks = new RemoteCallbackList<>();
        }
        this.mCancelCallbacks.register(receiver);
    }

    public void unregisterCancelListenerLocked(IResultReceiver receiver) {
        RemoteCallbackList<IResultReceiver> remoteCallbackList = this.mCancelCallbacks;
        if (remoteCallbackList != null) {
            remoteCallbackList.unregister(receiver);
            if (this.mCancelCallbacks.getRegisteredCallbackCount() <= 0) {
                this.mCancelCallbacks = null;
            }
        }
    }

    public RemoteCallbackList<IResultReceiver> detachCancelListenersLocked() {
        RemoteCallbackList<IResultReceiver> listeners = this.mCancelCallbacks;
        this.mCancelCallbacks = null;
        return listeners;
    }

    public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        sendInner(code, intent, resolvedType, whitelistToken, finishedReceiver, requiredPermission, null, null, 0, 0, 0, options);
    }

    public int sendWithResult(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        return sendInner(code, intent, resolvedType, whitelistToken, finishedReceiver, requiredPermission, null, null, 0, 0, 0, options);
    }

    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x0445  */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x045e  */
    public int sendInner(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle options) {
        SafeActivityOptions mergedOptions;
        Intent finalIntent;
        String resolvedType2;
        SafeActivityOptions mergedOptions2;
        Bundle options2;
        Long duration;
        String[] allResolvedTypes;
        Intent[] allIntents;
        boolean z;
        Throwable th;
        int userId;
        Intent finalIntent2;
        int res;
        boolean z2;
        RuntimeException e;
        int res2;
        boolean z3;
        String resolvedType3;
        String resolvedType4;
        Bundle options3 = options;
        if (intent != null) {
            intent.setDefusable(true);
        }
        if (options3 != null) {
            options3.setDefusable(true);
        }
        long startGetLockTime = System.currentTimeMillis();
        synchronized (this.controller.mLock) {
            try {
                if (this.canceled) {
                    try {
                        return -96;
                    } catch (Throwable th2) {
                        mergedOptions = th2;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th3) {
                                mergedOptions = th3;
                            }
                        }
                        throw mergedOptions;
                    }
                } else {
                    this.sent = true;
                    this.sendTime = System.currentTimeMillis();
                    long getLockCost = this.sendTime - startGetLockTime;
                    if (getLockCost > 5000) {
                        Slog.w("ActivityManager", "get controller.mLock cost:" + getLockCost);
                    }
                    if ((this.key.flags & 1073741824) != 0) {
                        this.controller.cancelIntentSender(this, true);
                    }
                    finalIntent = this.key.requestIntent != null ? new Intent(this.key.requestIntent) : new Intent();
                    try {
                        if (!((this.key.flags & DumpState.DUMP_HANDLE) != 0)) {
                            if (intent != null) {
                                try {
                                    if ((finalIntent.fillIn(intent, this.key.flags) & 2) == 0) {
                                        resolvedType4 = this.key.requestResolvedType;
                                    } else {
                                        resolvedType4 = resolvedType;
                                    }
                                    resolvedType3 = resolvedType4;
                                } catch (Throwable th4) {
                                    mergedOptions = th4;
                                    while (true) {
                                        break;
                                    }
                                    throw mergedOptions;
                                }
                            } else {
                                try {
                                    resolvedType3 = this.key.requestResolvedType;
                                } catch (Throwable th5) {
                                    mergedOptions = th5;
                                    while (true) {
                                        break;
                                    }
                                    throw mergedOptions;
                                }
                            }
                            int flagsMask2 = flagsMask & -196;
                            try {
                                finalIntent.setFlags((finalIntent.getFlags() & (~flagsMask2)) | (flagsValues & flagsMask2));
                                resolvedType2 = resolvedType3;
                            } catch (Throwable th6) {
                                mergedOptions = th6;
                                while (true) {
                                    break;
                                }
                                throw mergedOptions;
                            }
                        } else {
                            try {
                                resolvedType2 = this.key.requestResolvedType;
                            } catch (Throwable th7) {
                                mergedOptions = th7;
                                while (true) {
                                    break;
                                }
                                throw mergedOptions;
                            }
                        }
                    } catch (Throwable th8) {
                        mergedOptions = th8;
                        while (true) {
                            break;
                        }
                        throw mergedOptions;
                    }
                    try {
                        finalIntent.addHwFlags(256);
                        ActivityOptions opts = ActivityOptions.fromBundle(options);
                        if (opts != null) {
                            try {
                                finalIntent.addFlags(opts.getPendingIntentLaunchFlags());
                                if (HwActivityTaskManager.getVirtualDisplayId("padCast") != -1 && HwActivityTaskManager.isMirrorCast("padCast") && opts.getLaunchDisplayId() == -1) {
                                    opts.setLaunchDisplayId(0);
                                }
                            } catch (Throwable th9) {
                                mergedOptions = th9;
                                while (true) {
                                    break;
                                }
                                throw mergedOptions;
                            }
                        }
                        SafeActivityOptions mergedOptions3 = this.key.options;
                        if (mergedOptions3 == null) {
                            mergedOptions3 = new SafeActivityOptions(opts);
                        } else {
                            mergedOptions3.setCallerOptions(opts);
                        }
                        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.enabledInPad() || this.key.type != 2) {
                            options2 = options3;
                            mergedOptions2 = mergedOptions3;
                        } else {
                            if (options3 == null) {
                                options3 = new Bundle();
                            }
                            try {
                                options3.putInt("android.activity.launchDisplayId", HwPCUtils.getPCDisplayID());
                                mergedOptions2 = new SafeActivityOptions(ActivityOptions.fromBundle(options3));
                                options2 = options3;
                            } catch (Throwable th10) {
                                mergedOptions = th10;
                                while (true) {
                                    break;
                                }
                                throw mergedOptions;
                            }
                        }
                    } catch (Throwable th11) {
                        mergedOptions = th11;
                        while (true) {
                            break;
                        }
                        throw mergedOptions;
                    }
                    try {
                        if (this.whitelistDuration != null) {
                            try {
                                duration = this.whitelistDuration.get(whitelistToken);
                            } catch (Throwable th12) {
                                mergedOptions = th12;
                                while (true) {
                                    break;
                                }
                                throw mergedOptions;
                            }
                        } else {
                            duration = null;
                        }
                    } catch (Throwable th13) {
                        mergedOptions = th13;
                        while (true) {
                            break;
                        }
                        throw mergedOptions;
                    }
                    try {
                        if (this.key.type == 2) {
                            try {
                                if (this.key.allIntents != null && this.key.allIntents.length > 1) {
                                    Intent[] allIntents2 = new Intent[this.key.allIntents.length];
                                    String[] allResolvedTypes2 = new String[this.key.allIntents.length];
                                    System.arraycopy(this.key.allIntents, 0, allIntents2, 0, this.key.allIntents.length);
                                    if (this.key.allResolvedTypes != null) {
                                        z = false;
                                        System.arraycopy(this.key.allResolvedTypes, 0, allResolvedTypes2, 0, this.key.allResolvedTypes.length);
                                    } else {
                                        z = false;
                                    }
                                    allIntents2[allIntents2.length - 1] = finalIntent;
                                    allResolvedTypes2[allResolvedTypes2.length - 1] = resolvedType2;
                                    allIntents = allIntents2;
                                    allResolvedTypes = allResolvedTypes2;
                                }
                            } catch (Throwable th14) {
                                mergedOptions = th14;
                                while (true) {
                                    break;
                                }
                                throw mergedOptions;
                            }
                        }
                        z = false;
                        allIntents = null;
                        allResolvedTypes = null;
                        try {
                        } catch (Throwable th15) {
                            mergedOptions = th15;
                            while (true) {
                                break;
                            }
                            throw mergedOptions;
                        }
                    } catch (Throwable th16) {
                        mergedOptions = th16;
                        while (true) {
                            break;
                        }
                        throw mergedOptions;
                    }
                }
            } catch (Throwable th17) {
                mergedOptions = th17;
                while (true) {
                    break;
                }
                throw mergedOptions;
            }
        }
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long origId = Binder.clearCallingIdentity();
        if (duration != null) {
            try {
                int procState = this.controller.mAmInternal.getUidProcessState(callingUid);
                if (!ActivityManager.isProcStateBackground(procState)) {
                    StringBuilder tag = new StringBuilder(64);
                    tag.append("pendingintent:");
                    UserHandle.formatUid(tag, callingUid);
                    tag.append(":");
                    if (finalIntent.getAction() != null) {
                        tag.append(finalIntent.getAction());
                    } else if (finalIntent.getComponent() != null) {
                        finalIntent.getComponent().appendShortString(tag);
                    } else if (finalIntent.getData() != null) {
                        tag.append(finalIntent.getData().toSafeString());
                    }
                    this.controller.mAmInternal.tempWhitelistForPendingIntent(callingPid, callingUid, this.uid, duration.longValue(), tag.toString());
                } else {
                    Slog.w("ActivityManager", "Not doing whitelist " + this + ": caller state=" + procState);
                }
            } catch (Throwable th18) {
                th = th18;
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        }
        boolean sendFinish = finishedReceiver != null ? true : z;
        try {
            int userId2 = this.key.userId;
            if (userId2 == -2) {
                userId = this.controller.mUserController.getCurrentOrTargetUserId();
            } else {
                userId = userId2;
            }
            boolean allowTrampoline = (this.uid == callingUid || !this.controller.mAtmInternal.isUidForeground(callingUid)) ? z : true;
            int i = this.key.type;
            if (i == 1) {
                finalIntent2 = finalIntent;
                try {
                    ActivityManagerInternal activityManagerInternal = this.controller.mAmInternal;
                    String str = this.key.packageName;
                    int i2 = this.uid;
                    boolean z4 = finishedReceiver != null ? true : z;
                    if (!this.mAllowBgActivityStartsForBroadcastSender.contains(whitelistToken)) {
                        if (!allowTrampoline) {
                            z2 = z;
                            if (activityManagerInternal.broadcastIntentInPackage(str, i2, callingUid, callingPid, finalIntent2, resolvedType2, finishedReceiver, code, (String) null, (Bundle) null, requiredPermission, options2, z4, false, userId, z2) == 0) {
                                sendFinish = false;
                            }
                            res = 0;
                            if (!sendFinish) {
                            }
                            Binder.restoreCallingIdentity(origId);
                            return res;
                        }
                    }
                    z2 = true;
                    if (activityManagerInternal.broadcastIntentInPackage(str, i2, callingUid, callingPid, finalIntent2, resolvedType2, finishedReceiver, code, (String) null, (Bundle) null, requiredPermission, options2, z4, false, userId, z2) == 0) {
                    }
                    res = 0;
                } catch (RuntimeException e2) {
                    Slog.w("ActivityManager", "Unable to send startActivity intent", e2);
                } catch (Throwable th19) {
                    th = th19;
                }
                if (!sendFinish) {
                }
                Binder.restoreCallingIdentity(origId);
                return res;
            } else if (i == 2) {
                try {
                    if (this.key.allIntents != null) {
                        try {
                            if (this.key.allIntents.length > 1) {
                                finalIntent2 = finalIntent;
                                try {
                                    res2 = this.controller.mAtmInternal.startActivitiesInPackage(this.uid, callingPid, callingUid, this.key.packageName, allIntents, allResolvedTypes, resultTo, mergedOptions2, userId, false, this, this.mAllowBgActivityStartsForActivitySender.contains(whitelistToken));
                                    res = res2;
                                } catch (RuntimeException e3) {
                                    e = e3;
                                    try {
                                        Slog.w("ActivityManager", "Unable to send startActivity intent", e);
                                        res = 0;
                                        if (!sendFinish) {
                                        }
                                        Binder.restoreCallingIdentity(origId);
                                        return res;
                                    } catch (Throwable th20) {
                                        th = th20;
                                    }
                                } catch (Throwable th21) {
                                    th = th21;
                                    Binder.restoreCallingIdentity(origId);
                                    throw th;
                                }
                                if (!sendFinish && res != -96) {
                                    try {
                                        try {
                                            try {
                                                finishedReceiver.performReceive(new Intent(finalIntent2), 0, (String) null, (Bundle) null, false, false, this.key.userId);
                                            } catch (RemoteException e4) {
                                            } catch (Throwable th22) {
                                                th = th22;
                                                Binder.restoreCallingIdentity(origId);
                                                throw th;
                                            }
                                        } catch (RemoteException e5) {
                                        } catch (Throwable th23) {
                                            th = th23;
                                            Binder.restoreCallingIdentity(origId);
                                            throw th;
                                        }
                                    } catch (RemoteException e6) {
                                    } catch (Throwable th24) {
                                        th = th24;
                                        Binder.restoreCallingIdentity(origId);
                                        throw th;
                                    }
                                }
                                Binder.restoreCallingIdentity(origId);
                                return res;
                            }
                        } catch (RuntimeException e7) {
                            e = e7;
                            finalIntent2 = finalIntent;
                            Slog.w("ActivityManager", "Unable to send startActivity intent", e);
                            res = 0;
                            if (!sendFinish) {
                            }
                            Binder.restoreCallingIdentity(origId);
                            return res;
                        } catch (Throwable th25) {
                            th = th25;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    }
                    finalIntent2 = finalIntent;
                    res2 = this.controller.mAtmInternal.startActivityInPackage(this.uid, callingPid, callingUid, this.key.packageName, finalIntent2, resolvedType2, resultTo, resultWho, requestCode, 0, mergedOptions2, userId, (TaskRecord) null, "PendingIntentRecord", false, this, this.mAllowBgActivityStartsForActivitySender.contains(whitelistToken));
                    res = res2;
                } catch (RuntimeException e8) {
                    e = e8;
                    finalIntent2 = finalIntent;
                    Slog.w("ActivityManager", "Unable to send startActivity intent", e);
                    res = 0;
                    if (!sendFinish) {
                    }
                    Binder.restoreCallingIdentity(origId);
                    return res;
                } catch (Throwable th26) {
                    th = th26;
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
                if (!sendFinish) {
                }
                Binder.restoreCallingIdentity(origId);
                return res;
            } else if (i == 3) {
                this.controller.mAtmInternal.sendActivityResult(-1, this.key.activity, this.key.who, this.key.requestCode, code, finalIntent);
                finalIntent2 = finalIntent;
            } else if (i == 4 || i == 5) {
                try {
                    ActivityManagerInternal activityManagerInternal2 = this.controller.mAmInternal;
                    int i3 = this.uid;
                    boolean z5 = this.key.type == 5 ? true : z;
                    String str2 = this.key.packageName;
                    if (!this.mAllowBgActivityStartsForServiceSender.contains(whitelistToken)) {
                        if (!allowTrampoline) {
                            z3 = z;
                            activityManagerInternal2.startServiceInPackage(i3, finalIntent, resolvedType2, z5, str2, userId, z3);
                            finalIntent2 = finalIntent;
                        }
                    }
                    z3 = true;
                    activityManagerInternal2.startServiceInPackage(i3, finalIntent, resolvedType2, z5, str2, userId, z3);
                    finalIntent2 = finalIntent;
                } catch (RuntimeException e9) {
                    Slog.w("ActivityManager", "Unable to send startService intent", e9);
                    finalIntent2 = finalIntent;
                } catch (TransactionTooLargeException e10) {
                    finalIntent2 = finalIntent;
                    res = -96;
                }
            } else {
                finalIntent2 = finalIntent;
            }
            res = 0;
            if (!sendFinish) {
            }
            Binder.restoreCallingIdentity(origId);
            return res;
        } catch (Throwable th27) {
            th = th27;
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (!this.canceled) {
                this.controller.mH.sendMessage(PooledLambda.obtainMessage($$Lambda$PendingIntentRecord$hlEHdgdG_SS5n3v7IRr7e6QZgLQ.INSTANCE, this));
            }
        } finally {
            PendingIntentRecord.super.finalize();
        }
    }

    /* access modifiers changed from: private */
    public void completeFinalize() {
        synchronized (this.controller.mLock) {
            if (this.controller.mIntentSenderRecords.get(this.key) == this.ref) {
                this.controller.mIntentSenderRecords.remove(this.key);
            }
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("uid=");
        pw.print(this.uid);
        pw.print(" packageName=");
        pw.print(this.key.packageName);
        pw.print(" type=");
        pw.print(this.key.typeName());
        pw.print(" flags=0x");
        pw.println(Integer.toHexString(this.key.flags));
        if (!(this.key.activity == null && this.key.who == null)) {
            pw.print(prefix);
            pw.print("activity=");
            pw.print(this.key.activity);
            pw.print(" who=");
            pw.println(this.key.who);
        }
        if (!(this.key.requestCode == 0 && this.key.requestResolvedType == null)) {
            pw.print(prefix);
            pw.print("requestCode=");
            pw.print(this.key.requestCode);
            pw.print(" requestResolvedType=");
            pw.println(this.key.requestResolvedType);
        }
        if (this.key.requestIntent != null) {
            pw.print(prefix);
            pw.print("requestIntent=");
            pw.println(this.key.requestIntent.toShortString(true, true, true, true));
        }
        if (this.sent || this.canceled) {
            pw.print(prefix);
            pw.print("sent=");
            pw.print(this.sent);
            pw.print(" canceled=");
            pw.println(this.canceled);
        }
        if (this.whitelistDuration != null) {
            pw.print(prefix);
            pw.print("whitelistDuration=");
            for (int i = 0; i < this.whitelistDuration.size(); i++) {
                if (i != 0) {
                    pw.print(", ");
                }
                pw.print(Integer.toHexString(System.identityHashCode(this.whitelistDuration.keyAt(i))));
                pw.print(":");
                TimeUtils.formatDuration(this.whitelistDuration.valueAt(i).longValue(), pw);
            }
            pw.println();
        }
        if (this.mCancelCallbacks != null) {
            pw.print(prefix);
            pw.println("mCancelCallbacks:");
            for (int i2 = 0; i2 < this.mCancelCallbacks.getRegisteredCallbackCount(); i2++) {
                pw.print(prefix);
                pw.print("  #");
                pw.print(i2);
                pw.print(": ");
                pw.println(this.mCancelCallbacks.getRegisteredCallbackItem(i2));
            }
        }
    }

    public String toString() {
        String str = this.stringName;
        if (str != null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("PendingIntentRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.key.packageName);
        sb.append(' ');
        sb.append(this.key.typeName());
        if (this.whitelistDuration != null) {
            sb.append(" (whitelist: ");
            synchronized (this.controller.mLock) {
                for (int i = 0; i < this.whitelistDuration.size(); i++) {
                    if (i != 0) {
                        sb.append(",");
                    }
                    sb.append(Integer.toHexString(System.identityHashCode(this.whitelistDuration.keyAt(i))));
                    sb.append(":");
                    TimeUtils.formatDuration(this.whitelistDuration.valueAt(i).longValue(), sb);
                }
            }
            sb.append(")");
        }
        sb.append('}');
        String sb2 = sb.toString();
        this.stringName = sb2;
        return sb2;
    }
}
