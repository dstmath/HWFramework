package com.android.server.am;

import android.app.BroadcastOptions;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.PrintWriterPrinter;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/* access modifiers changed from: package-private */
public final class BroadcastRecord extends Binder {
    static final int APP_RECEIVE = 1;
    static final int CALL_DONE_RECEIVE = 3;
    static final int CALL_IN_RECEIVE = 2;
    static final int DELIVERY_DELIVERED = 1;
    static final int DELIVERY_PENDING = 0;
    static final int DELIVERY_SKIPPED = 2;
    static final int DELIVERY_TIMEOUT = 3;
    static final int IDLE = 0;
    static final int WAITING_SERVICES = 4;
    static AtomicInteger sNextToken = new AtomicInteger(1);
    final boolean allowBackgroundActivityStarts;
    int anrCount;
    final int appOp;
    final ProcessRecord callerApp;
    final boolean callerInstantApp;
    final String callerPackage;
    final int callingPid;
    final int callingUid;
    ProcessRecord curApp;
    ComponentName curComponent;
    BroadcastFilter curFilter;
    ActivityInfo curReceiver;
    boolean deferred;
    final int[] delivery;
    long dispatchClockTime;
    long dispatchTime;
    final long[] duration;
    long enqueueClockTime;
    long finishTime;
    int iawareCtrlType;
    final boolean initialSticky;
    final Intent intent;
    int manifestCount;
    int manifestSkipCount;
    int nextReceiver;
    final BroadcastOptions options;
    final boolean ordered;
    BroadcastQueue queue;
    IBinder receiver;
    long receiverTime;
    final List receivers;
    final String[] requiredPermissions;
    final String resolvedType;
    boolean resultAbort;
    int resultCode;
    String resultData;
    Bundle resultExtras;
    IIntentReceiver resultTo;
    int splitCount;
    int splitToken;
    int state;
    final boolean sticky;
    final ComponentName targetComp;
    boolean timeoutExempt;
    final int userId;

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix, SimpleDateFormat sdf) {
        long now = SystemClock.uptimeMillis();
        pw.print(prefix);
        pw.print(this);
        pw.print(" to user ");
        pw.println(this.userId);
        pw.print(prefix);
        pw.println(this.intent.toInsecureString());
        ComponentName componentName = this.targetComp;
        if (!(componentName == null || componentName == this.intent.getComponent())) {
            pw.print(prefix);
            pw.print("  targetComp: ");
            pw.println(this.targetComp.toShortString());
        }
        Bundle bundle = this.intent.getExtras();
        if (bundle != null && !"android.intent.action.PHONE_STATE".equals(this.intent.getAction()) && !"android.intent.action.NEW_OUTGOING_CALL".equals(this.intent.getAction()) && !isFromEmailMdm(this.intent)) {
            pw.print(prefix);
            pw.print("  extras: ");
            pw.println(bundle.toString());
        }
        pw.print(prefix);
        pw.print("caller=");
        pw.print(this.callerPackage);
        pw.print(" ");
        ProcessRecord processRecord = this.callerApp;
        pw.print(processRecord != null ? processRecord.toShortString() : "null");
        pw.print(" pid=");
        pw.print(this.callingPid);
        pw.print(" uid=");
        pw.println(this.callingUid);
        String[] strArr = this.requiredPermissions;
        if ((strArr != null && strArr.length > 0) || this.appOp != -1) {
            pw.print(prefix);
            pw.print("requiredPermissions=");
            pw.print(Arrays.toString(this.requiredPermissions));
            pw.print("  appOp=");
            pw.println(this.appOp);
        }
        if (this.options != null) {
            pw.print(prefix);
            pw.print("options=");
            pw.println(this.options.toBundle());
        }
        pw.print(prefix);
        pw.print("enqueueClockTime=");
        pw.print(sdf.format(new Date(this.enqueueClockTime)));
        pw.print(" dispatchClockTime=");
        pw.println(sdf.format(new Date(this.dispatchClockTime)));
        pw.print(prefix);
        pw.print("dispatchTime=");
        TimeUtils.formatDuration(this.dispatchTime, now, pw);
        pw.print(" (");
        TimeUtils.formatDuration(this.dispatchClockTime - this.enqueueClockTime, pw);
        pw.print(" since enq)");
        if (this.finishTime != 0) {
            pw.print(" finishTime=");
            TimeUtils.formatDuration(this.finishTime, now, pw);
            pw.print(" (");
            TimeUtils.formatDuration(this.finishTime - this.dispatchTime, pw);
            pw.print(" since disp)");
        } else {
            pw.print(" receiverTime=");
            TimeUtils.formatDuration(this.receiverTime, now, pw);
        }
        pw.println("");
        if (this.anrCount != 0) {
            pw.print(prefix);
            pw.print("anrCount=");
            pw.println(this.anrCount);
        }
        if (!(this.resultTo == null && this.resultCode == -1 && this.resultData == null)) {
            pw.print(prefix);
            pw.print("resultTo=");
            pw.print(this.resultTo);
            pw.print(" resultCode=");
            pw.print(this.resultCode);
            if (!"android.intent.action.NEW_OUTGOING_CALL".equals(this.intent.getAction())) {
                pw.print(" resultData=");
                pw.println(this.resultData);
            } else {
                pw.print(" resultData=");
                pw.println("xxxxxxxxxxx");
            }
        }
        if (this.resultExtras != null) {
            pw.print(prefix);
            pw.print("resultExtras=");
            pw.println(this.resultExtras);
        }
        if (this.resultAbort || this.ordered || this.sticky || this.initialSticky) {
            pw.print(prefix);
            pw.print("resultAbort=");
            pw.print(this.resultAbort);
            pw.print(" ordered=");
            pw.print(this.ordered);
            pw.print(" sticky=");
            pw.print(this.sticky);
            pw.print(" initialSticky=");
            pw.println(this.initialSticky);
        }
        if (!(this.nextReceiver == 0 && this.receiver == null)) {
            pw.print(prefix);
            pw.print("nextReceiver=");
            pw.print(this.nextReceiver);
            pw.print(" receiver=");
            pw.println(this.receiver);
        }
        if (this.curFilter != null) {
            pw.print(prefix);
            pw.print("curFilter=");
            pw.println(this.curFilter);
        }
        if (this.curReceiver != null) {
            pw.print(prefix);
            pw.print("curReceiver=");
            pw.println(this.curReceiver);
        }
        if (this.curApp != null) {
            pw.print(prefix);
            pw.print("curApp=");
            pw.println(this.curApp);
            pw.print(prefix);
            pw.print("curComponent=");
            ComponentName componentName2 = this.curComponent;
            pw.println(componentName2 != null ? componentName2.toShortString() : "--");
            ActivityInfo activityInfo = this.curReceiver;
            if (!(activityInfo == null || activityInfo.applicationInfo == null)) {
                pw.print(prefix);
                pw.print("curSourceDir=");
                pw.println(this.curReceiver.applicationInfo.sourceDir);
            }
        }
        int i = this.state;
        int i2 = 2;
        int i3 = 1;
        if (i != 0) {
            String stateStr = " (?)";
            if (i == 1) {
                stateStr = " (APP_RECEIVE)";
            } else if (i == 2) {
                stateStr = " (CALL_IN_RECEIVE)";
            } else if (i == 3) {
                stateStr = " (CALL_DONE_RECEIVE)";
            } else if (i == 4) {
                stateStr = " (WAITING_SERVICES)";
            }
            pw.print(prefix);
            pw.print("state=");
            pw.print(this.state);
            pw.println(stateStr);
        }
        List list = this.receivers;
        int N = list != null ? list.size() : 0;
        String p2 = prefix + "  ";
        PrintWriterPrinter printer = new PrintWriterPrinter(pw);
        int i4 = 0;
        while (i4 < N) {
            Object o = this.receivers.get(i4);
            pw.print(prefix);
            int i5 = this.delivery[i4];
            if (i5 == 0) {
                pw.print("Pending");
            } else if (i5 == i3) {
                pw.print("Deliver");
            } else if (i5 == i2) {
                pw.print("Skipped");
            } else if (i5 != 3) {
                pw.print("???????");
            } else {
                pw.print("Timeout");
            }
            pw.print(" ");
            TimeUtils.formatDuration(this.duration[i4], pw);
            pw.print(" #");
            pw.print(i4);
            pw.print(": ");
            if (o instanceof BroadcastFilter) {
                pw.println(o);
                ((BroadcastFilter) o).dumpBrief(pw, p2);
            } else if (o instanceof ResolveInfo) {
                pw.println("(manifest)");
                ((ResolveInfo) o).dump(printer, p2, 0);
            } else {
                pw.println(o);
            }
            i4++;
            i2 = 2;
            i3 = 1;
        }
    }

    private boolean isFromEmailMdm(Intent intentInner) {
        if (intentInner == null) {
            return false;
        }
        return "com.huawei.devicepolicy.action.POLICY_CHANGED".equals(intentInner.getAction());
    }

    BroadcastRecord(BroadcastQueue _queue, Intent _intent, ProcessRecord _callerApp, String _callerPackage, int _callingPid, int _callingUid, boolean _callerInstantApp, String _resolvedType, String[] _requiredPermissions, int _appOp, BroadcastOptions _options, List _receivers, IIntentReceiver _resultTo, int _resultCode, String _resultData, Bundle _resultExtras, boolean _serialized, boolean _sticky, boolean _initialSticky, int _userId, boolean _allowBackgroundActivityStarts, boolean _timeoutExempt) {
        if (_intent != null) {
            this.queue = _queue;
            this.intent = _intent;
            this.targetComp = _intent.getComponent();
            this.callerApp = _callerApp;
            this.callerPackage = _callerPackage;
            this.callingPid = _callingPid;
            this.callingUid = _callingUid;
            this.callerInstantApp = _callerInstantApp;
            this.resolvedType = _resolvedType;
            this.requiredPermissions = _requiredPermissions;
            this.appOp = _appOp;
            this.options = _options;
            this.receivers = _receivers;
            this.delivery = new int[(_receivers != null ? _receivers.size() : 0)];
            this.duration = new long[this.delivery.length];
            this.resultTo = _resultTo;
            this.resultCode = _resultCode;
            this.resultData = _resultData;
            this.resultExtras = _resultExtras;
            this.ordered = _serialized;
            this.sticky = _sticky;
            this.initialSticky = _initialSticky;
            this.userId = _userId;
            this.nextReceiver = 0;
            this.state = 0;
            this.allowBackgroundActivityStarts = _allowBackgroundActivityStarts;
            this.timeoutExempt = _timeoutExempt;
            return;
        }
        throw new NullPointerException("Can't construct with a null intent");
    }

    private BroadcastRecord(BroadcastRecord from, Intent newIntent) {
        this.intent = newIntent;
        this.targetComp = newIntent.getComponent();
        this.callerApp = from.callerApp;
        this.callerPackage = from.callerPackage;
        this.callingPid = from.callingPid;
        this.callingUid = from.callingUid;
        this.callerInstantApp = from.callerInstantApp;
        this.ordered = from.ordered;
        this.sticky = from.sticky;
        this.initialSticky = from.initialSticky;
        this.userId = from.userId;
        this.resolvedType = from.resolvedType;
        this.requiredPermissions = from.requiredPermissions;
        this.appOp = from.appOp;
        this.options = from.options;
        this.receivers = from.receivers;
        this.delivery = from.delivery;
        this.duration = from.duration;
        this.resultTo = from.resultTo;
        this.enqueueClockTime = from.enqueueClockTime;
        this.dispatchTime = from.dispatchTime;
        this.dispatchClockTime = from.dispatchClockTime;
        this.receiverTime = from.receiverTime;
        this.finishTime = from.finishTime;
        this.resultCode = from.resultCode;
        this.resultData = from.resultData;
        this.resultExtras = from.resultExtras;
        this.resultAbort = from.resultAbort;
        this.nextReceiver = from.nextReceiver;
        this.receiver = from.receiver;
        this.state = from.state;
        this.anrCount = from.anrCount;
        this.manifestCount = from.manifestCount;
        this.manifestSkipCount = from.manifestSkipCount;
        this.queue = from.queue;
        this.allowBackgroundActivityStarts = from.allowBackgroundActivityStarts;
        this.timeoutExempt = from.timeoutExempt;
    }

    /* access modifiers changed from: package-private */
    public BroadcastRecord splitRecipientsLocked(int slowAppUid, int startingAt) {
        ArrayList splitReceivers = null;
        int i = startingAt;
        while (i < this.receivers.size()) {
            Object o = this.receivers.get(i);
            if (getReceiverUid(o) == slowAppUid) {
                if (splitReceivers == null) {
                    splitReceivers = new ArrayList();
                }
                splitReceivers.add(o);
                this.receivers.remove(i);
            } else {
                i++;
            }
        }
        if (splitReceivers == null) {
            return null;
        }
        BroadcastRecord split = new BroadcastRecord(this.queue, this.intent, this.callerApp, this.callerPackage, this.callingPid, this.callingUid, this.callerInstantApp, this.resolvedType, this.requiredPermissions, this.appOp, this.options, splitReceivers, this.resultTo, this.resultCode, this.resultData, this.resultExtras, this.ordered, this.sticky, this.initialSticky, this.userId, this.allowBackgroundActivityStarts, this.timeoutExempt);
        split.splitToken = this.splitToken;
        return split;
    }

    /* access modifiers changed from: package-private */
    public int getReceiverUid(Object receiver2) {
        if (receiver2 instanceof BroadcastFilter) {
            return ((BroadcastFilter) receiver2).owningUid;
        }
        return ((ResolveInfo) receiver2).activityInfo.applicationInfo.uid;
    }

    public BroadcastRecord maybeStripForHistory() {
        if (!this.intent.canStripForHistory()) {
            return this;
        }
        return new BroadcastRecord(this, this.intent.maybeStripForHistory());
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean cleanupDisabledPackageReceiversLocked(String packageName, Set<String> filterByClasses, int userId2, boolean doit) {
        if (this.receivers == null) {
            return false;
        }
        boolean cleanupAllUsers = userId2 == -1;
        boolean sendToAllUsers = this.userId == -1;
        if (!(this.userId == userId2 || cleanupAllUsers || sendToAllUsers)) {
            return false;
        }
        boolean didSomething = false;
        for (int i = this.receivers.size() - 1; i >= 0; i--) {
            Object o = this.receivers.get(i);
            if (o instanceof ResolveInfo) {
                ActivityInfo info = ((ResolveInfo) o).activityInfo;
                if ((packageName == null || (info.applicationInfo.packageName.equals(packageName) && (filterByClasses == null || filterByClasses.contains(info.name)))) && (cleanupAllUsers || UserHandle.getUserId(info.applicationInfo.uid) == userId2)) {
                    if (!doit) {
                        return true;
                    }
                    didSomething = true;
                    this.receivers.remove(i);
                    int i2 = this.nextReceiver;
                    if (i < i2) {
                        this.nextReceiver = i2 - 1;
                    }
                    if (this == this.queue.mPendingBroadcast && i == this.queue.mPendingBroadcastRecvIndex) {
                        this.queue.mPendingBroadcast = null;
                    }
                }
            }
        }
        this.nextReceiver = Math.min(this.nextReceiver, this.receivers.size());
        return didSomething;
    }

    @Override // java.lang.Object
    public String toString() {
        return "BroadcastRecord{" + Integer.toHexString(System.identityHashCode(this)) + " u" + this.userId + " " + this.intent.getAction() + "}";
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, this.userId);
        proto.write(1138166333442L, this.intent.getAction());
        proto.end(token);
    }
}
