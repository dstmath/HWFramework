package com.android.server.rms.iaware.hiber.listener;

import com.android.server.rms.iaware.memory.data.content.AttrSegments;

class ResInputHandler extends AbsDataHandler {
    private static ResInputHandler sDataHandle = null;

    protected static synchronized ResInputHandler getInstance() {
        ResInputHandler resInputHandler;
        synchronized (ResInputHandler.class) {
            if (sDataHandle == null) {
                sDataHandle = new ResInputHandler();
            }
            resInputHandler = sDataHandle;
        }
        return resInputHandler;
    }

    private ResInputHandler() {
    }

    /* access modifiers changed from: protected */
    public int reportData(long timestamp, int event, AttrSegments attrSegments) {
        if (this.mAppHibernateTask == null) {
            return -1;
        }
        if (10001 == event || 80001 == event) {
            return this.mAppHibernateTask.setLastInputEventData(event, timestamp);
        }
        return -1;
    }
}
