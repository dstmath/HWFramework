package com.huawei.android.hwforcedark;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.WindowManager;

public class HwForceDarkManagerDummy implements IHwForceDarkManager {
    private static HwForceDarkManagerDummy sInstances = new HwForceDarkManagerDummy();

    public static IHwForceDarkManager getDefault() {
        return sInstances;
    }

    @Override // com.huawei.android.hwforcedark.IHwForceDarkManager
    public int updateHwForceDarkState(Context context, View rootView, WindowManager.LayoutParams lp) {
        return 0;
    }

    @Override // com.huawei.android.hwforcedark.IHwForceDarkManager
    public boolean setAllowedHwForceDark(Context context, Canvas canvas, int hwForceDarkState, boolean isViewAllowedForceDark, WindowManager.LayoutParams lp) {
        return isViewAllowedForceDark;
    }

    @Override // com.huawei.android.hwforcedark.IHwForceDarkManager
    public int updateHwForceDarkSystemUIVisibility(int systemUIVisiblity) {
        return systemUIVisiblity;
    }
}
