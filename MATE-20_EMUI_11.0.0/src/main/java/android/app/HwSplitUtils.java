package android.app;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.display.HwFoldScreenState;
import android.os.FreezeScreenScene;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.huawei.android.fsm.HwFoldScreenManager;
import java.math.BigDecimal;

public class HwSplitUtils {
    public static final int COLUMN_NUMBER_ONE = 1;
    public static final int COLUMN_NUMBER_TWO = 2;
    public static final String EXTRAS_HWSPLIT_SIZE = "extras_hw_split_size";
    public static final boolean IS_FOLDABLE_PHONE = HwFoldScreenManager.isFoldable();
    public static final double SPLIT_LAND_DEFAULT = 5.5d;
    public static final double SPLIT_PORT_DEFAULT = 8.0d;
    private static String TAG = "HwSplitUtils";
    private static final int WIDTH_LIMIT_LAND = 592;
    private static final int WIDTH_LIMIT_PORT = 533;
    private static double mDeviceSize = 0.0d;
    private static int sOldDisplayMode = 0;

    public static boolean isNeedSplit(Context context) {
        double portSplitLimit;
        double landSplitLimit;
        if (context == null) {
            return false;
        }
        if (IS_FOLDABLE_PHONE && !isFullState()) {
            return false;
        }
        int appWidth = getAppWidth(context);
        Activity activity = (Activity) context;
        double[] splitSize = activity.getIntent().getDoubleArrayExtra(EXTRAS_HWSPLIT_SIZE);
        if (splitSize == null || splitSize.length < 2) {
            landSplitLimit = 5.5d;
            portSplitLimit = 8.0d;
        } else {
            landSplitLimit = splitSize[0];
            portSplitLimit = splitSize[1];
        }
        if (calculateColumnsNumber(activity, appWidth, landSplitLimit, portSplitLimit) == 2) {
            return true;
        }
        return false;
    }

    private static boolean isFullState() {
        int displayMode = 0;
        try {
            displayMode = HwFoldScreenManager.getDisplayMode();
            String str = TAG;
            Log.i(str, "getDisplayMode : " + displayMode);
        } catch (RuntimeException e) {
            String str2 = TAG;
            Log.e(str2, "fail to getDisplayMode : " + e.getMessage());
        }
        if (displayMode != 0) {
            sOldDisplayMode = displayMode;
        }
        return sOldDisplayMode == 1;
    }

    private static boolean isScreenPotrait(Activity a) {
        int rotation = a.getWindowManager().getDefaultDisplay().getRotation();
        return rotation == 0 || rotation == 2;
    }

    private static int calculateColumnsNumber(Activity activity, int appWidth, double landSplitLimit, double portSplitLimit) {
        double sizeInch = calculateDeviceSize(activity);
        if (isScreenPotrait(activity)) {
            if (portSplitLimit <= 0.0d) {
                return 1;
            }
            if ((sizeInch >= portSplitLimit || Math.abs(sizeInch - portSplitLimit) < 0.1d) && appWidth >= dip2px(activity, 533.0f)) {
                return 2;
            }
            return 1;
        } else if (landSplitLimit <= 0.0d) {
            return 1;
        } else {
            if ((sizeInch >= landSplitLimit || Math.abs(sizeInch - landSplitLimit) < 0.1d) && appWidth >= dip2px(activity, 592.0f)) {
                return 2;
            }
            return 1;
        }
    }

    private static double calculateDeviceSize(Context context) {
        if (mDeviceSize > 0.0d && !HwFoldScreenState.isFoldScreenDevice()) {
            return mDeviceSize;
        }
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM)).getDefaultDisplay().getRealMetrics(dm);
        mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) + Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
        return mDeviceSize;
    }

    private static int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int getAppWidth(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return (int) (((((float) configuration.screenWidthDp) * ((float) configuration.densityDpi)) / 160.0f) + 0.5f);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static boolean isJumpedActivity(String invokerName, String calleesName) {
        char c;
        switch (invokerName.hashCode()) {
            case -1996220965:
                if (invokerName.equals("com.huawei.android.hicloud.ui.activity.NewHiSyncSettingActivity")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1017449393:
                if (invokerName.equals("com.huawei.android.hicloud.ui.activity.PhoneFinderGuideActivity")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -675936042:
                if (invokerName.equals("com.android.settings.ChooseLockGeneric")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1179242957:
                if (invokerName.equals("com.android.settings.fingerprint.FingerprintSettingsActivity")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1764624503:
                if (invokerName.equals("com.huawei.android.hicloud.ui.activity.HisyncGuideActivity")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1889347233:
                if (invokerName.equals("com.android.settings.password.ChooseLockGeneric")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c != 0) {
            if (c != 1) {
                if (c != 2) {
                    if (c != 3) {
                        if (c != 4) {
                            if (c != 5) {
                                return false;
                            }
                            if ("com.android.settings.password.ConfirmLockPattern$InternalActivity".equals(calleesName) || "com.android.settings.ConfirmLockPattern$InternalActivity".equals(calleesName)) {
                                return true;
                            }
                            return false;
                        } else if ("com.android.settings.ConfirmLockPattern$InternalActivity".equals(calleesName) || "com.android.settings.password.ConfirmLockPattern$InternalActivity".equals(calleesName)) {
                            return true;
                        } else {
                            return false;
                        }
                    } else if ("com.android.settings.ChooseLockGeneric".equals(calleesName) || "com.android.settings.password.ChooseLockGeneric".equals(calleesName) || "com.android.settings.password.ConfirmLockPassword$InternalActivity".equals(calleesName) || "com.android.settings.ConfirmLockPassword$InternalActivity".equals(calleesName)) {
                        return true;
                    } else {
                        return false;
                    }
                } else if ("com.huawei.android.hicloud.ui.activity.MainActivity".equals(calleesName)) {
                    return true;
                } else {
                    return false;
                }
            } else if ("com.huawei.android.hicloud.ui.activity.PhoneFinderGuideActivity".equals(calleesName)) {
                return true;
            } else {
                return false;
            }
        } else if ("com.huawei.android.hicloud.ui.activity.HisyncGuideActivity".equals(calleesName) || "com.huawei.android.hicloud.ui.activity.MainActivity".equals(calleesName)) {
            return true;
        } else {
            return false;
        }
    }
}
