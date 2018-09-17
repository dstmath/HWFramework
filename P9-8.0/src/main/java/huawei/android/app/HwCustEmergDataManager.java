package huawei.android.app;

import huawei.cust.HwCustUtils;

public class HwCustEmergDataManager {
    private static final String EMERGENCY_PKG_NAME = "";
    private static HwCustEmergDataManager mHwCustEmergDataManager = null;

    public static synchronized HwCustEmergDataManager getDefault() {
        HwCustEmergDataManager hwCustEmergDataManager;
        synchronized (HwCustEmergDataManager.class) {
            if (mHwCustEmergDataManager == null) {
                mHwCustEmergDataManager = (HwCustEmergDataManager) HwCustUtils.createObj(HwCustEmergDataManager.class, new Object[0]);
            }
            hwCustEmergDataManager = mHwCustEmergDataManager;
        }
        return hwCustEmergDataManager;
    }

    public boolean isEmergencyState() {
        return false;
    }

    public boolean isEmergencyMountState() {
        return false;
    }

    public void backupEmergencyDataFile() {
    }

    public String getEmergencyPkgName() {
        return "";
    }
}
