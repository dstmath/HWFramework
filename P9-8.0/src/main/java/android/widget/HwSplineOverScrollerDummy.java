package android.widget;

public class HwSplineOverScrollerDummy implements IHwSplineOverScroller {
    public void resetLastDistanceValue(double lastDistance, double lastDistanceActual) {
    }

    public void setStableItemHeight(int h) {
    }

    public double adjustDistance(double oirginalDistance) {
        return oirginalDistance;
    }

    public double getBallisticDistance(double originalDistance, int start, int end, long duration, long currentTime) {
        return originalDistance;
    }

    public double getCubicDistance(double originalDistance, int start, int end, long duration, long currentTime) {
        return originalDistance;
    }

    public int getBallisticDuration(int originalDuration) {
        return originalDuration;
    }

    public int getCubicDuration(int originalDuration) {
        return originalDuration;
    }

    public int adjustBallisticVelocity(int originalVelocity, float acceleration, int maxOver) {
        return originalVelocity;
    }

    public double getSplineFlingDistance(double orignDistance, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        return orignDistance;
    }

    public int getSplineFlingDuration(int orignDurtion, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
        return orignDurtion;
    }
}
