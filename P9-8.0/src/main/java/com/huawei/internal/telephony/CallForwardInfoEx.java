package com.huawei.internal.telephony;

import com.android.internal.telephony.CallForwardInfo;

public class CallForwardInfoEx {
    private CallForwardInfo mCallForwardInfo;

    private CallForwardInfoEx(CallForwardInfo callForwardInfo) {
        this.mCallForwardInfo = callForwardInfo;
    }

    public static CallForwardInfoEx from(Object result) {
        if (result == null || !(result instanceof CallForwardInfo)) {
            return null;
        }
        return new CallForwardInfoEx((CallForwardInfo) result);
    }

    public static CallForwardInfoEx[] fromArray(Object result) {
        if (result == null || !(result instanceof CallForwardInfo[])) {
            return new CallForwardInfoEx[0];
        }
        CallForwardInfo[] infos = (CallForwardInfo[]) result;
        int len = infos.length;
        CallForwardInfoEx[] infoExs = new CallForwardInfoEx[len];
        for (int i = 0; i < len; i++) {
            infoExs[i] = new CallForwardInfoEx(infos[i]);
        }
        return infoExs;
    }

    public String getNumber() {
        return this.mCallForwardInfo.number;
    }

    public int getStatus() {
        return this.mCallForwardInfo.status;
    }

    public int getServiceClass() {
        return this.mCallForwardInfo.serviceClass;
    }
}
