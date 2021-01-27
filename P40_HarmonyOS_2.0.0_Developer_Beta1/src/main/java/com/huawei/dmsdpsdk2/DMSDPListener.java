package com.huawei.dmsdpsdk2;

import java.util.Map;

public interface DMSDPListener {
    void onDeviceChange(DMSDPDevice dMSDPDevice, int i, Map<String, Object> map);

    void onDeviceServiceChange(DMSDPDeviceService dMSDPDeviceService, int i, Map<String, Object> map);
}
