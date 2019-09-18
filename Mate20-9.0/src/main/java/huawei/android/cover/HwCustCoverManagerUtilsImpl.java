package huawei.android.cover;

import android.os.SystemProperties;

public class HwCustCoverManagerUtilsImpl extends HwCustCoverManagerUtils {
    private static final boolean IS_SHOW_SMART_COVER;

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.config.show_smart_cover", false) || SystemProperties.getBoolean("ro.config.cover_max_bright", false)) {
            z = true;
        }
        IS_SHOW_SMART_COVER = z;
    }

    public boolean isSupportSmartCover() {
        if (IS_SHOW_SMART_COVER) {
            return true;
        }
        return false;
    }
}
