package android.widget;

import android.content.Context;
import android.view.MotionEvent;
import huawei.android.widget.HwOnChangePageListener;
import huawei.android.widget.HwOnChangeProgressListener;
import huawei.android.widget.HwOnScrollListener;

public class HwGenericEventDetectorDummy implements HwGenericEventDetector {
    public HwGenericEventDetectorDummy(Context context) {
    }

    @Override // android.widget.HwGenericEventDetector
    public void setOnChangePageListener(HwOnChangePageListener listener) {
    }

    @Override // android.widget.HwGenericEventDetector
    public HwOnChangePageListener getOnChangePageListener() {
        return null;
    }

    @Override // android.widget.HwGenericEventDetector
    public void setOnChangeProgressListener(HwOnChangeProgressListener listener) {
    }

    @Override // android.widget.HwGenericEventDetector
    public HwOnChangeProgressListener getOnChangeProgressListener() {
        return null;
    }

    @Override // android.widget.HwGenericEventDetector
    public void setOnScrollListener(HwOnScrollListener listener) {
    }

    @Override // android.widget.HwGenericEventDetector
    public HwOnScrollListener getOnScrollListener() {
        return null;
    }

    @Override // android.widget.HwGenericEventDetector
    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    @Override // android.widget.HwGenericEventDetector
    public boolean interceptGenericMotionEvent(MotionEvent event) {
        return false;
    }

    @Override // android.widget.HwGenericEventDetector
    public void setSensitivityMode(int mode) {
    }

    @Override // android.widget.HwGenericEventDetector
    public void setSensitivity(float sensitivity) {
    }

    @Override // android.widget.HwGenericEventDetector
    public float getSensitivity() {
        return 1.0f;
    }
}
