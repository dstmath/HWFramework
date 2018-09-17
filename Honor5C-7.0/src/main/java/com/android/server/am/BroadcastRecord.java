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
import android.util.PrintWriterPrinter;
import android.util.TimeUtils;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

final class BroadcastRecord extends Binder {
    static final int APP_RECEIVE = 1;
    static final int CALL_DONE_RECEIVE = 3;
    static final int CALL_IN_RECEIVE = 2;
    static final int DELIVERY_DELIVERED = 1;
    static final int DELIVERY_PENDING = 0;
    static final int DELIVERY_SKIPPED = 2;
    static final int DELIVERY_TIMEOUT = 3;
    static final int IDLE = 0;
    static final int WAITING_SERVICES = 4;
    int anrCount;
    final int appOp;
    final ProcessRecord callerApp;
    final String callerPackage;
    final int callingPid;
    final int callingUid;
    ProcessRecord curApp;
    ComponentName curComponent;
    BroadcastFilter curFilter;
    ActivityInfo curReceiver;
    final int[] delivery;
    long dispatchClockTime;
    long dispatchTime;
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
    int state;
    final boolean sticky;
    final ComponentName targetComp;
    final int userId;

    void dump(PrintWriter pw, String prefix, SimpleDateFormat sdf) {
        String stateStr;
        int N;
        String p2;
        PrintWriterPrinter printer;
        int i;
        Object o;
        long now = SystemClock.uptimeMillis();
        pw.print(prefix);
        pw.print(this);
        pw.print(" to user ");
        pw.println(this.userId);
        pw.print(prefix);
        pw.println(this.intent.toInsecureString());
        if (!(this.targetComp == null || this.targetComp == this.intent.getComponent())) {
            pw.print(prefix);
            pw.print("  targetComp: ");
            pw.println(this.targetComp.toShortString());
        }
        Bundle bundle = this.intent.getExtras();
        if (!(bundle == null || "android.intent.action.PHONE_STATE".equals(this.intent.getAction()) || "android.intent.action.NEW_OUTGOING_CALL".equals(this.intent.getAction()))) {
            pw.print(prefix);
            pw.print("  extras: ");
            pw.println(bundle.toString());
        }
        pw.print(prefix);
        pw.print("caller=");
        pw.print(this.callerPackage);
        pw.print(" ");
        pw.print(this.callerApp != null ? this.callerApp.toShortString() : "null");
        pw.print(" pid=");
        pw.print(this.callingPid);
        pw.print(" uid=");
        pw.println(this.callingUid);
        if (this.requiredPermissions == null || this.requiredPermissions.length <= 0) {
            if (this.appOp != -1) {
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
            if (this.finishTime == 0) {
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
            if (this.resultTo == null && this.resultCode == -1) {
                if (this.resultData != null) {
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
                    pw.println(this.curComponent != null ? this.curComponent.toShortString() : "--");
                    if (!(this.curReceiver == null || this.curReceiver.applicationInfo == null)) {
                        pw.print(prefix);
                        pw.print("curSourceDir=");
                        pw.println(this.curReceiver.applicationInfo.sourceDir);
                    }
                }
                if (this.state != 0) {
                    stateStr = " (?)";
                    switch (this.state) {
                        case DELIVERY_DELIVERED /*1*/:
                            stateStr = " (APP_RECEIVE)";
                            break;
                        case DELIVERY_SKIPPED /*2*/:
                            stateStr = " (CALL_IN_RECEIVE)";
                            break;
                        case DELIVERY_TIMEOUT /*3*/:
                            stateStr = " (CALL_DONE_RECEIVE)";
                            break;
                        case WAITING_SERVICES /*4*/:
                            stateStr = " (WAITING_SERVICES)";
                            break;
                    }
                    pw.print(prefix);
                    pw.print("state=");
                    pw.print(this.state);
                    pw.println(stateStr);
                }
                N = this.receivers != null ? this.receivers.size() : IDLE;
                p2 = prefix + "  ";
                printer = new PrintWriterPrinter(pw);
                for (i = IDLE; i < N; i += DELIVERY_DELIVERED) {
                    o = this.receivers.get(i);
                    pw.print(prefix);
                    switch (this.delivery[i]) {
                        case IDLE /*0*/:
                            pw.print("Pending");
                            break;
                        case DELIVERY_DELIVERED /*1*/:
                            pw.print("Deliver");
                            break;
                        case DELIVERY_SKIPPED /*2*/:
                            pw.print("Skipped");
                            break;
                        case DELIVERY_TIMEOUT /*3*/:
                            pw.print("Timeout");
                            break;
                        default:
                            pw.print("???????");
                            break;
                    }
                    pw.print(" #");
                    pw.print(i);
                    pw.print(": ");
                    if (!(o instanceof BroadcastFilter)) {
                        pw.println(o);
                        ((BroadcastFilter) o).dumpBrief(pw, p2);
                    } else if (o instanceof ResolveInfo) {
                        pw.println("(manifest)");
                        ((ResolveInfo) o).dump(printer, p2, IDLE);
                    } else {
                        pw.println(o);
                    }
                }
            }
            pw.print(prefix);
            pw.print("resultTo=");
            pw.print(this.resultTo);
            pw.print(" resultCode=");
            pw.print(this.resultCode);
            if ("android.intent.action.NEW_OUTGOING_CALL".equals(this.intent.getAction())) {
                pw.print(" resultData=");
                pw.println(this.resultData);
            } else {
                pw.print(" resultData=");
                pw.println("xxxxxxxxxxx");
            }
            if (this.resultExtras != null) {
                pw.print(prefix);
                pw.print("resultExtras=");
                pw.println(this.resultExtras);
            }
            pw.print(prefix);
            pw.print("resultAbort=");
            pw.print(this.resultAbort);
            pw.print(" ordered=");
            pw.print(this.ordered);
            pw.print(" sticky=");
            pw.print(this.sticky);
            pw.print(" initialSticky=");
            pw.println(this.initialSticky);
            pw.print(prefix);
            pw.print("nextReceiver=");
            pw.print(this.nextReceiver);
            pw.print(" receiver=");
            pw.println(this.receiver);
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
                if (this.curComponent != null) {
                }
                pw.println(this.curComponent != null ? this.curComponent.toShortString() : "--");
                pw.print(prefix);
                pw.print("curSourceDir=");
                pw.println(this.curReceiver.applicationInfo.sourceDir);
            }
            if (this.state != 0) {
                stateStr = " (?)";
                switch (this.state) {
                    case DELIVERY_DELIVERED /*1*/:
                        stateStr = " (APP_RECEIVE)";
                        break;
                    case DELIVERY_SKIPPED /*2*/:
                        stateStr = " (CALL_IN_RECEIVE)";
                        break;
                    case DELIVERY_TIMEOUT /*3*/:
                        stateStr = " (CALL_DONE_RECEIVE)";
                        break;
                    case WAITING_SERVICES /*4*/:
                        stateStr = " (WAITING_SERVICES)";
                        break;
                }
                pw.print(prefix);
                pw.print("state=");
                pw.print(this.state);
                pw.println(stateStr);
            }
            if (this.receivers != null) {
            }
            p2 = prefix + "  ";
            printer = new PrintWriterPrinter(pw);
            for (i = IDLE; i < N; i += DELIVERY_DELIVERED) {
                o = this.receivers.get(i);
                pw.print(prefix);
                switch (this.delivery[i]) {
                    case IDLE /*0*/:
                        pw.print("Pending");
                        break;
                    case DELIVERY_DELIVERED /*1*/:
                        pw.print("Deliver");
                        break;
                    case DELIVERY_SKIPPED /*2*/:
                        pw.print("Skipped");
                        break;
                    case DELIVERY_TIMEOUT /*3*/:
                        pw.print("Timeout");
                        break;
                    default:
                        pw.print("???????");
                        break;
                }
                pw.print(" #");
                pw.print(i);
                pw.print(": ");
                if (!(o instanceof BroadcastFilter)) {
                    pw.println(o);
                    ((BroadcastFilter) o).dumpBrief(pw, p2);
                } else if (o instanceof ResolveInfo) {
                    pw.println(o);
                } else {
                    pw.println("(manifest)");
                    ((ResolveInfo) o).dump(printer, p2, IDLE);
                }
            }
        }
        pw.print(prefix);
        pw.print("requiredPermissions=");
        pw.print(Arrays.toString(this.requiredPermissions));
        pw.print("  appOp=");
        pw.println(this.appOp);
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
        if (this.finishTime == 0) {
            pw.print(" receiverTime=");
            TimeUtils.formatDuration(this.receiverTime, now, pw);
        } else {
            pw.print(" finishTime=");
            TimeUtils.formatDuration(this.finishTime, now, pw);
            pw.print(" (");
            TimeUtils.formatDuration(this.finishTime - this.dispatchTime, pw);
            pw.print(" since disp)");
        }
        pw.println("");
        if (this.anrCount != 0) {
            pw.print(prefix);
            pw.print("anrCount=");
            pw.println(this.anrCount);
        }
        if (this.resultData != null) {
            pw.print(prefix);
            pw.print("resultTo=");
            pw.print(this.resultTo);
            pw.print(" resultCode=");
            pw.print(this.resultCode);
            if ("android.intent.action.NEW_OUTGOING_CALL".equals(this.intent.getAction())) {
                pw.print(" resultData=");
                pw.println("xxxxxxxxxxx");
            } else {
                pw.print(" resultData=");
                pw.println(this.resultData);
            }
        }
        if (this.resultExtras != null) {
            pw.print(prefix);
            pw.print("resultExtras=");
            pw.println(this.resultExtras);
        }
        pw.print(prefix);
        pw.print("resultAbort=");
        pw.print(this.resultAbort);
        pw.print(" ordered=");
        pw.print(this.ordered);
        pw.print(" sticky=");
        pw.print(this.sticky);
        pw.print(" initialSticky=");
        pw.println(this.initialSticky);
        pw.print(prefix);
        pw.print("nextReceiver=");
        pw.print(this.nextReceiver);
        pw.print(" receiver=");
        pw.println(this.receiver);
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
            if (this.curComponent != null) {
            }
            pw.println(this.curComponent != null ? this.curComponent.toShortString() : "--");
            pw.print(prefix);
            pw.print("curSourceDir=");
            pw.println(this.curReceiver.applicationInfo.sourceDir);
        }
        if (this.state != 0) {
            stateStr = " (?)";
            switch (this.state) {
                case DELIVERY_DELIVERED /*1*/:
                    stateStr = " (APP_RECEIVE)";
                    break;
                case DELIVERY_SKIPPED /*2*/:
                    stateStr = " (CALL_IN_RECEIVE)";
                    break;
                case DELIVERY_TIMEOUT /*3*/:
                    stateStr = " (CALL_DONE_RECEIVE)";
                    break;
                case WAITING_SERVICES /*4*/:
                    stateStr = " (WAITING_SERVICES)";
                    break;
            }
            pw.print(prefix);
            pw.print("state=");
            pw.print(this.state);
            pw.println(stateStr);
        }
        if (this.receivers != null) {
        }
        p2 = prefix + "  ";
        printer = new PrintWriterPrinter(pw);
        for (i = IDLE; i < N; i += DELIVERY_DELIVERED) {
            o = this.receivers.get(i);
            pw.print(prefix);
            switch (this.delivery[i]) {
                case IDLE /*0*/:
                    pw.print("Pending");
                    break;
                case DELIVERY_DELIVERED /*1*/:
                    pw.print("Deliver");
                    break;
                case DELIVERY_SKIPPED /*2*/:
                    pw.print("Skipped");
                    break;
                case DELIVERY_TIMEOUT /*3*/:
                    pw.print("Timeout");
                    break;
                default:
                    pw.print("???????");
                    break;
            }
            pw.print(" #");
            pw.print(i);
            pw.print(": ");
            if (!(o instanceof BroadcastFilter)) {
                pw.println(o);
                ((BroadcastFilter) o).dumpBrief(pw, p2);
            } else if (o instanceof ResolveInfo) {
                pw.println("(manifest)");
                ((ResolveInfo) o).dump(printer, p2, IDLE);
            } else {
                pw.println(o);
            }
        }
    }

