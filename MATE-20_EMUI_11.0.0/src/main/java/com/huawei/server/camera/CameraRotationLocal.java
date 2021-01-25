package com.huawei.server.camera;

import android.os.Message;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.HwMagicContainer;
import com.huawei.android.util.SlogEx;
import com.huawei.server.utils.SharedParameters;
import com.huawei.server.utils.Utils;

public class CameraRotationLocal extends CameraRotationBase {
    private static final String TAG = "HWMW_CameraRotationLocal";
    private int mDisplayRotation = -1;
    private String mLastMessage = "";

    public CameraRotationLocal(SharedParameters parameters, HwMagicContainer container) {
        super(parameters, container);
    }

    @Override // com.huawei.server.camera.CameraRotationBase
    public void updateCameraRotation(int dataType) {
        ActivityRecordEx topActivity;
        if (!this.mContainer.isFoldableDevice() && (topActivity = this.mParameters.getMwWinManager().getAmsPolicy().getTopActivity(this.mContainer)) != null) {
            boolean isNeedRotateCamera = (topActivity.inHwMagicWindowingMode() && !this.mParameters.getMwWinManager().isFull(topActivity)) || this.mParameters.getMwWinManager().getAmsPolicy().isShowDragBar(this.mContainer);
            int displayRotation = getDisplayRotation();
            if (isNeedRotateCamera != this.mLastNeedRotateCamera || displayRotation != this.mDisplayRotation) {
                SlogEx.i(TAG, "updateCameraRotation");
                this.mDisplayRotation = displayRotation;
                updateRotationProp(isNeedRotateCamera);
            }
        }
    }

    private void updateRotationProp(boolean needRotateCamera) {
        this.mLastNeedRotateCamera = needRotateCamera;
        Message msg = this.mParameters.getMwWinManager().getHandler().obtainMessage(3);
        String rotationProp = getRotationMessage(Utils.getRealPkgName(this.mParameters.getMwWinManager().getAmsPolicy().getTopActivity(this.mContainer)));
        msg.obj = needRotateCamera ? rotationProp : "-1";
        this.mParameters.getMwWinManager().getHandler().removeMessages(3);
        this.mParameters.getMwWinManager().getHandler().sendMessage(msg);
        this.mLastMessage = rotationProp;
    }

    private int getDisplayRotation() {
        return this.mParameters.getAms().getWindowManagerServiceEx().getDefaultDisplayRotation();
    }

    private String getRotationMessage(String pkg) {
        return String.join("/:+", pkg, String.valueOf(this.mDisplayRotation));
    }
}
