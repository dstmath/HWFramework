package com.android.server.wm;

import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.os.IBinder;
import android.view.IApplicationToken;
import com.huawei.annotation.HwSystemApi;

public interface IHwAppWindowTokenEx {
    @HwSystemApi
    void cancelInputMethodRetractAnimation(WindowState windowState);

    int continueHwStartWindow(ApplicationInfo applicationInfo, IBinder iBinder, IApplicationToken iApplicationToken, boolean[] zArr);

    IBinder getTransferFrom(ApplicationInfo applicationInfo);

    boolean isHwMwAnimationBelowStack(AppWindowToken appWindowToken);

    boolean isHwStartWindowEnabled(String str, CompatibilityInfo compatibilityInfo);
}
