package android.hsm;

public class HwCameraPermWrapper {
    private static final String TAG = HwCameraPermWrapper.class.getSimpleName();
    private boolean isBlocked = false;

    public void confirmPermission() {
        this.isBlocked = HwSystemManager.allowOp(1024) ^ 1;
    }

    public boolean confirmPermissionWithResult() {
        this.isBlocked = HwSystemManager.allowOp(1024) ^ 1;
        return this.isBlocked;
    }

    public boolean isBlocked() {
        return this.isBlocked;
    }
}
