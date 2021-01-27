package android.app.hwfeature;

import android.content.Context;

public class FeaturePermissionHelper {
    private static final boolean IS_DEBUG = false;
    private static final String TAG = "FeaturePermissionHelper";
    private static FeaturePermissionHelper mInstance = null;
    private static final Object sLock = new Object();
    private HwFeatureManager mFeatureManager;
    private final boolean mIsPermission;

    private FeaturePermissionHelper(Context context) {
        this.mFeatureManager = null;
        this.mFeatureManager = HwFeatureManager.getInstance();
        HwFeatureManager hwFeatureManager = this.mFeatureManager;
        if (hwFeatureManager != null) {
            this.mIsPermission = hwFeatureManager.requestPermission(context);
        } else {
            this.mIsPermission = false;
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
        if (this.mIsPermission) {
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
