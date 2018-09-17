package com.android.server.rms.iaware.hiber.listener;

import com.android.server.rms.iaware.memory.data.content.AttrSegments;

class DevStatusHandler extends AbsDataHandler {
    private static DevStatusHandler mDevStatusHandler = null;

    protected static synchronized DevStatusHandler getInstance() {
        DevStatusHandler devStatusHandler;
        synchronized (DevStatusHandler.class) {
            if (mDevStatusHandler == null) {
                mDevStatusHandler = new DevStatusHandler();
            }
            devStatusHandler = mDevStatusHandler;
        }
        return devStatusHandler;
    }

    private DevStatusHandler() {
    }

    protected int reportData(long timestamp, int event, AttrSegments attrSegments) {
        if (this.mAppHibernateTask == null) {
            return -1;
        }
        if (20011 != event && 90011 != event) {
            return -1;
        }
        this.mAppHibernateTask.setScreenState(event);
        return 0;
    }
}
