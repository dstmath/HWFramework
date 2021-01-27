package com.huawei.internal.telephony;

import android.os.Bundle;
import com.android.internal.telephony.CallForwardInfo;
import java.util.ArrayList;

public class CallForwardInfoExt {
    private static final String CALLBACK_CF_INFO = "CallForwardInfos";
    private static final int INVALID = -1;
    private static final String LOG = "CallForwardInfoExt";
    private CallForwardInfo mCallForwardInfo;

    public static CallForwardInfoExt from(Object object) {
        if (!(object instanceof CallForwardInfo)) {
            return null;
        }
        CallForwardInfoExt callForwardInfoExt = new CallForwardInfoExt();
        callForwardInfoExt.setCallForwardInfo((CallForwardInfo) object);
        return callForwardInfoExt;
    }

    public static CallForwardInfoExt[] fromArray(Object result) {
        if (!(result instanceof CallForwardInfo[])) {
            return new CallForwardInfoExt[0];
        }
        CallForwardInfo[] infos = (CallForwardInfo[]) result;
        int len = infos.length;
        CallForwardInfoExt[] infoExs = new CallForwardInfoExt[len];
        for (int i = 0; i < len; i++) {
            CallForwardInfoExt callForwardInfoExt = new CallForwardInfoExt();
            callForwardInfoExt.setCallForwardInfo(infos[i]);
            infoExs[i] = callForwardInfoExt;
        }
        return infoExs;
    }

    public static void putDataToBundle(Object result, Bundle data) {
        if (result instanceof CallForwardInfo[]) {
            CallForwardInfo[] cfiArray = (CallForwardInfo[]) result;
            if (cfiArray.length > 0) {
                ArrayList<CallForwardInfo> cfiList = new ArrayList<>();
                for (CallForwardInfo callForwardInfo : cfiArray) {
                    cfiList.add(callForwardInfo);
                }
                data.putParcelableArrayList(CALLBACK_CF_INFO, cfiList);
            }
        }
    }

    public CallForwardInfo getCallForwardInfo() {
        return this.mCallForwardInfo;
    }

    public void setCallForwardInfo(CallForwardInfo callForwardInfo) {
        this.mCallForwardInfo = callForwardInfo;
    }

    public String getNumber() {
        CallForwardInfo callForwardInfo = this.mCallForwardInfo;
        if (callForwardInfo != null) {
            return callForwardInfo.number;
        }
        return null;
    }

    public int getStatus() {
        CallForwardInfo callForwardInfo = this.mCallForwardInfo;
        if (callForwardInfo != null) {
            return callForwardInfo.status;
        }
        return -1;
    }

    public int getServiceClass() {
        CallForwardInfo callForwardInfo = this.mCallForwardInfo;
        if (callForwardInfo != null) {
            return callForwardInfo.serviceClass;
        }
        return -1;
    }
}
