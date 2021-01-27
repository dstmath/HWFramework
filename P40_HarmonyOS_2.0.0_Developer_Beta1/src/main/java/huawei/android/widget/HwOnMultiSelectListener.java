package huawei.android.widget;

import android.view.MotionEvent;

public interface HwOnMultiSelectListener {
    boolean onCancel(MotionEvent motionEvent);

    boolean onSelectContinuous(boolean z, MotionEvent motionEvent);

    boolean onSelectDiscrete(MotionEvent motionEvent);
}
