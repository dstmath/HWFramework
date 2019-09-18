package com.android.server;

import com.android.server.HwServiceFactory;
import huawei.cust.HwCustUtils;

public class HwCustContinuousRebootChecker implements HwServiceFactory.IContinuousRebootChecker {
    private static HwServiceFactory.IContinuousRebootChecker checker = null;

    public static synchronized HwServiceFactory.IContinuousRebootChecker getDefault() {
        HwServiceFactory.IContinuousRebootChecker iContinuousRebootChecker;
        synchronized (HwCustContinuousRebootChecker.class) {
            if (checker == null) {
                checker = (HwServiceFactory.IContinuousRebootChecker) HwCustUtils.createObj(HwCustContinuousRebootChecker.class, new Object[0]);
            }
            iContinuousRebootChecker = checker;
        }
        return iContinuousRebootChecker;
    }

    public void checkAbnormalReboot() {
    }
}
