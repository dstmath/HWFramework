package huawei.android.cover;

import android.os.SystemProperties;

public class HwCustCoverManagerUtilsImpl extends HwCustCoverManagerUtils {
    private static final boolean IS_SHOW_SMART_COVER;

    static {
        boolean z;
        if (SystemProperties.getBoolean("ro.config.show_smart_cover", false)) {
            z = true;
        } else {
            z = SystemProperties.getBoolean("ro.config.cover_max_bright", false);
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
