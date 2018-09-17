package com.huawei.android.hardware.toucforce;

import android.content.Context;
import android.util.Flog;
import huawei.android.os.HwGeneralManager;

public class TouchForceManager {
    HwGeneralManager mGeneralManage;
    private boolean mIsSupportPressure;

    public TouchForceManager(Context context) {
        this.mIsSupportPressure = false;
        this.mGeneralManage = null;
        this.mGeneralManage = HwGeneralManager.getInstance();
    }

    public boolean isSupportForce() {
        if (this.mGeneralManage != null) {
            this.mIsSupportPressure = this.mGeneralManage.isSupportForce();
        }
        return this.mIsSupportPressure;
    }

    public boolean isForceAvailble(float pressure) {
        if (isSupportForce()) {
            float val = 0.0f;
            if (this.mGeneralManage != null) {
                val = this.mGeneralManage.getPressureLimit();
            }
            if (val <= 0.0f || pressure < val) {
                return false;
            }
            return true;
        }
        Flog.e(1504, "donot support pressure");
        return false;
    }

    public float getPressureLimit() {
        if (!isSupportForce() || this.mGeneralManage == null) {
            return 0.0f;
        }
        return this.mGeneralManage.getPressureLimit();
    }
}
