package android.widget;

import android.view.MotionEvent;
import huawei.android.widget.HwOnChangePageListener;
import huawei.android.widget.HwOnChangeProgressListener;
import huawei.android.widget.HwOnScrollListener;

public interface HwGenericEventDetector {
    public static final int SCROLL_SENSITIVITY_FAST = 0;
    public static final int SCROLL_SENSITIVITY_NORMAL = 1;
    public static final int SCROLL_SENSITIVITY_SLOW = 2;

    HwOnChangePageListener getOnChangePageListener();

    HwOnChangeProgressListener getOnChangeProgressListener();

    HwOnScrollListener getOnScrollListener();

    float getSensitivity();

    boolean interceptGenericMotionEvent(MotionEvent motionEvent);

    boolean onGenericMotionEvent(MotionEvent motionEvent);

    void setOnChangePageListener(HwOnChangePageListener hwOnChangePageListener);

    void setOnChangeProgressListener(HwOnChangeProgressListener hwOnChangeProgressListener);

    void setOnScrollListener(HwOnScrollListener hwOnScrollListener);

    void setSensitivity(float f);

    void setSensitivityMode(int i);
}
