package com.huawei.android.app;

import android.view.DragEvent;
import com.huawei.android.view.IHwMultiDisplayDropStartListener;

public class HwMultiDisplayDropListenerEx {
    private static final String TAG = HwMultiDisplayDropListenerEx.class.getSimpleName();
    private IHwMultiDisplayDropStartListener mService = new IHwMultiDisplayDropStartListener.Stub() {
        /* class com.huawei.android.app.HwMultiDisplayDropListenerEx.AnonymousClass1 */

        public void onDropStart(DragEvent evt) {
            HwMultiDisplayDropListenerEx.this.onDropStart(evt);
        }

        public void setOriginalDropPoint(float x, float y) {
            HwMultiDisplayDropListenerEx.this.setOriginalDropPoint(x, y);
        }
    };

    public void onDropStart(DragEvent evt) {
    }

    public void setOriginalDropPoint(float x, float y) {
    }

    public IHwMultiDisplayDropStartListener getInnerListener() {
        return this.mService;
    }
}
