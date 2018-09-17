package android.rms.iaware;

import android.net.ProxyInfo;
import com.huawei.pgmng.log.LogPower;

public final class LogIAware extends LogPower {
    private LogIAware() {
    }

    public static int report(int tag) {
        return pushIAware(tag, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public static int report(int tag, String msg) {
        return pushIAware(tag, msg);
    }
}
