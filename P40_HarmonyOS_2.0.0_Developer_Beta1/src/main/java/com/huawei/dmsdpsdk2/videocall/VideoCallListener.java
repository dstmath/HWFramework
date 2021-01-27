package com.huawei.dmsdpsdk2.videocall;

public interface VideoCallListener {
    void onDeviceFound(VideoCallDevice videoCallDevice);

    void onDeviceLost(VideoCallDevice videoCallDevice);

    void onPinEvent(VideoCallDevice videoCallDevice, int i);

    void onServiceFailed(VideoCallDevice videoCallDevice, int i, int i2);

    void onServiceSuccess(VideoCallDevice videoCallDevice, int i, int i2);
}
