package com.huawei.android.app;

import com.huawei.android.view.IHwMouseEventListener;

public class HwMouseEventListenerEx {
    private static final String TAG = HwMouseEventListenerEx.class.getSimpleName();
    private IHwMouseEventListener mService = new IHwMouseEventListener.Stub() {
        /* class com.huawei.android.app.HwMouseEventListenerEx.AnonymousClass1 */

        public void onReportMousePosition(float x, float y) {
            HwMouseEventListenerEx.this.onReportMousePosition(x, y);
        }
    };

    public void onReportMousePosition(float x, float y) {
    }

    public IHwMouseEventListener getInnerListener() {
        return this.mService;
    }
}
