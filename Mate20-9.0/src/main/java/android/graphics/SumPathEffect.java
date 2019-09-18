package android.graphics;

public class SumPathEffect extends PathEffect {
    private static native long nativeCreate(long j, long j2);

    public SumPathEffect(PathEffect first, PathEffect second) {
        this.native_instance = nativeCreate(first.native_instance, second.native_instance);
    }
}
