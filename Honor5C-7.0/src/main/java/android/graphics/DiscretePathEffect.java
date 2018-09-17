package android.graphics;

public class DiscretePathEffect extends PathEffect {
    private static native long nativeCreate(float f, float f2);

    public DiscretePathEffect(float segmentLength, float deviation) {
        this.native_instance = nativeCreate(segmentLength, deviation);
    }
}
