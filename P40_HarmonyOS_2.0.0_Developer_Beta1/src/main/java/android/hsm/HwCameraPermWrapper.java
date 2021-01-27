package android.hsm;

public class HwCameraPermWrapper {
    private static final String TAG = HwCameraPermWrapper.class.getSimpleName();
    private boolean mIsBlocked = false;

    public void confirmPermission() {
        this.mIsBlocked = !HwSystemManager.allowOp(1024);
    }

    public boolean confirmPermissionWithResult() {
        this.mIsBlocked = !HwSystemManager.allowOp(1024);
        return this.mIsBlocked;
    }

    public boolean isBlocked() {
        return this.mIsBlocked;
    }
}
