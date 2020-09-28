package android.widget;

import android.view.View;

public class HwSpringBackHelperDummy implements HwSpringBackHelper {
    @Override // android.widget.HwSpringBackHelper
    public boolean springBack(int startPosition, int minPosition, int maxPosition) {
        return false;
    }

    @Override // android.widget.HwSpringBackHelper
    public boolean computeScrollOffset() {
        return false;
    }

    @Override // android.widget.HwSpringBackHelper
    public int getCurrentOffset() {
        return 0;
    }

    @Override // android.widget.HwSpringBackHelper
    public int getDynamicCurvedRateDelta(int viewHeight, int oldDelta, int currentPosition) {
        return oldDelta;
    }

    @Override // android.widget.HwSpringBackHelper
    public boolean isFinished() {
        return true;
    }

    @Override // android.widget.HwSpringBackHelper
    public void abortAnimation() {
    }

    @Override // android.widget.HwSpringBackHelper
    public void fling(View target, int startY, int velocityY, int minY, int maxY) {
    }

    @Override // android.widget.HwSpringBackHelper
    public void overFling(View target, int endPosition) {
    }

    @Override // android.widget.HwSpringBackHelper
    public void overFling(float velocity, int beginPosition, int endPosition) {
    }
}
