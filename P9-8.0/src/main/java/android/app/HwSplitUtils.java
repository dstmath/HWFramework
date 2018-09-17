package android.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.FreezeScreenScene;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.WindowManager;
import java.math.BigDecimal;

public class HwSplitUtils {
    public static final int COLUMN_NUMBER_ONE = 1;
    public static final int COLUMN_NUMBER_TWO = 2;
    public static final String EXTRAS_HWSPLIT_SIZE = "extras_hw_split_size";
    public static final double SPLIT_LAND_DEFAULT = 5.5d;
    public static final double SPLIT_PORT_DEFAULT = 8.0d;
    private static String TAG = "HwSplitUtils";
    private static final int WIDTH_LIMIT_LAND = 592;
    private static final int WIDTH_LIMIT_PORT = 533;
    private static double mDeviceSize = 0.0d;

    public static boolean isNeedSplit(Context context) {
        if (context == null) {
            return false;
        }
        int appWidth = getAppWidth(context);
        double landSplitLimit = 5.5d;
        double portSplitLimit = 8.0d;
        Activity activity = (Activity) context;
        double[] splitSize = activity.getIntent().getDoubleArrayExtra(EXTRAS_HWSPLIT_SIZE);
        if (splitSize != null) {
            landSplitLimit = splitSize[0];
            portSplitLimit = splitSize[1];
        }
        return calculateColumnsNumber(activity, appWidth, landSplitLimit, portSplitLimit) == 2;
    }

    private static boolean isScreenPotrait(Activity a) {
        int rotation = a.getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == 0 || rotation == 2) {
            return true;
        }
        return false;
    }

    private static int calculateColumnsNumber(Activity activity, int appWidth, double landSplitLimit, double portSplitLimit) {
        double sizeInch = calculateDeviceSize(activity);
        if (isScreenPotrait(activity)) {
            if (portSplitLimit > 0.0d && ((sizeInch >= portSplitLimit || Math.abs(sizeInch - portSplitLimit) < 0.1d) && appWidth >= dip2px(activity, 533.0f))) {
                return 2;
            }
        } else if (landSplitLimit > 0.0d && ((sizeInch >= landSplitLimit || Math.abs(sizeInch - landSplitLimit) < 0.1d) && appWidth >= dip2px(activity, 592.0f))) {
            return 2;
        }
        return 1;
    }

    private static double calculateDeviceSize(Context context) {
        if (mDeviceSize > 0.0d) {
            return mDeviceSize;
        }
        IWindowManager iwm = Stub.asInterface(ServiceManager.checkService(FreezeScreenScene.WINDOW_PARAM));
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM)).getDefaultDisplay().getRealMetrics(dm);
        if (iwm != null) {
            Point point = new Point();
            try {
                iwm.getInitialDisplaySize(0, point);
                mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / dm.xdpi), 2.0d) + Math.pow((double) (((float) point.y) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
                return mDeviceSize;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while calculate device size", e);
            }
        }
        mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) + Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
        return mDeviceSize;
    }

    private static int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int getAppWidth(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return (configuration.screenWidthDp * configuration.densityDpi) / PduHeaders.PREVIOUSLY_SENT_BY;
    }

    public static boolean isJumpedActivity(String invokerName, String calleesName) {
        if (invokerName.equals("com.huawei.android.hicloud.ui.activity.NewHiSyncSettingActivity")) {
            if ("com.huawei.android.hicloud.ui.activity.HisyncGuideActivity".equals(calleesName) || "com.huawei.android.hicloud.ui.activity.MainActivity".equals(calleesName)) {
                return true;
            }
            return false;
        } else if (invokerName.equals("com.huawei.android.hicloud.ui.activity.HisyncGuideActivity")) {
            if ("com.huawei.android.hicloud.ui.activity.PhoneFinderGuideActivity".equals(calleesName)) {
                return true;
            }
            return false;
        } else if (invokerName.equals("com.huawei.android.hicloud.ui.activity.PhoneFinderGuideActivity")) {
            if ("com.huawei.android.hicloud.ui.activity.MainActivity".equals(calleesName)) {
                return true;
            }
            return false;
        } else if (invokerName.equals("com.android.settings.fingerprint.FingerprintSettingsActivity")) {
            if ("com.android.settings.ChooseLockGeneric".equals(calleesName) || "com.android.settings.ConfirmLockPassword$InternalActivity".equals(calleesName)) {
                return true;
            }
            return false;
        } else if (invokerName.equals("com.android.settings.ChooseLockGeneric") && "com.android.settings.ConfirmLockPattern$InternalActivity".equals(calleesName)) {
            return true;
        } else {
            return false;
        }
    }
}
