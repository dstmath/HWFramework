package android.hardware.display;

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
    protected static int SCREEN_FOLD_EDGE_WIDTH = 0;
    protected static int SCREEN_FOLD_FULL_HEIGHT = 0;
    protected static int SCREEN_FOLD_FULL_WIDTH = 0;
    protected static int SCREEN_FOLD_MAIN_WIDTH = 0;
    protected static int SCREEN_FOLD_REAL_FULL_HEIGHT = 0;
    protected static int SCREEN_FOLD_SUB_WIDTH = 0;
    protected static int SCREEN_FOLD_TOP_OFFSET = 0;
    public static final String TAG = "HwFoldScreenState";
    private static int mFoldingPartScreenWidth;
    private static int mFullScreenHeight;
    private static int mFullScreenWidth;
    protected static boolean mIsFoldScreenDevice;
    protected static boolean mIsSimulated;
    private static int mLeftPartScreenWidth;
    private static int mPhysicalFullScreenHeight;
    private static int mPhysicalFullScreenWidth;
    private static int mRightPartScreenWidth;
    private static float mScreenFoldFullRatio;
    private static int mTopOffset;

    static {
        mScreenFoldFullRatio = 0.0f;
        mIsFoldScreenDevice = false;
        mIsSimulated = false;
        String[] screenSizes = SystemProperties.get("ro.config.hw_fold_disp").split(SmsManager.REGEX_PREFIX_DELIMITER);
        if (screenSizes.length != 6) {
            screenSizes = SystemProperties.get(DEBUG_HW_FOLD_DISP_PROP).split(SmsManager.REGEX_PREFIX_DELIMITER);
            mIsSimulated = true;
        }
        if (screenSizes.length == 6) {
            try {
                mLeftPartScreenWidth = Integer.parseInt(screenSizes[0]);
                mFoldingPartScreenWidth = Integer.parseInt(screenSizes[1]);
                mRightPartScreenWidth = Integer.parseInt(screenSizes[2]);
                mFullScreenWidth = mLeftPartScreenWidth + mFoldingPartScreenWidth + mRightPartScreenWidth;
                mFullScreenHeight = Integer.parseInt(screenSizes[3]);
                mPhysicalFullScreenWidth = Integer.parseInt(screenSizes[4]);
                mPhysicalFullScreenHeight = Integer.parseInt(screenSizes[5]);
                mTopOffset = mPhysicalFullScreenHeight - mFullScreenHeight;
                SCREEN_FOLD_REAL_FULL_HEIGHT = mPhysicalFullScreenHeight;
                SCREEN_FOLD_FULL_HEIGHT = mFullScreenHeight;
                SCREEN_FOLD_FULL_WIDTH = mFullScreenWidth;
                SCREEN_FOLD_MAIN_WIDTH = mRightPartScreenWidth;
                SCREEN_FOLD_SUB_WIDTH = mLeftPartScreenWidth;
                SCREEN_FOLD_EDGE_WIDTH = mFoldingPartScreenWidth;
                SCREEN_FOLD_TOP_OFFSET = mTopOffset;
                mScreenFoldFullRatio = (((float) SCREEN_FOLD_FULL_HEIGHT) * 1.0f) / ((float) SCREEN_FOLD_FULL_WIDTH);
                mIsFoldScreenDevice = true;
                Slog.d(TAG, "parse prop ro.config.hw_fold_disp: [" + mLeftPartScreenWidth + ", " + mFoldingPartScreenWidth + ", " + mRightPartScreenWidth + ", " + mFullScreenHeight + ", " + mPhysicalFullScreenWidth + ", " + mPhysicalFullScreenHeight + "]");
            } catch (NumberFormatException e) {
                mIsFoldScreenDevice = false;
                Slog.e(TAG, "parse screenSizes config error");
            }
        } else {
            mIsFoldScreenDevice = false;
            Slog.w(TAG, "screenSizes config length error");
        }
    }

    public static boolean isFoldScreenDevice() {
        return mIsFoldScreenDevice;
    }

    public Rect getScreenDispRect(int orientation) {
        return null;
    }

    public int getDisplayMode() {
        return 0;
    }

    public int setDisplayMode(int mode) {
        return 0;
    }

    public void adjustViewportFrame(DisplayViewport viewport, Rect layerRect, Rect displayRect) {
    }

    public int rotateScreen() {
        return 0;
    }

    public static Rect getScreenPhysicalRect(int displayMode) {
        if (!mIsFoldScreenDevice) {
            return null;
        }
        if (2 == displayMode) {
            int i = SCREEN_FOLD_FULL_WIDTH;
            return new Rect(i - SCREEN_FOLD_MAIN_WIDTH, 0, i, SCREEN_FOLD_FULL_HEIGHT);
        } else if (3 == displayMode) {
            return new Rect(0, 0, SCREEN_FOLD_SUB_WIDTH, SCREEN_FOLD_FULL_HEIGHT);
        } else {
            return new Rect(0, 0, SCREEN_FOLD_FULL_WIDTH, SCREEN_FOLD_FULL_HEIGHT);
        }
    }

    public static void getAppFolderDisplayRect(int displayMode, float appMaxRatio, Rect outBounds, Rect appBounds, int rotation) {
        if (mIsFoldScreenDevice && appMaxRatio != 0.0f && displayMode == 1) {
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
        return mScreenFoldFullRatio;
    }
}
