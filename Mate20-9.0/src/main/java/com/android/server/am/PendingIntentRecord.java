package com.android.server.am;

import android.app.ActivityManager;
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
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.os.IResultReceiver;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Objects;

final class PendingIntentRecord extends IIntentSender.Stub {
    private static final String TAG = "ActivityManager";
    boolean canceled = false;
    final Key key;
    String lastTag;
    String lastTagPrefix;
    private RemoteCallbackList<IResultReceiver> mCancelCallbacks;
    final ActivityManagerService owner;
    final WeakReference<PendingIntentRecord> ref;
    long sendTime = 0;
    boolean sent = false;
    String stringName;
    final int uid;
    private ArrayMap<IBinder, Long> whitelistDuration;

    static final class Key {
        private static final int ODD_PRIME_NUMBER = 37;
        final ActivityRecord activity;
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

        Key(int _t, String _p, ActivityRecord _a, String _w, int _r, Intent[] _i, String[] _it, int _f, SafeActivityOptions _o, int _userId) {
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
            int hash = (37 * ((37 * ((37 * 23) + _f)) + _r)) + _userId;
            hash = _w != null ? (37 * hash) + _w.hashCode() : hash;
            this.hashCode = (37 * ((37 * (this.requestResolvedType != null ? (37 * (this.requestIntent != null ? (37 * (_a != null ? (37 * hash) + _a.hashCode() : hash)) + this.requestIntent.filterHashCode() : hash)) + this.requestResolvedType.hashCode() : hash)) + (_p != null ? _p.hashCode() : 0))) + _t;
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
                if (this.requestIntent != other.requestIntent) {
                    if (this.requestIntent != null) {
                        if (!this.requestIntent.filterEquals(other.requestIntent) || this.requestIntent.getHwFlags() != other.requestIntent.getHwFlags()) {
                            return false;
                        }
                    } else if (other.requestIntent != null) {
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
            StringBuilder sb = new StringBuilder();
            sb.append("Key{");
            sb.append(typeName());
            sb.append(" pkg=");
            sb.append(this.packageName);
            sb.append(" intent=");
            sb.append(this.requestIntent != null ? this.requestIntent.toShortString(true, true, false, false) : "<null>");
            sb.append(" flags=0x");
            sb.append(Integer.toHexString(this.flags));
            sb.append(" u=");
            sb.append(this.userId);
            sb.append("}");
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public String typeName() {
            switch (this.type) {
                case 1:
                    return "broadcastIntent";
                case 2:
                    return "startActivity";
                case 3:
                    return "activityResult";
                case 4:
                    return "startService";
                case 5:
                    return "startForegroundService";
                default:
                    return Integer.toString(this.type);
            }
        }
    }

    PendingIntentRecord(ActivityManagerService _owner, Key _k, int _u) {
        this.owner = _owner;
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
        } else if (this.whitelistDuration != null) {
            this.whitelistDuration.remove(whitelistToken);
            if (this.whitelistDuration.size() <= 0) {
                this.whitelistDuration = null;
            }
        }
        this.stringName = null;
    }

    public void registerCancelListenerLocked(IResultReceiver receiver) {
        if (this.mCancelCallbacks == null) {
            this.mCancelCallbacks = new RemoteCallbackList<>();
        }
        this.mCancelCallbacks.register(receiver);
    }

    public void unregisterCancelListenerLocked(IResultReceiver receiver) {
        if (this.mCancelCallbacks != null) {
            this.mCancelCallbacks.unregister(receiver);
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x0318  */
    public int sendInner(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle options) {
        ActivityManagerService activityManagerService;
        int i;
        ActivityManagerService activityManagerService2;
        String resolvedType2;
        int callingUid;
        int callingPid;
        Bundle options2;
        Bundle options3;
        Intent finalIntent;
        long origId;
        int i2;
        String resolvedType3;
        Intent intent2 = intent;
        Bundle options4 = options;
        boolean z = true;
        if (intent2 != null) {
            intent2.setDefusable(true);
        }
        if (options4 != null) {
            options4.setDefusable(true);
        }
        ActivityManagerService activityManagerService3 = this.owner;
        synchronized (activityManagerService3) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (!this.canceled) {
                    this.sent = true;
                    this.sendTime = System.currentTimeMillis();
                    if ((this.key.flags & 1073741824) != 0) {
                        try {
                            this.owner.cancelIntentSenderLocked(this, true);
                        } catch (Throwable th) {
                            th = th;
                            String str = resolvedType;
                            int i3 = flagsMask;
                            int i4 = flagsValues;
                        }
                    }
                    Intent finalIntent2 = this.key.requestIntent != null ? new Intent(this.key.requestIntent) : new Intent();
                    if (!((this.key.flags & 67108864) != 0)) {
                        if (intent2 == null) {
                            try {
                                resolvedType3 = this.key.requestResolvedType;
                            } catch (Throwable th2) {
                                th = th2;
                                String str2 = resolvedType;
                                int i5 = flagsValues;
                                Bundle bundle = options4;
                                int i6 = flagsMask;
                                activityManagerService = activityManagerService3;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } else if ((finalIntent2.fillIn(intent2, this.key.flags) & 2) == 0) {
                            resolvedType3 = this.key.requestResolvedType;
                        } else {
                            resolvedType3 = resolvedType;
                        }
                        int flagsMask2 = flagsMask & -196;
                        int flagsValues2 = flagsValues & flagsMask2;
                        try {
                            finalIntent2.setFlags((finalIntent2.getFlags() & (~flagsMask2)) | flagsValues2);
                            resolvedType2 = resolvedType3;
                            int i7 = flagsMask2;
                            int i8 = flagsValues2;
                        } catch (Throwable th4) {
                            th = th4;
                            String str3 = resolvedType3;
                            int i9 = flagsMask2;
                            int i10 = flagsValues2;
                            activityManagerService = activityManagerService3;
                            while (true) {
                                break;
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        i = flagsMask;
                        try {
                            int i11 = flagsValues;
                            resolvedType2 = this.key.requestResolvedType;
                            int i12 = i;
                        } catch (Throwable th5) {
                            th = th5;
                            activityManagerService2 = activityManagerService3;
                            int flagsMask3 = i;
                            while (true) {
                                break;
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    try {
                        finalIntent2.addHwFlags(256);
                        callingUid = Binder.getCallingUid();
                        callingPid = Binder.getCallingPid();
                        if (HwPCUtils.enabledInPad()) {
                            try {
                                if (HwPCUtils.isPcCastModeInServer() && this.key.type == 2) {
                                    if (options4 == null) {
                                        options4 = new Bundle();
                                    }
                                    options4.putInt("android.activity.launchDisplayId", HwPCUtils.getPCDisplayID());
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                activityManagerService = activityManagerService3;
                                while (true) {
                                    break;
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                        options2 = options4;
                    } catch (Throwable th7) {
                        th = th7;
                        activityManagerService = activityManagerService3;
                        while (true) {
                            break;
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                    try {
                        SafeActivityOptions mergedOptions = this.key.options;
                        if (mergedOptions == null) {
                            try {
                                mergedOptions = SafeActivityOptions.fromBundle(options2);
                            } catch (RuntimeException e) {
                                Slog.w("ActivityManager", "Unable to send startActivity intent", e);
                            } catch (Throwable th8) {
                                th = th8;
                            }
                        } else {
                            mergedOptions.setCallerOptions(ActivityOptions.fromBundle(options2));
                        }
                        SafeActivityOptions mergedOptions2 = mergedOptions;
                        long origId2 = Binder.clearCallingIdentity();
                        if (this.whitelistDuration != null) {
                            try {
                                Long duration = this.whitelistDuration.get(whitelistToken);
                                if (duration != null) {
                                    if (!ActivityManager.isProcStateBackground(this.owner.getUidState(callingUid))) {
                                        StringBuilder tag = new StringBuilder(64);
                                        tag.append("pendingintent:");
                                        UserHandle.formatUid(tag, callingUid);
                                        tag.append(":");
                                        if (finalIntent2.getAction() != null) {
                                            tag.append(finalIntent2.getAction());
                                        } else if (finalIntent2.getComponent() != null) {
                                            finalIntent2.getComponent().appendShortString(tag);
                                        } else if (finalIntent2.getData() != null) {
                                            tag.append(finalIntent2.getData());
                                        }
                                        this.owner.tempWhitelistForPendingIntentLocked(callingPid, callingUid, this.uid, duration.longValue(), tag.toString());
                                    } else {
                                        Slog.w("ActivityManager", "Not doing whitelist " + this + ": caller state=" + procState);
                                    }
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                IBinder iBinder = whitelistToken;
                                activityManagerService = activityManagerService3;
                                while (true) {
                                    break;
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } else {
                            IBinder iBinder2 = whitelistToken;
                        }
                        boolean sendFinish = finishedReceiver != null;
                        int userId = this.key.userId;
                        if (userId == -2) {
                            userId = this.owner.mUserController.getCurrentOrTargetUserId();
                        }
                        int userId2 = userId;
                        int res = 0;
                        switch (this.key.type) {
                            case 1:
                                origId = origId2;
                                try {
                                    options3 = options2;
                                    int i13 = callingUid;
                                    finalIntent = finalIntent2;
                                    activityManagerService = activityManagerService3;
                                    try {
                                        if (this.owner.broadcastIntentInPackage(this.key.packageName, this.uid, finalIntent2, resolvedType2, finishedReceiver, code, null, null, requiredPermission, options3, finishedReceiver != null, false, userId2) == 0) {
                                            sendFinish = false;
                                        }
                                    } catch (RuntimeException e2) {
                                        e = e2;
                                        try {
                                            Slog.w("ActivityManager", "Unable to send startActivity intent", e);
                                            int res2 = res;
                                            if (sendFinish) {
                                            }
                                            Binder.restoreCallingIdentity(origId);
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return res2;
                                        } catch (Throwable th10) {
                                            th = th10;
                                            Bundle bundle2 = options3;
                                            break;
                                        }
                                    }
                                } catch (RuntimeException e3) {
                                    e = e3;
                                    options3 = options2;
                                    int i14 = callingUid;
                                    finalIntent = finalIntent2;
                                    activityManagerService = activityManagerService3;
                                    Slog.w("ActivityManager", "Unable to send startActivity intent", e);
                                    int res22 = res;
                                    if (sendFinish) {
                                    }
                                    Binder.restoreCallingIdentity(origId);
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return res22;
                                }
                                break;
                            case 2:
                                origId = origId2;
                                if (this.key.allIntents == null || this.key.allIntents.length <= 1) {
                                    i2 = this.owner.getActivityStartController().startActivityInPackage(this.uid, callingPid, callingUid, this.key.packageName, finalIntent2, resolvedType2, resultTo, resultWho, requestCode, 0, mergedOptions2, userId2, null, "PendingIntentRecord", false);
                                } else {
                                    Intent[] allIntents = new Intent[this.key.allIntents.length];
                                    String[] allResolvedTypes = new String[this.key.allIntents.length];
                                    System.arraycopy(this.key.allIntents, 0, allIntents, 0, this.key.allIntents.length);
                                    if (this.key.allResolvedTypes != null) {
                                        System.arraycopy(this.key.allResolvedTypes, 0, allResolvedTypes, 0, this.key.allResolvedTypes.length);
                                    }
                                    allIntents[allIntents.length - 1] = finalIntent2;
                                    allResolvedTypes[allResolvedTypes.length - 1] = resolvedType2;
                                    i2 = this.owner.getActivityStartController().startActivitiesInPackage(this.uid, this.key.packageName, allIntents, allResolvedTypes, resultTo, mergedOptions2, userId2, false);
                                }
                                res = i2;
                                break;
                            case 3:
                                origId = origId2;
                                ActivityStack stack = this.key.activity.getStack();
                                if (stack != null) {
                                    stack.sendActivityResultLocked(-1, this.key.activity, this.key.who, this.key.requestCode, code, finalIntent2);
                                    break;
                                }
                                break;
                            case 4:
                            case 5:
                                try {
                                    ActivityManagerService activityManagerService4 = this.owner;
                                    int i15 = this.uid;
                                    if (this.key.type != 5) {
                                        z = false;
                                    }
                                    origId = origId2;
                                    try {
                                        activityManagerService4.startServiceInPackage(i15, finalIntent2, resolvedType2, z, this.key.packageName, userId2);
                                    } catch (RuntimeException e4) {
                                        e = e4;
                                    } catch (TransactionTooLargeException e5) {
                                        res = -96;
                                        options3 = options2;
                                        finalIntent = finalIntent2;
                                        activityManagerService = activityManagerService3;
                                        int res222 = res;
                                        if (sendFinish || res222 == -96) {
                                        } else {
                                            try {
                                                try {
                                                    finishedReceiver.performReceive(new Intent(finalIntent), 0, null, null, false, false, this.key.userId);
                                                } catch (RemoteException e6) {
                                                }
                                            } catch (RemoteException e7) {
                                                Intent intent3 = finalIntent;
                                            }
                                        }
                                        Binder.restoreCallingIdentity(origId);
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        return res222;
                                    }
                                } catch (RuntimeException e8) {
                                    e = e8;
                                    origId = origId2;
                                    Slog.w("ActivityManager", "Unable to send startService intent", e);
                                    options3 = options2;
                                    finalIntent = finalIntent2;
                                    activityManagerService = activityManagerService3;
                                    int res2222 = res;
                                    if (sendFinish) {
                                    }
                                    Binder.restoreCallingIdentity(origId);
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return res2222;
                                } catch (TransactionTooLargeException e9) {
                                    origId = origId2;
                                    res = -96;
                                    options3 = options2;
                                    finalIntent = finalIntent2;
                                    activityManagerService = activityManagerService3;
                                    int res22222 = res;
                                    if (sendFinish) {
                                    }
                                    Binder.restoreCallingIdentity(origId);
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return res22222;
                                }
                            default:
                                options3 = options2;
                                origId = origId2;
                                int i16 = callingUid;
                                finalIntent = finalIntent2;
                                activityManagerService = activityManagerService3;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        Bundle bundle3 = options2;
                        activityManagerService = activityManagerService3;
                        while (true) {
                            break;
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                } else {
                    i = flagsMask;
                    activityManagerService2 = activityManagerService3;
                    try {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return -96;
                    } catch (Throwable th12) {
                        th = th12;
                        int flagsMask32 = i;
                        while (true) {
                            break;
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } catch (Throwable th13) {
                th = th13;
                i = flagsMask;
                activityManagerService2 = activityManagerService3;
                int flagsMask322 = i;
                while (true) {
                    break;
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (!this.canceled) {
                this.owner.mHandler.sendMessage(this.owner.mHandler.obtainMessage(23, this));
            }
        } finally {
            PendingIntentRecord.super.finalize();
        }
    }

    public void completeFinalize() {
        synchronized (this.owner) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.owner.mIntentSenderRecords.get(this.key) == this.ref) {
                    this.owner.mIntentSenderRecords.remove(this.key);
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
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
        int i = 0;
        if (this.whitelistDuration != null) {
            pw.print(prefix);
            pw.print("whitelistDuration=");
            for (int i2 = 0; i2 < this.whitelistDuration.size(); i2++) {
                if (i2 != 0) {
                    pw.print(", ");
                }
                pw.print(Integer.toHexString(System.identityHashCode(this.whitelistDuration.keyAt(i2))));
                pw.print(":");
                TimeUtils.formatDuration(this.whitelistDuration.valueAt(i2).longValue(), pw);
            }
            pw.println();
        }
        if (this.mCancelCallbacks != null) {
            pw.print(prefix);
            pw.println("mCancelCallbacks:");
            while (true) {
                int i3 = i;
                if (i3 < this.mCancelCallbacks.getRegisteredCallbackCount()) {
                    pw.print(prefix);
                    pw.print("  #");
                    pw.print(i3);
                    pw.print(": ");
                    pw.println(this.mCancelCallbacks.getRegisteredCallbackItem(i3));
                    i = i3 + 1;
                } else {
                    return;
                }
            }
        }
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
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
            for (int i = 0; i < this.whitelistDuration.size(); i++) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(Integer.toHexString(System.identityHashCode(this.whitelistDuration.keyAt(i))));
                sb.append(":");
                TimeUtils.formatDuration(this.whitelistDuration.valueAt(i).longValue(), sb);
            }
            sb.append(")");
        }
        sb.append('}');
        String sb2 = sb.toString();
        this.stringName = sb2;
        return sb2;
    }
}
