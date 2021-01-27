package com.android.server.rms.iaware.memory.data.handle;

import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.utils.PrereadUtils;
import com.android.server.rms.memrepair.SystemAppMemRepairMng;

public class DataDevStatusHandle extends AbsDataHandle {
    private static final Object LOCK = new Object();
    private static final String TAG = "AwareMem_DevHandle";
    private static DataDevStatusHandle sDataHandle;

    private DataDevStatusHandle() {
    }

    public static DataDevStatusHandle getInstance() {
        DataDevStatusHandle dataDevStatusHandle;
        synchronized (LOCK) {
            if (sDataHandle == null) {
                sDataHandle = new DataDevStatusHandle();
            }
            dataDevStatusHandle = sDataHandle;
        }
        return dataDevStatusHandle;
    }

    @Override // com.android.server.rms.iaware.memory.data.handle.AbsDataHandle
    public int reportData(long timeStamp, int event, AttrSegments attrSegments) {
        if (event != 20011) {
            if (!(event == 20023 || event == 20025)) {
                if (event == 90011) {
                    AwareLog.d(TAG, "dev status event screen off");
                    this.mDmeServer.disable();
                    SystemAppMemRepairMng.getInstance().reportData(90011);
                    return 0;
                } else if (event != 90023) {
                    AwareLog.w(TAG, "Dev Status event invalid");
                    return 0;
                }
            }
            PrereadUtils.getInstance().reportEvent(event);
            return 0;
        }
        AwareLog.d(TAG, "dev status event screen on");
        this.mDmeServer.enable();
        SystemAppMemRepairMng.getInstance().reportData(20011);
        return 0;
    }
}
