package android.app.hwfeature;

import android.content.Context;

public class FeaturePermissionHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "FeaturePermissionHelper";
    private static FeaturePermissionHelper mInstance = null;
    private static final Object sLock = new Object();
    private final boolean isPermission;
    private HwFeatureManager mFeatureManager;

    private FeaturePermissionHelper(Context context) {
        this.mFeatureManager = null;
        this.mFeatureManager = HwFeatureManager.getInstance();
        if (this.mFeatureManager != null) {
            this.isPermission = this.mFeatureManager.requestPermission(context);
        } else {
            this.isPermission = false;
        }
    }

    public static FeaturePermissionHelper getInstance(Context context) {
        FeaturePermissionHelper featurePermissionHelper;
        synchronized (sLock) {
            if (mInstance == null) {
                mInstance = new FeaturePermissionHelper(context);
            }
            featurePermissionHelper = mInstance;
        }
        return featurePermissionHelper;
    }

    public boolean checkPermission() {
        if (this.isPermission) {
            return true;
        }
        setInstacnece();
        return false;
    }

    private static void setInstacnece() {
        synchronized (sLock) {
            mInstance = null;
        }
    }
}
