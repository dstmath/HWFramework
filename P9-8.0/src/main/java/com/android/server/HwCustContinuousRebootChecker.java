package com.android.server;

import com.android.server.HwServiceFactory.IContinuousRebootChecker;
import huawei.cust.HwCustUtils;

public class HwCustContinuousRebootChecker implements IContinuousRebootChecker {
    private static IContinuousRebootChecker checker = null;

    public static synchronized IContinuousRebootChecker getDefault() {
        IContinuousRebootChecker iContinuousRebootChecker;
        synchronized (HwCustContinuousRebootChecker.class) {
            if (checker == null) {
                checker = (IContinuousRebootChecker) HwCustUtils.createObj(HwCustContinuousRebootChecker.class, new Object[0]);
            }
            iContinuousRebootChecker = checker;
        }
        return iContinuousRebootChecker;
    }

    public void checkAbnormalReboot() {
    }
}
