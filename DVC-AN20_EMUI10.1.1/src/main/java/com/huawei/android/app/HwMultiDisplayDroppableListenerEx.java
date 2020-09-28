package com.huawei.android.app;

import com.huawei.android.view.IHwMultiDisplayDroppableListener;

public class HwMultiDisplayDroppableListenerEx {
    private static final String TAG = HwMultiDisplayDroppableListenerEx.class.getSimpleName();
    private IHwMultiDisplayDroppableListener mService = new IHwMultiDisplayDroppableListener.Stub() {
        /* class com.huawei.android.app.HwMultiDisplayDroppableListenerEx.AnonymousClass1 */

        public void onDroppableResult(float x, float y, boolean result) {
            HwMultiDisplayDroppableListenerEx.this.onDroppableResult(x, y, result);
        }
    };

    public void onDroppableResult(float x, float y, boolean result) {
    }

    public IHwMultiDisplayDroppableListener getInnerListener() {
        return this.mService;
    }
}
