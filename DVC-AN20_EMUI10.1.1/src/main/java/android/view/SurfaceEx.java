package android.view;

public class SurfaceEx {
    public static void setScalingMode(Surface surface, int scalingMode) {
        if (surface != null) {
            surface.setScalingMode(scalingMode);
        }
    }
}
