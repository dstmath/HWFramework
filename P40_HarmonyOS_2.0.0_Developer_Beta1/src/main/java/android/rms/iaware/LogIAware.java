package android.rms.iaware;

import com.huawei.hwpartiaware.BuildConfig;
import com.huawei.pgmng.log.LogPower;

public final class LogIAware extends LogPower {
    private LogIAware() {
    }

    public static int report(int tag) {
        return pushIAware(tag, BuildConfig.FLAVOR);
    }

    public static int report(int tag, String msg) {
        return pushIAware(tag, msg);
    }
}
