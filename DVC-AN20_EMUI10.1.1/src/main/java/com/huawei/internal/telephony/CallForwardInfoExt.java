package com.huawei.internal.telephony;

import android.os.Bundle;
import com.android.internal.telephony.CallForwardInfo;
import com.huawei.android.os.AsyncResultEx;
import java.util.ArrayList;

public class CallForwardInfoExt {
    private static final String CALLBACK_CF_INFO = "CallForwardInfos";
    private CallForwardInfo mCallForwardInfo;

    private CallForwardInfoExt() {
    }

    private void setCallForwardInfo(CallForwardInfo callForwardInfo) {
        this.mCallForwardInfo = callForwardInfo;
    }

    public static CallForwardInfoExt from(Object object) {
        if (!(object instanceof CallForwardInfo)) {
            return null;
        }
        CallForwardInfoExt callForwardInfoExt = new CallForwardInfoExt();
        callForwardInfoExt.setCallForwardInfo((CallForwardInfo) object);
        return callForwardInfoExt;
    }

    public static CallForwardInfoExt[] fromArray(Object object) {
        if (!(object instanceof CallForwardInfo[])) {
            return null;
        }
        CallForwardInfo[] infos = (CallForwardInfo[]) object;
        int len = infos.length;
        CallForwardInfoExt[] infoExs = new CallForwardInfoExt[len];
        for (int i = 0; i < len; i++) {
            infoExs[i] = new CallForwardInfoExt();
            infoExs[i].setCallForwardInfo(infos[i]);
        }
        return infoExs;
    }

    public static void putDataToBundle(AsyncResultEx ar, Bundle data) {
        CallForwardInfo[] cfiArray;
        if ((ar.getResult() instanceof CallForwardInfo[]) && (cfiArray = (CallForwardInfo[]) ar.getResult()) != null && cfiArray.length > 0) {
            ArrayList<CallForwardInfo> cfiList = new ArrayList<>();
            for (CallForwardInfo callForwardInfo : cfiArray) {
                cfiList.add(callForwardInfo);
            }
            data.putParcelableArrayList(CALLBACK_CF_INFO, cfiList);
        }
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
