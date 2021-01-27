package com.android.server.rms.iaware.hiber.listener;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;

/* access modifiers changed from: package-private */
public class ResAppHandler extends AbsDataHandler {
    private static final Object LOCK = new Object();
    private static ResAppHandler sDataHandle = null;

    ResAppHandler() {
    }

    protected static ResAppHandler getInstance() {
        ResAppHandler resAppHandler;
        synchronized (LOCK) {
            if (sDataHandle == null) {
                sDataHandle = new ResAppHandler();
            }
            resAppHandler = sDataHandle;
        }
        return resAppHandler;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.rms.iaware.hiber.listener.AbsDataHandler
    public int reportData(long timeStamp, int event, AttrSegments attrSegments) {
        if (this.mAppHibernateTask == null) {
            return -1;
        }
        ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
        if (appInfo == null) {
            AwareLog.w("AppHiber_AbsDataHandler", "appInfo is NULL");
            return -1;
        } else if (event != 15005) {
            return -1;
        } else {
            try {
                return this.mAppHibernateTask.interruptReclaim(Integer.parseInt(appInfo.get("uid")), appInfo.get("packageName"), timeStamp);
            } catch (NumberFormatException e) {
                AwareLog.e("AppHiber_AbsDataHandler", "get uid fail, happend NumberFormatException");
                return -1;
            }
        }
    }
}
