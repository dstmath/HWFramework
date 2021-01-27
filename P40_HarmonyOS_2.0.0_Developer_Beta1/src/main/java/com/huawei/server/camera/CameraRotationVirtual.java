package com.huawei.server.camera;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.HwMagicContainer;
import com.android.server.wm.HwMagicWinAmsPolicy;
import com.huawei.android.util.SlogEx;
import com.huawei.server.utils.SharedParameters;
import com.huawei.server.utils.Utils;

public class CameraRotationVirtual extends CameraRotationBase {
    private static final String TAG = "HWMW_CameraRotationVirtual";
    private int mCameraDevice = -1;
    private ContentObserver mCameraDeviceObserver = new ContentObserver(new Handler()) {
        /* class com.huawei.server.camera.CameraRotationVirtual.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            CameraRotationVirtual.this.updateCameraDeviceType();
        }
    };
    private String mLastMessage = "";
    private String mPackageName = "";
    private int mPid = 0;
    private int mSensorRotation = 0;

    public CameraRotationVirtual(SharedParameters parameters, HwMagicContainer container) {
        super(parameters, container);
        registerCameraDeviceObserver();
    }

    @Override // com.huawei.server.camera.CameraRotationBase
    public void release() {
        unregisterCameraDeviceObserver();
    }

    @Override // com.huawei.server.camera.CameraRotationBase
    public void updateCameraRotation(int dataType) {
        if (dataType == 0) {
            updateRotationProp(isFullScreenActivity(null));
        } else if (dataType == 1) {
            updateAppInfo();
        } else if (dataType != 2) {
            updateRotationProp(false);
        } else {
            updateCameraDeviceType();
        }
    }

    @Override // com.huawei.server.camera.CameraRotationBase
    public void updateSensorRotation(int sensorRotation) {
        if (sensorRotation != this.mSensorRotation) {
            SlogEx.i(TAG, "updateSensorRotation sensor=" + sensorRotation);
            this.mSensorRotation = sensorRotation;
            updateRotationProp(isFullScreenActivity(null));
        }
    }

    /* access modifiers changed from: protected */
    public void updateCameraDeviceType() {
        int cameraDeviceType = getCameraDeviceType();
        if (this.mCameraDevice != cameraDeviceType) {
            SlogEx.i(TAG, "updateCameraDeviceType cameraDevice=" + cameraDeviceType);
            this.mCameraDevice = cameraDeviceType;
            updateRotationProp(isFullScreenActivity(null));
        }
    }

    private void updateAppInfo() {
        String packageName = "";
        int pid = 0;
        ActivityRecordEx topActivity = this.mParameters.getMwWinManager().getAmsPolicy().getTopActivity(this.mContainer);
        if (!(topActivity == null || topActivity.getAppEx() == null)) {
            packageName = topActivity.getAppEx().getInfo().packageName;
            pid = topActivity.getAppEx().getPid();
        }
        this.mPackageName = packageName;
        this.mPid = pid;
        updateRotationProp(isFullScreenActivity(topActivity));
    }

    private String getRotationMessage() {
        return String.join("/:+", String.valueOf(this.mCameraDevice), this.mPackageName, String.valueOf(this.mPid), String.valueOf(this.mSensorRotation * 90));
    }

    private int getCameraDeviceType() {
        return Settings.Global.getInt(this.mParameters.getContext().getContentResolver(), Utils.SETTINGS_DEVICETYPE_FOR_OSD_CAMERA, -1);
    }

    private void registerCameraDeviceObserver() {
        this.mParameters.getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor(Utils.SETTINGS_DEVICETYPE_FOR_OSD_CAMERA), true, this.mCameraDeviceObserver);
        updateCameraDeviceType();
    }

    private void unregisterCameraDeviceObserver() {
        this.mParameters.getContext().getContentResolver().unregisterContentObserver(this.mCameraDeviceObserver);
        updateRotationProp(false);
    }

    private void updateRotationProp(boolean isNeedRotateCamera) {
        String sendMsg = isNeedRotateCamera ? getRotationMessage() : "-1";
        if (!sendMsg.equals(this.mLastMessage)) {
            Message msg = this.mParameters.getMwWinManager().getHandler().obtainMessage(23);
            msg.obj = sendMsg;
            this.mParameters.getMwWinManager().getHandler().removeMessages(23);
            this.mParameters.getMwWinManager().getHandler().sendMessage(msg);
            this.mLastMessage = sendMsg;
            SlogEx.i(TAG, "updateRotationProp sendMessage=" + sendMsg);
        }
    }

    private boolean isFullScreenActivity(ActivityRecordEx topActivity) {
        HwMagicWinAmsPolicy hwMagicWinAmsPolicy = this.mParameters.getMwWinManager().getAmsPolicy();
        if (topActivity == null && (topActivity = hwMagicWinAmsPolicy.getTopActivity(this.mContainer)) == null) {
            return false;
        }
        if (hwMagicWinAmsPolicy.isFullScreenActivity(topActivity) || (topActivity.getWindowingMode() == 1 && this.mContainer.checkPosition(topActivity.getBounds(), 5))) {
            return true;
        }
        return false;
    }
}
