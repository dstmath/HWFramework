package com.android.server.rms.iaware.memory.data.handle;

import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.utils.PrereadUtils;

public class DataDevStatusHandle extends AbsDataHandle {
    private static final String TAG = "AwareMem_DevHandle";
    private static DataDevStatusHandle sDataHandle;

    public static DataDevStatusHandle getInstance() {
        DataDevStatusHandle dataDevStatusHandle;
        synchronized (DataDevStatusHandle.class) {
            if (sDataHandle == null) {
                sDataHandle = new DataDevStatusHandle();
            }
            dataDevStatusHandle = sDataHandle;
        }
        return dataDevStatusHandle;
    }

    public int reportData(long timestamp, int event, AttrSegments attrSegments) {
        if (event != 20011) {
            if (!(event == 20023 || event == 20025)) {
                if (event == 90011) {
                    AwareLog.d(TAG, "dev status event screen off");
                    this.mDMEServer.disable();
                } else if (event != 90023) {
                    AwareLog.w(TAG, "Dev Status event invalid");
                }
            }
            PrereadUtils.getInstance().reportEvent(event);
        } else {
            AwareLog.d(TAG, "dev status event screen on");
            this.mDMEServer.enable();
        }
        return 0;
    }

    private DataDevStatusHandle() {
    }
}
