package com.android.server.rms.iaware.hiber.listener;

import com.android.server.rms.iaware.memory.data.content.AttrSegments;

/* access modifiers changed from: package-private */
public class ResInputHandler extends AbsDataHandler {
    private static final Object LOCK = new Object();
    private static ResInputHandler sDataHandle = null;

    private ResInputHandler() {
    }

    protected static ResInputHandler getInstance() {
        ResInputHandler resInputHandler;
        synchronized (LOCK) {
            if (sDataHandle == null) {
                sDataHandle = new ResInputHandler();
            }
            resInputHandler = sDataHandle;
        }
        return resInputHandler;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.rms.iaware.hiber.listener.AbsDataHandler
    public int reportData(long timeStamp, int event, AttrSegments attrSegments) {
        if (this.mAppHibernateTask == null) {
            return -1;
        }
        if (event == 10001 || event == 80001) {
            return this.mAppHibernateTask.setLastInputEventData(event, timeStamp);
        }
        return -1;
    }
}
