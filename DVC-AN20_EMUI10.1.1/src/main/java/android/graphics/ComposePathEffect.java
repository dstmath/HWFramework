package android.graphics;

public class ComposePathEffect extends PathEffect {
    private static native long nativeCreate(long j, long j2);

    public ComposePathEffect(PathEffect outerpe, PathEffect innerpe) {
        this.native_instance = nativeCreate(outerpe.native_instance, innerpe.native_instance);
    }
}
