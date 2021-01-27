package com.huawei.android.view;

import android.graphics.Region;
import android.view.ISystemGestureExclusionListener;

public class ISystemGestureExclusionListenerEx {
    private ISystemGestureExclusionListener mListener = new ISystemGestureExclusionListener.Stub() {
        /* class com.huawei.android.view.ISystemGestureExclusionListenerEx.AnonymousClass1 */

        public void onSystemGestureExclusionChanged(int displayId, Region systemGestureExclusion) {
            ISystemGestureExclusionListenerEx.this.onSystemGestureExclusionChanged(displayId, systemGestureExclusion);
        }
    };

    public ISystemGestureExclusionListener getISystemGestureExclusionListener() {
        return this.mListener;
    }

    public void onSystemGestureExclusionChanged(int displayId, Region systemGestureExclusion) {
    }
}
