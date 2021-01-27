package android.widget;

import android.view.View;

public interface HwSpringBackHelper {
    void abortAnimation();

    boolean computeScrollOffset();

    void fling(View view, int i, int i2, int i3, int i4);

    int getCurrentOffset();

    int getDynamicCurvedRateDelta(int i, int i2, int i3);

    boolean isFinished();

    void overFling(float f, int i, int i2);

    void overFling(View view, int i);

    boolean springBack(int i, int i2, int i3);
}
