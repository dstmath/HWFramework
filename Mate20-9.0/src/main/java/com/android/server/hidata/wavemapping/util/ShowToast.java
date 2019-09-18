package com.android.server.hidata.wavemapping.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.server.hidata.wavemapping.cons.ContextManager;

public class ShowToast {
    private static final String TAG = ("WMapping." + ShowToast.class.getSimpleName());
    private static boolean isShow = false;
    private Context mCtx;

    public static void showToast(String info) {
        try {
            if (isShow) {
                LogUtil.i("showToast:" + info);
                Toast.makeText(ContextManager.getInstance().getContext(), info, 1).show();
            }
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "showToast,e:" + e);
        }
    }

    public static boolean isIsShow() {
        return isShow;
    }

    public static void setIsShow(boolean isShow2) {
        isShow = isShow2;
    }
}
