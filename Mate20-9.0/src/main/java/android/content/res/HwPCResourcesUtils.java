package android.content.res;

import android.app.ActivityThread;
import android.cover.CoverManager;
import android.util.HwPCUtils;
import android.util.SparseArray;
import android.util.TypedValue;

public class HwPCResourcesUtils implements IHwPCResourcesUtils {
    static final int COLUMN_FOUR = 3;
    static final int COLUMN_ONE = 0;
    static final int COLUMN_THREE = 2;
    static final int COLUMN_TWO = 1;
    private static HwPCResourcesUtils mInstance = null;
    private static final Object mLock = new Object();
    private AssetManager mAssetManager;
    final String[][] mOverrideNames = {new String[]{"navigation_bar_width", "dimen", CoverManager.HALL_STATE_RECEIVER_DEFINE, "hw_desktop_dock_height"}, new String[]{"navigation_bar_height", "dimen", CoverManager.HALL_STATE_RECEIVER_DEFINE, "hw_desktop_dock_height"}, new String[]{"status_bar_height", "dimen", CoverManager.HALL_STATE_RECEIVER_DEFINE, "1"}};
    private SparseArray<TypedValue> mOverrideValues = null;

    private HwPCResourcesUtils(AssetManager assetManager) {
        this.mAssetManager = assetManager;
    }

    public static HwPCResourcesUtils getDefault(AssetManager assetManager) {
        HwPCResourcesUtils hwPCResourcesUtils;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new HwPCResourcesUtils(assetManager);
            }
            hwPCResourcesUtils = mInstance;
        }
        return hwPCResourcesUtils;
    }

    private void loadHwPCOverrideValues() {
        if (this.mAssetManager != null && this.mOverrideValues == null) {
            this.mOverrideValues = new SparseArray<>();
            int size = this.mOverrideNames.length;
            for (int i = 0; i < size; i++) {
                int id = this.mAssetManager.getResourceIdentifier(this.mOverrideNames[i][0], this.mOverrideNames[i][1], this.mOverrideNames[i][2]);
                if (id != 0) {
                    TypedValue outValue = new TypedValue();
                    if (this.mAssetManager.getResourceValue(id, 0, outValue, true)) {
                        if (outValue.type == 5) {
                            try {
                                outValue.data = Integer.parseInt(this.mOverrideNames[i][3]);
                            } catch (NumberFormatException e) {
                                int overrideId = this.mAssetManager.getResourceIdentifier(this.mOverrideNames[i][3], this.mOverrideNames[i][1], "androidhwext");
                                if (overrideId != 0) {
                                    this.mAssetManager.getResourceValue(overrideId, 0, outValue, true);
                                    outValue.resourceId = id;
                                }
                            }
                        }
                        outValue.string = this.mOverrideNames[i][3];
                        this.mOverrideValues.put(id, outValue);
                    }
                }
            }
        }
    }

    public boolean getResourceValue(int resId, TypedValue outValue) {
        ActivityThread at = ActivityThread.currentActivityThread();
        if (at != null && HwPCUtils.isValidExtDisplayId(at.getDisplayId())) {
            if (this.mOverrideValues == null) {
                loadHwPCOverrideValues();
            }
            if (this.mOverrideValues == null) {
                return false;
            }
            TypedValue tv = this.mOverrideValues.get(resId);
            if (tv != null) {
                outValue.setTo(tv);
                return true;
            }
        }
        return false;
    }
}
