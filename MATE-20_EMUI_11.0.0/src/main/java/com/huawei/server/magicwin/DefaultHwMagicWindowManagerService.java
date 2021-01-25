package com.huawei.server.magicwin;

import android.content.Context;
import android.graphics.Bitmap;
import android.magicwin.IHwMagicWindow;
import android.os.Bundle;
import android.os.IBinder;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.wm.ActivityStackEx;
import com.android.server.wm.HwActivityTaskManagerServiceEx;
import com.android.server.wm.WindowManagerServiceEx;
import com.huawei.android.view.HwTaskSnapshotWrapper;
import com.huawei.annotation.HwSystemApi;
import java.util.List;
import java.util.Map;

@HwSystemApi
public class DefaultHwMagicWindowManagerService extends IHwMagicWindow.Stub {
    public DefaultHwMagicWindowManagerService(Context context, ActivityManagerServiceEx amsEx, WindowManagerServiceEx wmsEx) {
    }

    public Bundle invokeSync(String packageName, String method, String params, Bundle objects) {
        return Bundle.EMPTY;
    }

    public void invokeAsync(String packageName, String method, String params, Bundle objects, IBinder callback) {
    }

    public Bundle performHwMagicWindowPolicy(int policy, List params) {
        return Bundle.EMPTY;
    }

    public Map getHwMagicWinEnabledApps() {
        return null;
    }

    public boolean setHwMagicWinEnabled(String pkg, boolean enabled) {
        return false;
    }

    public boolean getHwMagicWinEnabled(String pkg) {
        return false;
    }

    public boolean notifyConnectionState(boolean isSink, boolean isConnected) {
        return false;
    }

    public void updateAppMagicWinStatusInMultiDevice(int reason, int targetDisplayid, int targetWidth, int targetHeight) {
    }

    public boolean isSupportMagicWindowSink() {
        return false;
    }

    public Bitmap getWallpaperScreenShot() {
        return null;
    }

    public ActivityStackEx getNewTopStack(ActivityStackEx oldStack, int otherSideModeToChange) {
        return null;
    }

    public void addOtherSnapShot(ActivityStackEx stackEx, HwActivityTaskManagerServiceEx hwAtmsEx, List<HwTaskSnapshotWrapper> list) {
    }
}
