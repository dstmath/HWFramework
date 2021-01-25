package com.android.server.rms.iaware.hiber.listener;

import com.android.server.rms.iaware.memory.data.content.AttrSegments;

/* access modifiers changed from: package-private */
public class DevStatusHandler extends AbsDataHandler {
    private static final Object LOCK = new Object();
    private static DevStatusHandler sDevStatusHandler = null;

    private DevStatusHandler() {
    }

    protected static DevStatusHandler getInstance() {
        DevStatusHandler devStatusHandler;
        synchronized (LOCK) {
            if (sDevStatusHandler == null) {
                sDevStatusHandler = new DevStatusHandler();
            }
            devStatusHandler = sDevStatusHandler;
        }
        return devStatusHandler;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.rms.iaware.hiber.listener.AbsDataHandler
    public int reportData(long timeStamp, int event, AttrSegments attrSegments) {
        if (this.mAppHibernateTask == null) {
            return -1;
        }
        if (event != 20011 && event != 90011) {
            return -1;
        }
        this.mAppHibernateTask.setScreenState(event);
        return 0;
    }
}
