package android.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.provider.Settings;

public final class CoordinationModeUtils {
    public static final String COORDINATION_CREATE_MODE = "coordination_create_mode";
    public static final int FINISH_ENTERING_COORDINATION_MODE = 2;
    public static final int FINISH_EXITING_COORDINATION_MODE = 4;
    public static final String HW_FOLD_DISP_PROP = "ro.config.hw_fold_disp";
    public static final int LEFT_TO_RIGHT = 3;
    public static final int NORMAL_MODE = 0;
    public static final int RIGHT_TO_LEFT = 4;
    public static final int START_ENTERING_COORDINATION_MODE = 1;
    public static final int START_EXITING_COORDINATION_MODE = 3;
    private static int mFoldScreenEdgeWidth;
    private static int mFoldScreenFullHeight;
    private static int mFoldScreenFullWidth;
    private static int mFoldScreenMainWidth;
    private static int mFoldScreenSubWidth;
    private static CoordinationModeUtils mInstance;
    private static final boolean mIsFoldable = (!SystemProperties.get(HW_FOLD_DISP_PROP).isEmpty() || !SystemProperties.get("persist.sys.fold.disp.size").isEmpty());
    private Context mContext;
    int mCoordinationCreateMode = 0;
    int mCoordinationState = 0;

    static {
        String[] screenSizes = SystemProperties.get(HW_FOLD_DISP_PROP).split(",");
        if (screenSizes.length == 6) {
            try {
                mFoldScreenSubWidth = Integer.parseInt(screenSizes[0]);
                mFoldScreenEdgeWidth = Integer.parseInt(screenSizes[1]);
                mFoldScreenMainWidth = Integer.parseInt(screenSizes[2]);
                mFoldScreenFullWidth = mFoldScreenSubWidth + mFoldScreenEdgeWidth + mFoldScreenMainWidth;
                mFoldScreenFullHeight = Integer.parseInt(screenSizes[3]);
            } catch (Exception e) {
            }
        }
    }

    private CoordinationModeUtils(Context context) {
        this.mContext = context;
    }

    public static synchronized CoordinationModeUtils getInstance(Context context) {
        CoordinationModeUtils coordinationModeUtils;
        synchronized (CoordinationModeUtils.class) {
            if (mInstance == null) {
                mInstance = new CoordinationModeUtils(context);
            }
            coordinationModeUtils = mInstance;
        }
        return coordinationModeUtils;
    }

    public static boolean isFoldable() {
        return mIsFoldable;
    }

    public void setCoordinationCreateMode(int mode) {
        Settings.Global.putInt(this.mContext.getContentResolver(), COORDINATION_CREATE_MODE, mode);
        this.mCoordinationCreateMode = mode;
    }

    public int getCoordinationCreateMode() {
        return this.mCoordinationCreateMode;
    }

    public void setCoordinationState(int state) {
        this.mCoordinationState = state;
    }

    public int getCoordinationState() {
        return this.mCoordinationState;
    }

    public boolean isEnterOrExitCoordinationMode() {
        return this.mCoordinationState == 1 || this.mCoordinationState == 3;
    }

    public boolean isEnteringCoordinationMode() {
        return this.mCoordinationState == 1;
    }

    public boolean isExitingCoordinationMode() {
        return this.mCoordinationState == 3;
    }

    public void getStackCoordinationModeBounds(boolean isCoordinationPrimary, int rotation, Rect outBounds) {
        switch (rotation) {
            case 0:
                if (this.mCoordinationCreateMode == 3) {
                    if (isCoordinationPrimary) {
                        outBounds.set(0, 0, mFoldScreenSubWidth, mFoldScreenFullHeight);
                        return;
                    } else {
                        outBounds.set(mFoldScreenSubWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullWidth, mFoldScreenFullHeight);
                        return;
                    }
                } else if (this.mCoordinationCreateMode != 4) {
                    return;
                } else {
                    if (isCoordinationPrimary) {
                        outBounds.set(mFoldScreenSubWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullWidth, mFoldScreenFullHeight);
                        return;
                    } else {
                        outBounds.set(0, 0, mFoldScreenSubWidth, mFoldScreenFullHeight);
                        return;
                    }
                }
            case 1:
                if (this.mCoordinationCreateMode == 3) {
                    if (isCoordinationPrimary) {
                        outBounds.set(0, mFoldScreenMainWidth + mFoldScreenEdgeWidth, mFoldScreenFullHeight, mFoldScreenFullWidth);
                        return;
                    } else {
                        outBounds.set(0, 0, mFoldScreenFullHeight, mFoldScreenMainWidth);
                        return;
                    }
                } else if (this.mCoordinationCreateMode != 4) {
                    return;
                } else {
                    if (isCoordinationPrimary) {
                        outBounds.set(0, 0, mFoldScreenFullHeight, mFoldScreenMainWidth);
                        return;
                    } else {
                        outBounds.set(0, mFoldScreenMainWidth + mFoldScreenEdgeWidth, mFoldScreenFullHeight, mFoldScreenFullWidth);
                        return;
                    }
                }
            case 2:
                if (this.mCoordinationCreateMode == 3) {
                    if (isCoordinationPrimary) {
                        outBounds.set(mFoldScreenMainWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullWidth, mFoldScreenFullHeight);
                        return;
                    } else {
                        outBounds.set(0, 0, mFoldScreenMainWidth, mFoldScreenFullHeight);
                        return;
                    }
                } else if (this.mCoordinationCreateMode != 4) {
                    return;
                } else {
                    if (isCoordinationPrimary) {
                        outBounds.set(0, 0, mFoldScreenMainWidth, mFoldScreenFullHeight);
                        return;
                    } else {
                        outBounds.set(mFoldScreenMainWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullWidth, mFoldScreenFullHeight);
                        return;
                    }
                }
            case 3:
                if (this.mCoordinationCreateMode == 3) {
                    if (isCoordinationPrimary) {
                        outBounds.set(0, 0, mFoldScreenFullHeight, mFoldScreenSubWidth);
                        return;
                    } else {
                        outBounds.set(mFoldScreenSubWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullHeight, mFoldScreenFullWidth);
                        return;
                    }
                } else if (this.mCoordinationCreateMode != 4) {
                    return;
                } else {
                    if (isCoordinationPrimary) {
                        outBounds.set(mFoldScreenSubWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullHeight, mFoldScreenFullWidth);
                        return;
                    } else {
                        outBounds.set(0, 0, mFoldScreenFullHeight, mFoldScreenSubWidth);
                        return;
                    }
                }
            default:
                return;
        }
    }

    public static int getFoldScreenMainWidth() {
        return mFoldScreenMainWidth;
    }

    public static int getFoldScreenEdgeWidth() {
        return mFoldScreenEdgeWidth;
    }

    public static int getFoldScreenSubWidth() {
        return mFoldScreenSubWidth;
    }

    public static int getFoldScreenFullWidth() {
        return mFoldScreenFullWidth;
    }

    public static int getFoldScreenFullHeight() {
        return mFoldScreenFullHeight;
    }
}
