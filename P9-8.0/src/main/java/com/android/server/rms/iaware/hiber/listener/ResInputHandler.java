package com.android.server.rms.iaware.hiber.listener;

import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.huawei.displayengine.IDisplayEngineService;

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

    protected int reportData(long timestamp, int event, AttrSegments attrSegments) {
        if (this.mAppHibernateTask == null) {
            return -1;
        }
        if (IDisplayEngineService.DE_ACTION_PG_BROWSER_FRONT == event || 80001 == event) {
            return this.mAppHibernateTask.setLastInputEventData(event, timestamp);
        }
        return -1;
    }
}
