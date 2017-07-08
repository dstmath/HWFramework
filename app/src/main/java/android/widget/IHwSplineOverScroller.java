package android.widget;

public interface IHwSplineOverScroller {
    int adjustBallisticVelocity(int i, float f, int i2);

    double adjustDistance(double d);

    double getBallisticDistance(double d, int i, int i2, long j, long j2);

    int getBallisticDuration(int i);

    double getCubicDistance(double d, int i, int i2, long j, long j2);

    int getCubicDuration(int i);

    double getSplineFlingDistance(double d, int i, double d2, float f, float f2);

    int getSplineFlingDuration(int i, int i2, double d, float f, float f2);

    void resetLastDistanceValue(double d, double d2);

    void setStableItemHeight(int i);
}
