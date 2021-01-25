package com.huawei.android.app;

import android.content.ClipData;
import android.graphics.Bitmap;
import com.huawei.android.view.IHwMultiDisplayBasicModeDragStartListener;

public class HwMultiDisplayBasicModeDragListenerEx {
    private static final String TAG = HwMultiDisplayBasicModeDragListenerEx.class.getSimpleName();
    private IHwMultiDisplayBasicModeDragStartListener mService = new IHwMultiDisplayBasicModeDragStartListener.Stub() {
        /* class com.huawei.android.app.HwMultiDisplayBasicModeDragListenerEx.AnonymousClass1 */

        public void onDragStart(ClipData data, Bitmap bitmap) {
            HwMultiDisplayBasicModeDragListenerEx.this.onDragStart(data, bitmap);
        }
    };

    public void onDragStart(ClipData data, Bitmap bitamp) {
    }

    public IHwMultiDisplayBasicModeDragStartListener getInnerListener() {
        return this.mService;
    }
}
