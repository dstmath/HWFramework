package com.android.server.rms.iaware.hiber.listener;

import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;

class ResAppHandler extends AbsDataHandler {
    private static ResAppHandler mDataHandle = null;

    protected static synchronized ResAppHandler getInstance() {
        ResAppHandler resAppHandler;
        synchronized (ResAppHandler.class) {
            if (mDataHandle == null) {
                mDataHandle = new ResAppHandler();
            }
            resAppHandler = mDataHandle;
        }
        return resAppHandler;
    }

    private ResAppHandler() {
    }

    protected int reportData(long timestamp, int event, AttrSegments attrSegments) {
        if (this.mAppHibernateTask == null) {
            return -1;
        }
        ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
        if (appInfo == null) {
            AwareLog.w("AppHiber_AbsDataHandler", "appInfo is NULL");
            return -1;
        }
        int retValue = -1;
        if (15005 == event) {
            try {
                retValue = this.mAppHibernateTask.interruptReclaim(Integer.parseInt((String) appInfo.get("uid")), (String) appInfo.get(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY), timestamp);
            } catch (NumberFormatException e) {
                AwareLog.e("AppHiber_AbsDataHandler", "get uid fail, happend NumberFormatException");
            }
        }
        return retValue;
    }
}
