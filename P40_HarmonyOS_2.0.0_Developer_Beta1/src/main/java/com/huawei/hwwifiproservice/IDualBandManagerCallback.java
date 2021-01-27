package com.huawei.hwwifiproservice;

import java.util.List;

public interface IDualBandManagerCallback {
    void onDualBandNetWorkFind(List<HwDualBandMonitorInfo> list, int i);

    void onDualBandNetWorkType(int i, List<HwDualBandMonitorInfo> list, int i2);
}
