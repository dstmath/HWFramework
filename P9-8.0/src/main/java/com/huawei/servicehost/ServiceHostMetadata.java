package com.huawei.servicehost;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Key;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.util.Log;

public class ServiceHostMetadata {
    private static final boolean DEBUG = false;
    private static final String TAG = "ServiceHostMetadata";
    private CameraMetadataNative mNativeMeta;

    public ServiceHostMetadata(CameraCharacteristics character) {
        this.mNativeMeta = character.getNativeCopy();
    }

    public ServiceHostMetadata(CaptureRequest request) {
        this.mNativeMeta = request.getNativeCopy();
    }

    public ServiceHostMetadata(CaptureResult result) {
        this.mNativeMeta = result.getNativeCopy();
    }

    public <T> void set(Key<T> key, T value) {
        Log.i(TAG, "set metadata (" + key.getName() + ", " + value + ")");
        this.mNativeMeta.set(key, value);
    }

    public CameraMetadataNative getNativeMetadata() {
        return this.mNativeMeta;
    }
}
