package huawei.android.cover;

import huawei.cust.HwCustUtils;

public class HwCustCoverManagerUtils {
    private static HwCustCoverManagerUtils mHwCustCoverManagerUtils = null;

    public static synchronized HwCustCoverManagerUtils getDefault() {
        HwCustCoverManagerUtils hwCustCoverManagerUtils;
        synchronized (HwCustCoverManagerUtils.class) {
            if (mHwCustCoverManagerUtils == null) {
                mHwCustCoverManagerUtils = (HwCustCoverManagerUtils) HwCustUtils.createObj(HwCustCoverManagerUtils.class, new Object[0]);
            }
            hwCustCoverManagerUtils = mHwCustCoverManagerUtils;
        }
        return hwCustCoverManagerUtils;
    }

    public boolean isSupportSmartCover() {
        return false;
    }
}
