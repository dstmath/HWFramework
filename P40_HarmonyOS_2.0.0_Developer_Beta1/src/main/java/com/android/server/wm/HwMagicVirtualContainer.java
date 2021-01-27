package com.android.server.wm;

import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Display;
import com.huawei.android.util.SlogEx;
import com.huawei.server.camera.CameraRotationVirtual;
import com.huawei.server.magicwin.HwMagicWinAnimation;
import com.huawei.server.magicwin.HwMagicWindowConfig;
import com.huawei.server.utils.SharedParameters;
import java.util.List;

public class HwMagicVirtualContainer extends HwMagicContainer {
    private static final String TAG = "HWMW_HwMagicVirtualContainer";
    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    public HwMagicVirtualContainer(SharedParameters parameters, List<Uri> uris, HwMagicWindowConfig.SystemConfig sysCfg) {
        this.mParameters = parameters;
        this.mConfig = new HwMagicWindowConfig(this.mParameters.getContext(), this, uris, sysCfg);
        this.mAnimation = new HwMagicWinAnimation(this.mParameters, this);
        this.mCameraRotation = new CameraRotationVirtual(this.mParameters, this);
    }

    @Override // com.android.server.wm.HwMagicContainer, com.huawei.server.magicwin.DeviceAttribute
    public boolean isVirtualContainer() {
        return true;
    }

    @Override // com.huawei.server.magicwin.DeviceAttribute
    public DisplayMetrics getDisplayMetrics() {
        return this.mDisplayMetrics;
    }

    @Override // com.android.server.wm.HwMagicContainer
    public void updateDisplayMetrics(int width, int height) {
        SlogEx.d(TAG, "Update virtual display metrics. width=" + width + " height=" + height);
        Display display = ((DisplayManager) this.mParameters.getContext().getSystemService("display")).getDisplay(getDisplayId());
        if (display == null) {
            SlogEx.w(TAG, "Get null virtual display. Id=" + getDisplayId());
            return;
        }
        display.getRealMetrics(this.mDisplayMetrics);
        DisplayMetrics displayMetrics = this.mDisplayMetrics;
        displayMetrics.widthPixels = width;
        displayMetrics.heightPixels = height;
        calcHwSplitStackBounds();
        this.mConfig.updateSystemBoundSize(this.mDisplayMetrics);
    }

    @Override // com.android.server.wm.HwMagicContainer
    public ActivityDisplayEx getActivityDisplay() {
        return this.mParameters.getAms().getActivityTaskManagerEx().getActivityDisplayEx(getDisplayId());
    }

    @Override // com.android.server.wm.HwMagicContainer, com.huawei.server.magicwin.DeviceAttribute
    public boolean isPadDevice() {
        return true;
    }

    @Override // com.android.server.wm.HwMagicContainer
    public int getOrientation() {
        return 2;
    }

    @Override // com.android.server.wm.HwMagicContainer
    public int getType() {
        return 1;
    }
}