    BroadcastRecord(BroadcastQueue _queue, Intent _intent, ProcessRecord _callerApp, String _callerPackage, int _callingPid, int _callingUid, String _resolvedType, String[] _requiredPermissions, int _appOp, BroadcastOptions _options, List _receivers, IIntentReceiver _resultTo, int _resultCode, String _resultData, Bundle _resultExtras, boolean _serialized, boolean _sticky, boolean _initialSticky, int _userId) {
        this.queue = _queue;
        this.intent = _intent;
        this.targetComp = _intent.getComponent();
        this.callerApp = _callerApp;
        this.callerPackage = _callerPackage;
        this.callingPid = _callingPid;
        this.callingUid = _callingUid;
        this.resolvedType = _resolvedType;
        this.requiredPermissions = _requiredPermissions;
        this.appOp = _appOp;
        this.options = _options;
        this.receivers = _receivers;
        this.delivery = new int[(_receivers != null ? _receivers.size() : IDLE)];
        this.resultTo = _resultTo;
        this.resultCode = _resultCode;
        this.resultData = _resultData;
        this.resultExtras = _resultExtras;
        this.ordered = _serialized;
        this.sticky = _sticky;
        this.initialSticky = _initialSticky;
        this.userId = _userId;
        this.nextReceiver = IDLE;
        this.state = IDLE;
    }

    boolean cleanupDisabledPackageReceiversLocked(String packageName, Set<String> filterByClasses, int userId, boolean doit) {
        if ((userId != -1 && this.userId != userId) || this.receivers == null) {
            return false;
        }
        boolean didSomething = false;
        for (int i = this.receivers.size() - 1; i >= 0; i--) {
            Object o = this.receivers.get(i);
            if (o instanceof ResolveInfo) {
                ActivityInfo info = ((ResolveInfo) o).activityInfo;
                boolean sameComponent = packageName != null ? info.applicationInfo.packageName.equals(packageName) ? filterByClasses != null ? filterByClasses.contains(info.name) : true : false : true;
                if (!sameComponent) {
                    continue;
                } else if (!doit) {
                    return true;
                } else {
                    didSomething = true;
                    this.receivers.remove(i);
                    if (i < this.nextReceiver) {
                        this.nextReceiver--;
                    }
                }
            }
        }
        this.nextReceiver = Math.min(this.nextReceiver, this.receivers.size());
        return didSomething;
    }

    public String toString() {
        return "BroadcastRecord{" + Integer.toHexString(System.identityHashCode(this)) + " u" + this.userId + " " + this.intent.getAction() + "}";
    }
}
