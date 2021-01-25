package android.widget;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import huawei.android.widget.HwOnEditEventListener;
import huawei.android.widget.HwOnGlobalNextTabEventListener;
import huawei.android.widget.HwOnNextTabEventListener;
import huawei.android.widget.HwOnSearchEventListener;

public class HwKeyEventDetectorDummy implements HwKeyEventDetector {
    public HwKeyEventDetectorDummy(Context context) {
    }

    @Override // android.widget.HwKeyEventDetector
    public void setOnEditEventListener(HwOnEditEventListener listener) {
    }

    @Override // android.widget.HwKeyEventDetector
    public HwOnEditEventListener getOnEditEventListener() {
        return null;
    }

    @Override // android.widget.HwKeyEventDetector
    public void setOnSearchEventListener(HwOnSearchEventListener listener) {
    }

    @Override // android.widget.HwKeyEventDetector
    public HwOnSearchEventListener getOnSearchEventListener() {
        return null;
    }

    @Override // android.widget.HwKeyEventDetector
    public void setOnNextTabListener(HwOnNextTabEventListener listener) {
    }

    @Override // android.widget.HwKeyEventDetector
    public HwOnGlobalNextTabEventListener getOnGlobalNextTabListener() {
        return null;
    }

    @Override // android.widget.HwKeyEventDetector
    public void setOnGlobalNextTabListener(View view, HwOnGlobalNextTabEventListener listener) {
    }

    @Override // android.widget.HwKeyEventDetector
    public HwOnNextTabEventListener getOnNextTabListener() {
        return null;
    }

    @Override // android.widget.HwKeyEventDetector
    public void onDetachedFromWindow() {
    }

    @Override // android.widget.HwKeyEventDetector
    public boolean onKeyEvent(int keyCode, KeyEvent event) {
        return false;
    }
}
