package ohos.multimodalinput.event;

public class MmiPoint {
    private float px;
    private float py;
    private float pz;

    public MmiPoint(float f, float f2) {
        this(f, f2, 0.0f);
    }

    public MmiPoint(float f, float f2, float f3) {
        this.px = f;
        this.py = f2;
        this.pz = f3;
    }

    public float getX() {
        return this.px;
    }

    public float getY() {
        return this.py;
    }

    public float getZ() {
        return this.pz;
    }

    public String toString() {
        return "MmiPoint{px=" + this.px + ", py=" + this.py + ", pz=" + this.pz + '}';
    }
}
