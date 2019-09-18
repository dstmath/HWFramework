package android.graphics;

public class PathDashPathEffect extends PathEffect {

    public enum Style {
        TRANSLATE(0),
        ROTATE(1),
        MORPH(2);
        
        int native_style;

        private Style(int value) {
            this.native_style = value;
        }
    }

    private static native long nativeCreate(long j, float f, float f2, int i);

    public PathDashPathEffect(Path shape, float advance, float phase, Style style) {
        this.native_instance = nativeCreate(shape.readOnlyNI(), advance, phase, style.native_style);
    }
}
