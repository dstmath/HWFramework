package com.huawei.server.security.securitydiagnose;

import android.common.HwFrameworkSecurityPartsFactory;
import android.util.HiLog;
import android.util.HiLogLabel;
import com.huawei.hwstp.HwStpHidlAdapter;

class KernelDetectionCallback implements HwStpHidlAdapter.KernelDetectionCallbackWrapper {
    private static final int DOMAIN = 218115849;
    private static final HiLogLabel HILOG_LABEL = new HiLogLabel(3, (int) DOMAIN, TAG);
    private static final int KERNEL_DESTRUCTION = 6;
    private static final String TAG = "Module Kernel Detection";

    KernelDetectionCallback() {
    }

    public void onEvent(int uid, int pid, int isMalApp) {
        HwFrameworkSecurityPartsFactory.getInstance().getInnerHwBehaviorCollectManager().sendEvent((int) KERNEL_DESTRUCTION, uid, isMalApp, (String) null, (String) null);
        HiLog.info(HILOG_LABEL, "sendEvent succeed", new Object[0]);
    }
}
