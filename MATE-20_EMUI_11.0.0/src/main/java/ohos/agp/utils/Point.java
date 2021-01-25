package ohos.agp.utils;

public class Point {
    public float[] position;

    public Point() {
        this.position = new float[]{0.0f, 0.0f};
    }

    public Point(float f, float f2) {
        this.position = new float[]{f, f2};
    }
}
