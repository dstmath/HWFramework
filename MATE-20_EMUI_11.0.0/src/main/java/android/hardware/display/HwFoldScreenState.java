package android.hardware.display;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.util.Flog;
import android.util.Slog;

public class HwFoldScreenState {
    public static final float ASPECT_RATIO_16_9 = 1.7777778f;
    public static final float ASPECT_RATIO_4_3 = 1.3333334f;
    public static final String DEBUG_HW_FOLD_DISP_PROP = "persist.sys.fold.disp.size";
    public static final String HW_FOLD_DISP_PROP = "ro.config.hw_fold_disp";
    public static final int REGION_TYPE_EDGE = 2;
    public static final int REGION_TYPE_MAIN = 1;
    public static final int REGION_TYPE_SUB = 4;
    public static final int REGION_TYPE_UNKNOWN = 0;
    public static int SCREEN_FOLD_EDGE_WIDTH = 0;
    public static int SCREEN_FOLD_FULL_HEIGHT = 0;
    public static int SCREEN_FOLD_FULL_WIDTH = 0;
    public static int SCREEN_FOLD_MAIN_HEIGHT = 0;
    public static int SCREEN_FOLD_MAIN_LOGICAL_HEIGHT = sOutScreenLogicalHeight;
    public static int SCREEN_FOLD_MAIN_LOGICAL_WIDTH = sOutScreenLogicalWidth;
    public static int SCREEN_FOLD_MAIN_WIDTH = 0;
    public static int SCREEN_FOLD_REAL_FULL_HEIGHT = 0;
    public static int SCREEN_FOLD_SUB_WIDTH = 0;
    public static int SCREEN_FOLD_TOP_OFFSET = 0;
    public static final String TAG = "HwFoldScreenState";
    private static int sFoldingPartScreenWidth;
    private static int sFullScreenHeight;
    private static int sFullScreenWidth = ((sLeftPartScreenWidth + sFoldingPartScreenWidth) + sRightPartScreenWidth);
    protected static boolean sInFoldScreenDevice;
    private static int sInScreenHeight;
    private static int sInScreenWidth;
    protected static boolean sIsFoldScreenDevice;
    protected static boolean sIsSimulated;
    private static int sLeftPartScreenWidth;
    protected static boolean sOutFoldScreenDevice;
    private static int sOutScreenHeight;
    private static int sOutScreenLogicalHeight;
    private static int sOutScreenLogicalWidth;
    private static int sOutScreenWidth;
    private static int sPhysicalFullScreenHeight;
    private static int sPhysicalFullScreenWidth;
    private static int sRightPartScreenWidth;
    private static float sScreenFoldFullRatio;
    private static int sTopOffset;

