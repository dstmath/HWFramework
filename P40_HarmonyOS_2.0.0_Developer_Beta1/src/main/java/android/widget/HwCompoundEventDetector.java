package android.widget;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import huawei.android.widget.HwOnMultiSelectListener;
import huawei.android.widget.HwOnZoomEventListener;

public interface HwCompoundEventDetector {
    HwOnMultiSelectListener getOnMultiSelectEventListener();

    HwOnZoomEventListener getOnZoomEventListener();

    void onDetachedFromWindow();

    boolean onGenericMotionEvent(MotionEvent motionEvent);

    boolean onKeyEvent(int i, KeyEvent keyEvent);

    boolean onTouchEvent(MotionEvent motionEvent);

    void setOnMultiSelectEventListener(View view, HwOnMultiSelectListener hwOnMultiSelectListener);

    void setOnZoomEventListener(View view, HwOnZoomEventListener hwOnZoomEventListener);
}
