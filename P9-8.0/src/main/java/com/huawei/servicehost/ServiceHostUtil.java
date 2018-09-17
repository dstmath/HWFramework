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
    private static final boolean DEBUG = false;
    private static final String TAG = "ServiceHostUtil";

    public void setMetadata(IIPRequest4Metadata request, ServiceHostMetadata metadata) {
        try {
            request.setMetadata(metadata.getNativeMetadata());
        } catch (RemoteException e) {
            Log.e(TAG, "set metadata error: " + e.getMessage());
        }
    }

    public TotalCaptureResult getTotalCaptureResult(IIPEvent4Metadata event4Metadata) {
        CameraMetadataNative cameraMetadataNative = null;
        try {
            cameraMetadataNative = event4Metadata.getMetadata();
            if (cameraMetadataNative == null) {
                Log.e(TAG, "result is null!");
                return null;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "get total capture result error: " + e.getMessage());
        }
        return new TotalCaptureResult(cameraMetadataNative, -1);
    }

    public CaptureResult getCaptureResult(IIPEvent4Metadata event4Metadata) {
        CameraMetadataNative metadataNative = null;
        try {
            metadataNative = event4Metadata.getMetadata();
        } catch (RemoteException e) {
            Log.e(TAG, "get capture result error: " + e.getMessage());
        }
        return new CaptureResult(metadataNative, -1);
    }

    public CameraCharacteristics getCharacteristics(IIPEvent4Metadata event4Metadata) {
        CameraMetadataNative metadataNative = null;
        try {
            metadataNative = event4Metadata.getMetadata();
        } catch (RemoteException e) {
            Log.e(TAG, "get characteristics error: " + e.getMessage());
        }
        return new CameraCharacteristics(metadataNative);
    }
}