    static {
        sIsSimulated = false;
        sOutFoldScreenDevice = false;
        sInFoldScreenDevice = false;
        sScreenFoldFullRatio = 0.0f;
        String[] screenSizes = SystemProperties.get("ro.config.hw_fold_disp").split(SmsManager.REGEX_PREFIX_DELIMITER);
        if (screenSizes.length < 6) {
            screenSizes = SystemProperties.get(DEBUG_HW_FOLD_DISP_PROP).split(SmsManager.REGEX_PREFIX_DELIMITER);
            sIsSimulated = true;
        }
        if (screenSizes.length >= 6) {
            try {
                sIsFoldScreenDevice = true;
                sFoldingPartScreenWidth = Integer.parseInt(screenSizes[1]);
                if (sFoldingPartScreenWidth != 0) {
                    sLeftPartScreenWidth = Integer.parseInt(screenSizes[0]);
                    sRightPartScreenWidth = Integer.parseInt(screenSizes[2]);
                    sFullScreenHeight = Integer.parseInt(screenSizes[3]);
                    sPhysicalFullScreenWidth = Integer.parseInt(screenSizes[4]);
                    sPhysicalFullScreenHeight = Integer.parseInt(screenSizes[5]);
                    sTopOffset = sPhysicalFullScreenHeight - sFullScreenHeight;
                    SCREEN_FOLD_REAL_FULL_HEIGHT = sPhysicalFullScreenHeight;
                    SCREEN_FOLD_FULL_HEIGHT = sFullScreenHeight;
                    SCREEN_FOLD_FULL_WIDTH = sFullScreenWidth;
                    SCREEN_FOLD_MAIN_HEIGHT = sFullScreenHeight;
                    SCREEN_FOLD_MAIN_WIDTH = sRightPartScreenWidth;
                    SCREEN_FOLD_SUB_WIDTH = sLeftPartScreenWidth;
                    SCREEN_FOLD_EDGE_WIDTH = sFoldingPartScreenWidth;
                    SCREEN_FOLD_TOP_OFFSET = sTopOffset;
                    sOutFoldScreenDevice = true;
                    Slog.d(TAG, "parse prop ro.config.hw_fold_disp: [" + sLeftPartScreenWidth + ", " + sFoldingPartScreenWidth + ", " + sRightPartScreenWidth + ", " + sFullScreenHeight + ", " + sPhysicalFullScreenWidth + ", " + sPhysicalFullScreenHeight + "]");
                } else {
                    sOutScreenHeight = Integer.parseInt(screenSizes[0]);
                    sOutScreenWidth = Integer.parseInt(screenSizes[2]);
                    sFullScreenHeight = Integer.parseInt(screenSizes[3]);
                    sInScreenWidth = Integer.parseInt(screenSizes[4]);
                    sInScreenHeight = Integer.parseInt(screenSizes[5]);
                    if (screenSizes.length == 8) {
                        sOutScreenLogicalHeight = Integer.parseInt(screenSizes[6]);
                        sOutScreenLogicalWidth = Integer.parseInt(screenSizes[7]);
                    }
                    SCREEN_FOLD_MAIN_HEIGHT = sOutScreenHeight;
                    SCREEN_FOLD_EDGE_WIDTH = sFoldingPartScreenWidth;
                    SCREEN_FOLD_MAIN_WIDTH = sOutScreenWidth;
                    SCREEN_FOLD_FULL_WIDTH = sInScreenWidth;
                    SCREEN_FOLD_FULL_HEIGHT = sInScreenHeight;
                    SCREEN_FOLD_REAL_FULL_HEIGHT = SCREEN_FOLD_FULL_HEIGHT;
                    SCREEN_FOLD_SUB_WIDTH = 0;
                    sTopOffset = SCREEN_FOLD_REAL_FULL_HEIGHT - SCREEN_FOLD_FULL_HEIGHT;
                    SCREEN_FOLD_TOP_OFFSET = sTopOffset;
                    sInFoldScreenDevice = true;
                    Slog.d(TAG, "parse prop ro.config.hw_fold_disp: [" + sOutScreenHeight + ", " + sFoldingPartScreenWidth + ", " + sOutScreenWidth + ", " + sFullScreenHeight + ", " + sInScreenWidth + ", " + sInScreenHeight + ", " + sOutScreenLogicalHeight + ", " + sOutScreenLogicalWidth + "]");
                }
                sScreenFoldFullRatio = (((float) SCREEN_FOLD_FULL_HEIGHT) * 1.0f) / ((float) SCREEN_FOLD_FULL_WIDTH);
            } catch (NumberFormatException e) {
                sIsFoldScreenDevice = false;
                Slog.e(TAG, "parse screenSizes config error");
            }
        } else {
            sIsFoldScreenDevice = false;
            Slog.w(TAG, "screenSizes config length error");
        }
    }

    public static boolean isFoldScreenDevice() {
        return sIsFoldScreenDevice;
    }

    public static boolean isInwardFoldDevice() {
        return sInFoldScreenDevice;
    }

    public static boolean isOutFoldDevice() {
        return sOutFoldScreenDevice;
    }

