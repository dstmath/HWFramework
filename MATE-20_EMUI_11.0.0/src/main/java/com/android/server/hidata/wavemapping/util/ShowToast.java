package com.android.server.hidata.wavemapping.util;

import android.util.wifi.HwHiLog;
import android.widget.Toast;
import com.android.server.hidata.wavemapping.cons.ContextManager;

public class ShowToast {
    private static final String TAG = ("WMapping." + ShowToast.class.getSimpleName());
    private static boolean isShow = false;

    private ShowToast() {
    }

    public static void showToast(String info) {
        try {
            if (isShow) {
                LogUtil.i(false, "showToast:%{public}s", info);
                Toast.makeText(ContextManager.getInstance().getContext(), info, 1).show();
            }
        } catch (Exception e) {
            HwHiLog.e(TAG, false, "showToast failed by Exception", new Object[0]);
        }
    }

    public static boolean getShow() {
        return isShow;
    }

    public static void setIsShow(boolean isShow2) {
        isShow = isShow2;
    }
}
