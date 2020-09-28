package android.view;

public class SurfaceViewEx {
    public static boolean isSurfaceCreated(SurfaceView surfaceView) {
        if (surfaceView == null) {
            return false;
        }
        return surfaceView.mSurfaceCreated;
    }
}
