package com.android.server.wm;

import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.HwMwUtils;
import android.view.Display;
import android.view.WindowManager;
import com.huawei.server.camera.CameraRotationLocal;
import com.huawei.server.magicwin.HwMagicWinAnimation;
import com.huawei.server.magicwin.HwMagicWindowConfig;
import com.huawei.server.utils.SharedParameters;
import java.util.List;

public class HwMagicLocalContainer extends HwMagicContainer {
    private static final String TAG = "HWMW_HwMagicLocalContainer";

    public HwMagicLocalContainer(SharedParameters parameters, List<Uri> uris, HwMagicWindowConfig.SystemConfig sysCfg) {
        this.mParameters = parameters;
        this.mConfig = new HwMagicWindowConfig(this.mParameters.getContext(), this, uris, sysCfg);
        this.mAnimation = new HwMagicWinAnimation(this.mParameters, this);
        attachDisplayId(0);
        registerEventListener();
        this.mCameraRotation = new CameraRotationLocal(this.mParameters, this);
    }

    @Override // com.android.server.wm.HwMagicContainer, com.huawei.server.magicwin.DeviceAttribute
    public boolean isLocalContainer() {
        return true;
    }

    @Override // com.huawei.server.magicwin.DeviceAttribute
    public DisplayMetrics getDisplayMetrics() {
        Display display = ((WindowManager) this.mParameters.getContext().getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        return metrics;
    }

    @Override // com.android.server.wm.HwMagicContainer, com.huawei.server.magicwin.DeviceAttribute
    public boolean isPadDevice() {
        return HwMwUtils.IS_TABLET;
    }

    @Override // com.android.server.wm.HwMagicContainer, com.huawei.server.magicwin.DeviceAttribute
    public boolean isFoldableDevice() {
        return HwMwUtils.IS_FOLD_SCREEN_DEVICE;
    }

    @Override // com.android.server.wm.HwMagicContainer
    public int getType() {
        return 0;
    }
}
