package android.widget;

public class HwSplineOverScrollerDummy implements IHwSplineOverScroller {
    @Override // android.widget.IHwSplineOverScroller
    public void resetLastDistanceValue(double lastDistance, double lastDistanceActual) {
    }

    @Override // android.widget.IHwSplineOverScroller
    public void setStableItemHeight(int height) {
    }

    @Override // android.widget.IHwSplineOverScroller
    public double adjustDistance(double oirginalDistance) {
        return oirginalDistance;
    }

    @Override // android.widget.IHwSplineOverScroller
    public double getBallisticDistance(double originalDistance, int start, int end, long duration, long currentTime) {
        return originalDistance;
    }

    @Override // android.widget.IHwSplineOverScroller
    public double getCubicDistance(double originalDistance, int start, int end, long duration, long currentTime) {
        return originalDistance;
    }

    @Override // android.widget.IHwSplineOverScroller
    public int getBallisticDuration(int originalDuration) {
        return originalDuration;
    }

    @Override // android.widget.IHwSplineOverScroller
    public int getCubicDuration(int originalDuration) {
        return originalDuration;
    }

    @Override // android.widget.IHwSplineOverScroller
    public int adjustBallisticVelocity(int originalVelocity, float acceleration, int maxOver) {
        return originalVelocity;
    }

    @Override // android.widget.IHwSplineOverScroller
    public double getSplineFlingDistance(double orignDistance, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        return orignDistance;
    }

    @Override // android.widget.IHwSplineOverScroller
    public int getSplineFlingDuration(int orignDurtion, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        return orignDurtion;
    }
}
