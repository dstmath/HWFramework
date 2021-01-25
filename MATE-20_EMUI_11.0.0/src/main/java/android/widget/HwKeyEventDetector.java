package android.widget;

import android.view.KeyEvent;
import android.view.View;
import huawei.android.widget.HwOnEditEventListener;
import huawei.android.widget.HwOnGlobalNextTabEventListener;
import huawei.android.widget.HwOnNextTabEventListener;
import huawei.android.widget.HwOnSearchEventListener;

public interface HwKeyEventDetector {
    HwOnEditEventListener getOnEditEventListener();

    HwOnGlobalNextTabEventListener getOnGlobalNextTabListener();

    HwOnNextTabEventListener getOnNextTabListener();

    HwOnSearchEventListener getOnSearchEventListener();

    void onDetachedFromWindow();

    boolean onKeyEvent(int i, KeyEvent keyEvent);

    void setOnEditEventListener(HwOnEditEventListener hwOnEditEventListener);

    void setOnGlobalNextTabListener(View view, HwOnGlobalNextTabEventListener hwOnGlobalNextTabEventListener);

    void setOnNextTabListener(HwOnNextTabEventListener hwOnNextTabEventListener);

    void setOnSearchEventListener(HwOnSearchEventListener hwOnSearchEventListener);
}