    public static Rect getScreenPhysicalRect(int displayMode) {
        if (!sIsFoldScreenDevice) {
            return null;
        }
        if (sOutFoldScreenDevice) {
            return getOutFoldScreenPhysicalRect(displayMode);
        }
        return getInFoldScreenPhysicalRect(displayMode);
    }

    public static void getAppFolderDisplayRect(int displayMode, float appMaxRatio, Rect outBounds, Rect appBounds, int rotation) {
        if (sIsFoldScreenDevice && appMaxRatio != 0.0f && displayMode == 1) {
            int containingAppWidth = appBounds.width();
            int containingAppHeight = appBounds.height();
            int maxActivityWidth = containingAppWidth;
            int maxActivityHeight = containingAppHeight;
            int xOffSet = appBounds.left;
            int yOffSet = appBounds.top;
            if (rotation == 0 || rotation == 2) {
                maxActivityWidth = (int) ((((float) maxActivityHeight) / appMaxRatio) + 0.5f);
                xOffSet = (containingAppWidth - maxActivityWidth) / 2;
            } else {
                maxActivityHeight = (int) ((((float) maxActivityWidth) / appMaxRatio) + 0.5f);
                yOffSet = (containingAppHeight - maxActivityHeight) / 2;
            }
            if (containingAppWidth > maxActivityWidth || containingAppHeight > maxActivityHeight) {
                Flog.i(101, "getAppFolderDisplayRect appMaxRatio " + appMaxRatio + ", containingAppBounds " + appBounds.toString() + ", maxActivityWidth " + maxActivityWidth + ", maxActivityHeight " + maxActivityHeight + ", xOffSet " + xOffSet + ", yOffSet " + yOffSet);
                outBounds.set(xOffSet, yOffSet, maxActivityWidth + xOffSet, maxActivityHeight + yOffSet);
                return;
            }
            outBounds.set(appBounds);
        }
    }

    public static float getScreenFoldFullRatio() {
        return sScreenFoldFullRatio;
    }

    public static int getClickRegion(Point point) {
        if (isInwardFoldDevice()) {
            return 1;
        }
        if (point == null) {
            return 0;
        }
        int x = SCREEN_FOLD_FULL_WIDTH - point.y;
        int y = point.x;
        Slog.i(TAG, "getDisplayRect x:" + x + ", y:" + y);
        if (y > SCREEN_FOLD_FULL_HEIGHT) {
            return 0;
        }
        int i = SCREEN_FOLD_SUB_WIDTH;
        if (x <= i) {
            return 4;
        }
        if (x < i + SCREEN_FOLD_EDGE_WIDTH) {
            return 2;
        }
        if (x <= SCREEN_FOLD_FULL_WIDTH) {
            return 1;
        }
        return 0;
    }

    private static Rect getOutFoldScreenPhysicalRect(int displayMode) {
        if (2 == displayMode) {
            int i = SCREEN_FOLD_FULL_WIDTH;
            return new Rect(i - SCREEN_FOLD_MAIN_WIDTH, 0, i, SCREEN_FOLD_FULL_HEIGHT);
        } else if (3 == displayMode) {
            return new Rect(0, 0, SCREEN_FOLD_SUB_WIDTH, SCREEN_FOLD_FULL_HEIGHT);
        } else {
            return new Rect(0, 0, SCREEN_FOLD_FULL_WIDTH, SCREEN_FOLD_FULL_HEIGHT);
        }
    }

    private static Rect getInFoldScreenPhysicalRect(int displayMode) {
        if (displayMode == 1) {
            return new Rect(0, 0, SCREEN_FOLD_FULL_WIDTH, SCREEN_FOLD_FULL_HEIGHT);
        }
        if (displayMode == 2) {
            return new Rect(0, 0, SCREEN_FOLD_MAIN_WIDTH, SCREEN_FOLD_MAIN_HEIGHT);
        }
        return new Rect(0, 0, 0, 0);
    }
}
