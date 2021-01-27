package com.android.server.wm;

import android.graphics.Rect;
import android.util.ArrayMap;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.commgmt.zrhung.DefaultHwDisplayContentEx;
import com.huawei.android.util.SlogEx;
import com.huawei.dfr.HwDFRFrameworkFactory;

public final class HwDisplayContentEx extends DefaultHwDisplayContentEx {
    private static final int DEFALUT_CAPACITY = 0;
    private static final String TAG = "HwDisplayContentEx";
    private static IZrHung focusWindowZrHung = HwDFRFrameworkFactory.getZrHungFrameworkFactory().getIZrHung("appeye_nofocuswindow");
    private static IZrHung transWindowZrHung = HwDFRFrameworkFactory.getZrHungFrameworkFactory().getIZrHung("appeye_transparentwindow");

    public void focusWinZrHung(WindowStateCommonEx currentFocus, AppWindowTokenEx focusedApp, int displayId) {
        ArrayMap<String, Object> params = new ArrayMap<>((int) DEFALUT_CAPACITY);
        if (!currentFocus.isWindowStateNull()) {
            params.put("focusedWindowName", currentFocus.toString());
            params.put("layoutParams", currentFocus.getAttrs());
            if (!currentFocus.isSessionNull()) {
                params.put("pid", Integer.valueOf(currentFocus.getPid()));
            }
            params.put("focusedWinPackageName", currentFocus.getOwningPackageName());
        } else {
            params.put("focusedWindowName", "null");
            params.put("layoutParams", "null");
            params.put("pid", Integer.valueOf((int) DEFALUT_CAPACITY));
            params.put("focusedWinPackageName", "null");
        }
        if (!focusedApp.isAppWindowNull()) {
            params.put("focusedAppPackageName", focusedApp.getAppPackageName());
            params.put("focusedActivityName", focusedApp.getAppComponentName());
        } else {
            params.put("focusedAppPackageName", "null");
            params.put("focusedActivityName", "null");
        }
        params.put("displayId", Integer.valueOf(displayId));
        ZrHungData zrhungData = new ZrHungData();
        zrhungData.putAll(params);
        if (focusWindowZrHung != null) {
            if (currentFocus.isWindowStateNull()) {
                SlogEx.i(TAG, "currentFocus windowstate null");
                focusWindowZrHung.check(zrhungData);
            } else {
                SlogEx.i(TAG, "currentFocus windowstate not null");
                focusWindowZrHung.cancelCheck(zrhungData);
            }
        }
        IZrHung iZrHung = transWindowZrHung;
        if (iZrHung != null) {
            iZrHung.cancelCheck(zrhungData);
            if (!currentFocus.isWindowStateNull()) {
                SlogEx.i(TAG, "trans currentFocus windowstate not null");
                transWindowZrHung.check(zrhungData);
            }
        }
    }

    public boolean isPointOutsideMagicWindow(WindowStateCommonEx win, int x, int y) {
        if (win == null || !win.inHwMagicWindowingMode() || Math.abs(1.0f - win.getMmUsedScaleFactor()) < 1.0E-6f) {
            return false;
        }
        Rect tmpBound = new Rect();
        tmpBound.set(win.getVisibleFrameLw());
        if (tmpBound.isEmpty()) {
            tmpBound.set(win.getFrameLw());
        }
        tmpBound.right = (int) (((float) tmpBound.left) + (((float) tmpBound.width()) * win.getMmUsedScaleFactor()) + 0.5f);
        tmpBound.bottom = (int) (((float) tmpBound.top) + (((float) tmpBound.height()) * win.getMmUsedScaleFactor()) + 0.5f);
        return !tmpBound.contains(x, y);
    }
}
