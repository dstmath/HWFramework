package android.view;

public class SurfaceViewEx {
    public static boolean isSurfaceCreated(SurfaceView surfaceView) {
        if (surfaceView == null) {
            return false;
        }
        return surfaceView.mSurfaceCreated;
    }

    public static void setCornerRadiusEx(SurfaceView surfaceView, float cornerRadius) {
        if (surfaceView != null) {
            surfaceView.setCornerRadiusEx(cornerRadius);
        }
    }
}
