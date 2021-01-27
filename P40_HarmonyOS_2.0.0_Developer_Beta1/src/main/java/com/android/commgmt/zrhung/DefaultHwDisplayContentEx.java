package com.android.commgmt.zrhung;

import android.util.Log;
import com.android.server.wm.AppWindowTokenEx;
import com.android.server.wm.IHwDisplayContentEx;
import com.android.server.wm.WindowStateCommonEx;

public class DefaultHwDisplayContentEx implements IHwDisplayContentEx {
    private static final String TAG = "DefaultHwDisplayContentEx";

    public static IHwDisplayContentEx getDefaultIHwDisplayContent() {
        Log.i(TAG, "getDefaultIHwDisplayContent");
        return new DefaultHwDisplayContentEx();
    }

    public void focusWinZrHung(WindowStateCommonEx windowStateEx, AppWindowTokenEx appWindowTokenEx, int i) {
        Log.i(TAG, "DefaultIHwDisplayContent focuswin");
    }

    public boolean isPointOutsideMagicWindow(WindowStateCommonEx win, int x, int y) {
        Log.i(TAG, "isPointOutsideMagicWindow default");
        return false;
    }
}
