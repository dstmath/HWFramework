package com.huawei.iimagekit.common.agp.graphics.engine;

import android.content.Context;
import com.huawei.iimagekit.blur.util.DebugUtil;
import com.huawei.iimagekit.common.agp.graphics.GFX2D;

public class GraphicsContext {
    private static Context mContext;

    static {
        DebugUtil.log("System.loadLibrary iimagekit_jni");
        System.loadLibrary("iimagekit_jni");
    }

    public static void init(Context appContext) {
        setAppContext(appContext);
        init();
    }

    public static void init() {
        if (mContext == null) {
            DebugUtil.log("ERROR: call setAppContext before calling init");
        } else {
            GFX2D.init();
        }
    }

    public static void setAppContext(Context context) {
        mContext = context;
    }

    public static void destroy() {
        GFX2D.destroy();
    }

    public static Context getAppContext() {
        return mContext;
    }
}
