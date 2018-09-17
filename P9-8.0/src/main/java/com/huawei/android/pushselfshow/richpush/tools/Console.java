package com.huawei.android.pushselfshow.richpush.tools;

import android.webkit.JavascriptInterface;
import com.huawei.android.pushagent.a.a.c;

public class Console {
    private static final String TAG = "[WebView]";

    @JavascriptInterface
    public void log(String str) {
        c.b(TAG, str);
    }

    @JavascriptInterface
    public void logV(String str) {
        c.e(TAG, str);
    }
}
