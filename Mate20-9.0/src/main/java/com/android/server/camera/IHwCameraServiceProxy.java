package com.android.server.camera;

public interface IHwCameraServiceProxy {
    void notifyCameraStateChange(String str, int i, int i2, String str2);

    void updateActivityCount(String str, int i, int i2, String str2);
}
