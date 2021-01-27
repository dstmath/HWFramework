package com.huawei.dmsdp.devicevirtualization;

import java.util.Map;

public interface CameraDataCallback {
    void onVirCameraBufferDone(byte[] bArr, Map<String, Object> map);
}
