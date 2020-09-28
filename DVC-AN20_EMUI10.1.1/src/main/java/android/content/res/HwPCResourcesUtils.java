package android.content.res;

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
    final String[][] mOverrideNames = {new String[]{"navigation_bar_width", "dimen", "android", "hw_desktop_dock_height"}, new String[]{"navigation_bar_height", "dimen", "android", "hw_desktop_dock_height"}, new String[]{"status_bar_height", "dimen", "android", "1"}};
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
                AssetManager assetManager = this.mAssetManager;
                String[][] strArr = this.mOverrideNames;
                int id = assetManager.getResourceIdentifier(strArr[i][0], strArr[i][1], strArr[i][2]);
                if (id != 0) {
                    TypedValue outValue = new TypedValue();
                    if (this.mAssetManager.getResourceValue(id, 0, outValue, true)) {
                        if (outValue.type == 5) {
                            try {
                                outValue.data = Integer.parseInt(this.mOverrideNames[i][3]);
                            } catch (NumberFormatException e) {
                                AssetManager assetManager2 = this.mAssetManager;
                                String[][] strArr2 = this.mOverrideNames;
                                int overrideId = assetManager2.getResourceIdentifier(strArr2[i][3], strArr2[i][1], "androidhwext");
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
        return false;
    }
}
