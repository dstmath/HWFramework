package android.widget;

public class HwSmartSlideOptimizeImplDummy implements HwSmartSlideOptimize {
    @Override // android.widget.HwSmartSlideOptimize
    public boolean isOptimizeEnable() {
        return false;
    }

    @Override // android.widget.HwSmartSlideOptimize
    public int fling(int velocityX, int velocityY, float oldVelocityX, float oldVelocityY, float distance) {
        return 0;
    }

    @Override // android.widget.HwSmartSlideOptimize
    public double getSplineFlingDistance(int velocity) {
        return 0.0d;
    }

    @Override // android.widget.HwSmartSlideOptimize
    public int getSplineFlingDuration(int velocity) {
        return 0;
    }

    @Override // android.widget.HwSmartSlideOptimize
    public double getUpdateDistance(long currentTime, int splineDuration, int splineDistance) {
        return 0.0d;
    }

    @Override // android.widget.HwSmartSlideOptimize
    public float getUpdateVelocity(long currentTime, int splineDuration, int velocity) {
        return 0.0f;
    }

    @Override // android.widget.HwSmartSlideOptimize
    public int adjustDuration(int adjustDistance, int splineDuration, int splineDistance) {
        return 0;
    }
}
