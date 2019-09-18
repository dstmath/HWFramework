package com.huawei.android.filemonitor;

import android.os.Bundle;
import android.util.Slog;
import huawei.android.filemonitor.HwFileMonitorManager;
import java.util.List;

public class HwFileMonitorEx {
    public static int request(Bundle bundle) {
        return HwFileMonitorManager.getInstance().request(bundle);
    }

    public static List<String> getPolicy() {
        return HwFileMonitorManager.getInstance().getPolicy();
    }

    public static boolean setPolicy(List<String> policy) {
        if (policy != null && !policy.isEmpty()) {
            return HwFileMonitorManager.getInstance().setPolicy(policy);
        }
        Slog.d("", "setPolicy  is null");
        return false;
    }
}
