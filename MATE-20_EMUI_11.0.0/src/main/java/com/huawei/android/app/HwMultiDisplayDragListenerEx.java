package com.huawei.android.app;

import android.content.ClipData;
import com.huawei.android.view.IHwMultiDisplayDragStartListener;

public class HwMultiDisplayDragListenerEx {
    private static final String TAG = HwMultiDisplayDragListenerEx.class.getSimpleName();
    private IHwMultiDisplayDragStartListener mService = new IHwMultiDisplayDragStartListener.Stub() {
        /* class com.huawei.android.app.HwMultiDisplayDragListenerEx.AnonymousClass1 */

        public void onDragStart(ClipData data) {
            HwMultiDisplayDragListenerEx.this.onDragStart(data);
        }
    };

    public void onDragStart(ClipData data) {
    }

    public IHwMultiDisplayDragStartListener getInnerListener() {
        return this.mService;
    }
}
