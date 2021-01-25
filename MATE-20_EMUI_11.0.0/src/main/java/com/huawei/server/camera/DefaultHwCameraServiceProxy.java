package com.huawei.server.camera;

import android.content.Context;
import android.util.Slog;
import com.android.server.camera.IHwCameraServiceProxy;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwCameraServiceProxy implements IHwCameraServiceProxy {
    private static final String TAG = "DefaultHwCameraServiceProxy";

    public void notifyCameraStateChange(String cameraId, int newCameraState, int facing, String clientName) {
        Slog.i(TAG, "Return nothing");
    }

    public void startObservingLightStrapCaseStatus() {
    }

    public void binderDied() {
    }

    public void initHwCameraServiceProxyParams(Context context) {
        Slog.i(TAG, "do nothing in initHwCameraServiceProxyParams");
    }

    public void notifyPopupCamera(String shortCompName) {
        Slog.i(TAG, "do nothing in notifyPopupCamera");
    }

    public boolean isLightStrapCaseOn() {
        Slog.i(TAG, "do nothing in isLightStrapCaseOn");
        return false;
    }
}
