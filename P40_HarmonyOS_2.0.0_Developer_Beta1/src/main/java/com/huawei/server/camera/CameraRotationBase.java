package com.huawei.server.camera;

import com.android.server.wm.HwMagicContainer;
import com.huawei.server.utils.SharedParameters;

public abstract class CameraRotationBase {
    protected static final String FLAG_SPLIT = "/:+";
    protected static final String INVALID_MESSAGE = "-1";
    public static final int TYPE_APP_INFO = 1;
    public static final int TYPE_CAMERA_DEVICE = 2;
    public static final int TYPE_NOT_ROTATE = -1;
    public static final int TYPE_ORIENTATION = 0;
    protected HwMagicContainer mContainer;
    protected boolean mLastNeedRotateCamera = false;
    protected SharedParameters mParameters;

    public abstract void updateCameraRotation(int i);

    protected CameraRotationBase(SharedParameters parameters, HwMagicContainer container) {
        this.mParameters = parameters;
        this.mContainer = container;
    }

    public void updateSensorRotation(int sensorRotation) {
    }

    public void release() {
    }
}
