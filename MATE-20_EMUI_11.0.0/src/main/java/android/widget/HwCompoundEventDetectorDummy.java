package android.widget;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import huawei.android.widget.HwOnMultiSelectListener;
import huawei.android.widget.HwOnZoomEventListener;

public class HwCompoundEventDetectorDummy implements HwCompoundEventDetector {
    public HwCompoundEventDetectorDummy(Context context) {
    }

    @Override // android.widget.HwCompoundEventDetector
    public void setOnMultiSelectEventListener(View view, HwOnMultiSelectListener listener) {
    }

    @Override // android.widget.HwCompoundEventDetector
    public HwOnMultiSelectListener getOnMultiSelectEventListener() {
        return null;
    }

    @Override // android.widget.HwCompoundEventDetector
    public void setOnZoomEventListener(View view, HwOnZoomEventListener listener) {
    }

    @Override // android.widget.HwCompoundEventDetector
    public HwOnZoomEventListener getOnZoomEventListener() {
        return null;
    }

    @Override // android.widget.HwCompoundEventDetector
    public boolean onKeyEvent(int keyCode, KeyEvent event) {
        return false;
    }

    @Override // android.widget.HwCompoundEventDetector
    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    @Override // android.widget.HwCompoundEventDetector
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override // android.widget.HwCompoundEventDetector
    public void onDetachedFromWindow() {
    }
}
