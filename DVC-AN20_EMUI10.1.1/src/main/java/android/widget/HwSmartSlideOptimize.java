package android.widget;

public interface HwSmartSlideOptimize {
    int adjustDuration(int i, int i2, int i3);

    int fling(int i, int i2, float f, float f2, float f3);

    double getSplineFlingDistance(int i);

    int getSplineFlingDuration(int i);

    double getUpdateDistance(long j, int i, int i2);

    float getUpdateVelocity(long j, int i, int i2);

    boolean isOptimizeEnable();
}
