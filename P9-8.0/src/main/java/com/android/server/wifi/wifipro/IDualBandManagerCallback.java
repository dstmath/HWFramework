package com.android.server.wifi.wifipro;

import java.util.List;

public interface IDualBandManagerCallback {
    void onDualBandNetWorkFind(List<HwDualBandMonitorInfo> list);

    void onDualBandNetWorkType(int i, List<HwDualBandMonitorInfo> list);
}
