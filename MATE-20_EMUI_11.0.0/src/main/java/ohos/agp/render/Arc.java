package ohos.agp.render;

public class Arc {
    private float startAngle;
    private float sweepAngle;
    private boolean useCenter;

    public Arc() {
    }

    public Arc(float f, float f2, boolean z) {
        this.startAngle = f;
        this.sweepAngle = f2;
        this.useCenter = z;
    }

    public void setArc(float f, float f2, boolean z) {
        this.startAngle = f;
        this.sweepAngle = f2;
        this.useCenter = z;
    }

    public float getSweepAngle() {
        return this.sweepAngle;
    }

    public float getStartAngle() {
        return this.startAngle;
    }

    public boolean getUseCenter() {
        return this.useCenter;
    }
}
