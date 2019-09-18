package com.huawei.android.os.storage;

import android.os.storage.DiskInfo;
import android.telephony.HwCarrierConfigManager;
import com.huawei.android.app.AppOpsManagerEx;

public class DiskInfoEx {
    public static final int TYPE_HONOR_EARL = 1;
    public static final int TYPE_HUAWEI_EARL = 0;
    private DiskInfo mDiskInfo;

    public DiskInfoEx(DiskInfo info) {
        this.mDiskInfo = info;
    }

    public boolean isSd() {
        return this.mDiskInfo.isSd();
    }

    public boolean isUsb() {
        return this.mDiskInfo.isUsb();
    }

    public boolean isUsbPartner(int type) {
        boolean z = false;
        if (type == 0) {
            if ((this.mDiskInfo.flags & HwCarrierConfigManager.HD_ICON_MASK_DIALER) != 0) {
                z = true;
            }
            return z;
        } else if (type != 1) {
            return false;
        } else {
            if ((this.mDiskInfo.flags & AppOpsManagerEx.TYPE_MICROPHONE) != 0) {
                z = true;
            }
            return z;
        }
    }
}
