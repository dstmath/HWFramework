package com.huawei.servicehost;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.servicehost.normal.IIPEvent4Metadata;
import com.huawei.servicehost.normal.IIPRequest4Metadata;

public class ServiceHostUtil {
    private static final String TAG = "ServiceHostUtil";

    public void setMetadata(IIPRequest4Metadata request, ServiceHostMetadata metadata) {
        Log.d(TAG, "set metadata.");
        if (request == null) {
            Log.e(TAG, "request is null");
            return;
        }
        try {
            request.setMetadata(metadata.getNativeMetadata());
        } catch (RemoteException e) {
            Log.e(TAG, "set metadata error: " + e.getMessage());
        }
    }

    public TotalCaptureResult getTotalCaptureResult(IIPEvent4Metadata event4Metadata) {
        Log.d(TAG, "get total capture result from native metadata.");
        if (event4Metadata == null) {
            Log.e(TAG, "event4Metadata is null");
            return null;
        }
        try {
            CameraMetadataNative metadataNative = event4Metadata.getMetadata();
            if (metadataNative != null && !metadataNative.isEmpty()) {
                return new TotalCaptureResult(metadataNative, -1);
            }
            Log.e(TAG, "result is null!");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "get total capture result error: " + e.getMessage());
            return null;
        }
    }

    public CaptureResult getCaptureResult(IIPEvent4Metadata event4Metadata) {
        Log.d(TAG, "get capture result from native metadata.");
        if (event4Metadata == null) {
            Log.e(TAG, "event4Metadata is null");
            return null;
        }
        try {
            CameraMetadataNative metadataNative = event4Metadata.getMetadata();
            if (metadataNative != null && !metadataNative.isEmpty()) {
                return new CaptureResult(metadataNative, -1);
            }
            Log.e(TAG, "result is null!");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "get capture result error: " + e.getMessage());
            return null;
        }
    }

    public CameraCharacteristics getCharacteristics(IIPEvent4Metadata event4Metadata) {
        Log.d(TAG, "get characteristics from native metadata.");
        if (event4Metadata == null) {
            Log.e(TAG, "event4Metadata is null");
            return null;
        }
        CameraMetadataNative metadataNative = null;
        try {
            metadataNative = event4Metadata.getMetadata();
        } catch (RemoteException e) {
            Log.e(TAG, "get characteristics error: " + e.getMessage());
        }
        if (metadataNative == null) {
            return null;
        }
        return new CameraCharacteristics(metadataNative);
    }
}
