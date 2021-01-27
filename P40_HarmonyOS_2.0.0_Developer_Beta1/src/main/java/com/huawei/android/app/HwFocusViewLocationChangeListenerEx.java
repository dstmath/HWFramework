package com.huawei.android.app;

import android.content.ComponentName;
import android.graphics.Rect;
import com.huawei.android.view.IHwFocusViewLocationChangeListener;
import java.util.List;

public class HwFocusViewLocationChangeListenerEx {
    private static final String TAG = HwFocusViewLocationChangeListenerEx.class.getSimpleName();
    private IHwFocusViewLocationChangeListener mService = new IHwFocusViewLocationChangeListener.Stub() {
        /* class com.huawei.android.app.HwFocusViewLocationChangeListenerEx.AnonymousClass1 */

        public void onLocationChange(List<Rect> focusAreas, ComponentName componentName) {
            HwFocusViewLocationChangeListenerEx.this.onLocationChange(focusAreas, componentName);
        }
    };

    public void onLocationChange(List<Rect> list, ComponentName componentName) {
    }

    public IHwFocusViewLocationChangeListener getInnerListener() {
        return this.mService;
    }
}
