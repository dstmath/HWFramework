package com.android.server.am;

import android.app.BroadcastOptions;
import android.app.BroadcastOptionsEx;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import com.huawei.android.content.IIntentReceiverEx;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class BroadcastRecordEx {
    public static final int IDLE = 0;
    private BroadcastRecord mBroadcastRecord;
    public BroadcastOptionsEx options;
    public BroadcastQueueEx queue;

    public BroadcastRecordEx() {
    }

    public BroadcastRecordEx(BroadcastRecord br) {
        if (br != null) {
            this.mBroadcastRecord = br;
            this.queue = new BroadcastQueueEx(br.queue);
            this.options = new BroadcastOptionsEx(br.options);
        }
    }

    public BroadcastRecord getBroadcastRecord() {
        return this.mBroadcastRecord;
    }

    public BroadcastRecordEx(BroadcastQueueEx queueEx, Intent intent, ProcessRecordEx callerAppEx, String callerPackage, int callingPid, int callingUid, boolean callerInstantApp, String resolvedType, String[] requiredPermissions, int appOp, BroadcastOptionsEx optionsEx, List receivers, IIntentReceiverEx resultTo, int resultCode, String resultData, Bundle resultExtras, boolean serialized, boolean sticky, boolean initialSticky, int userId, boolean allowBackgroundActivityStarts, boolean timeoutExempt) {
        BroadcastOptions broadcastOptions = null;
        IIntentReceiver resultToRec = resultTo == null ? null : resultTo.getIntentReceiver();
        BroadcastQueue broadcastQueue = queueEx == null ? null : queueEx.getBroadcastQueue();
        ProcessRecord callerApp = callerAppEx == null ? null : callerAppEx.getProcessRecord();
        broadcastOptions = optionsEx != null ? optionsEx.getBroadcastOptions() : broadcastOptions;
        this.mBroadcastRecord = new BroadcastRecord(broadcastQueue, intent, callerApp, callerPackage, callingPid, callingUid, callerInstantApp, resolvedType, requiredPermissions, appOp, broadcastOptions, convertReceivers(receivers), resultToRec, resultCode, resultData, resultExtras, serialized, sticky, initialSticky, userId, allowBackgroundActivityStarts, timeoutExempt);
        this.queue = new BroadcastQueueEx(broadcastQueue);
        this.options = new BroadcastOptionsEx(broadcastOptions);
    }

    private List<Object> convertReceivers(List receivers) {
        List<Object> list = new ArrayList<>();
        if (receivers == null || receivers.isEmpty()) {
            return list;
        }
        for (Object obj : receivers) {
            if (obj instanceof BroadcastFilterEx) {
                list.add(((BroadcastFilterEx) obj).getBroadcastFilter());
            } else if (obj instanceof ResolveInfo) {
                list.add((ResolveInfo) obj);
            } else {
                list.add(obj);
            }
        }
        return list;
    }

    public ProcessRecordEx getCallerApp() {
        return new ProcessRecordEx(this.mBroadcastRecord.callerApp);
    }

    public int getCallingPid() {
        return this.mBroadcastRecord.callingPid;
    }

    public int getCallingUid() {
        return this.mBroadcastRecord.callingUid;
    }

    public Intent getIntent() {
        return this.mBroadcastRecord.intent;
    }

    public boolean getCallerInstantApp() {
        return this.mBroadcastRecord.callerInstantApp;
    }

    public String getResolvedType() {
        return this.mBroadcastRecord.resolvedType;
    }

    public String[] getRequiredPermissions() {
        return this.mBroadcastRecord.requiredPermissions;
    }

    public int getAppOp() {
        return this.mBroadcastRecord.appOp;
    }

    public IIntentReceiverEx getResultTo() {
        return new IIntentReceiverEx(this.mBroadcastRecord.resultTo);
    }

    public boolean getOrdered() {
        return this.mBroadcastRecord.ordered;
    }

    public boolean getSticky() {
        return this.mBroadcastRecord.sticky;
    }

    public boolean getInitialSticky() {
        return this.mBroadcastRecord.initialSticky;
    }

    public int getUserId() {
        return this.mBroadcastRecord.userId;
    }

    public boolean getAllowBackgroundActivityStarts() {
        return this.mBroadcastRecord.allowBackgroundActivityStarts;
    }

    public boolean getTimeoutExempt() {
        return this.mBroadcastRecord.timeoutExempt;
    }

    public long getDispatchTime() {
        return this.mBroadcastRecord.dispatchTime;
    }

    public long getDispatchClockTime() {
        return this.mBroadcastRecord.dispatchClockTime;
    }

    public List getReceivers() {
        return this.mBroadcastRecord.receivers;
    }

    public String toString() {
        return this.mBroadcastRecord.toString();
    }

    public String getCallerPackage() {
        return this.mBroadcastRecord.callerPackage;
    }

    public int getIawareCtrlType() {
        return this.mBroadcastRecord.iawareCtrlType;
    }

    public int getResultCode() {
        return this.mBroadcastRecord.resultCode;
    }

    public String getResultData() {
        return this.mBroadcastRecord.resultData;
    }

    public Bundle getResultExtras() {
        return this.mBroadcastRecord.resultExtras;
    }

    public boolean getResultAbort() {
        return this.mBroadcastRecord.resultAbort;
    }

    public int getNextReceiver() {
        return this.mBroadcastRecord.nextReceiver;
    }

    public void setState(int state) {
        this.mBroadcastRecord.state = state;
    }

    public void setDispatchClockTime(long dispatchClockTime) {
        this.mBroadcastRecord.dispatchClockTime = dispatchClockTime;
    }

    public void setDispatchTime(long dispatchTime) {
        this.mBroadcastRecord.dispatchTime = dispatchTime;
    }

    public void setIawareCtrlType(int awareCtrlType) {
        this.mBroadcastRecord.iawareCtrlType = awareCtrlType;
    }

    public boolean isRecordNull() {
        return this.mBroadcastRecord == null;
    }
}
