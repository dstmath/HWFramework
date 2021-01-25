package android.util;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SmsManager;

public final class CoordinationModeUtils {
    public static final String COORDINATION_CREATE_MODE = "coordination_create_mode";
    public static final int COORDINATION_MODE_EXITING_RIGHT = 2;
    public static final int FINISH_ENTERING_COORDINATION_MODE = 2;
    public static final int FINISH_EXITING_COORDINATION_MODE = 4;
    public static final String HW_FOLD_DISP_PROP = "ro.config.hw_fold_disp";
    private static final int HW_FOLD_DISP_PROP_NUM = 6;
    private static final int HW_FOLD_EDGE_WIDTH_INDEX = 1;
    private static final int HW_FOLD_FULL_HEIGHT_INDEX = 3;
    private static final int HW_FOLD_MAIN_WIDTH_INDEX = 2;
    private static final int HW_FOLD_SUB_WIDTH_INDEX = 0;
    private static final boolean IS_FOLDABLE = (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty() || !SystemProperties.get(HwFoldScreenState.DEBUG_HW_FOLD_DISP_PROP).isEmpty());
    public static final int LEFT_TO_RIGHT = 3;
    public static final int NORMAL_MODE = 0;
    public static final int RIGHT_TO_LEFT = 4;
    public static final int START_ENTERING_COORDINATION_MODE = 1;
    public static final int START_EXITING_COORDINATION_MODE = 3;
    private static int sFoldScreenEdgeWidth;
    private static int sFoldScreenFullHeight;
    private static int sFoldScreenFullWidth;
    private static int sFoldScreenMainWidth;
    private static int sFoldScreenSubWidth;
    private static CoordinationModeUtils sInstance;
    private Context mContext;
    int mCoordinationCreateMode = 0;
    int mCoordinationState = 0;

    static {
        String[] screenSizes = SystemProperties.get("ro.config.hw_fold_disp").split(SmsManager.REGEX_PREFIX_DELIMITER);
        if (screenSizes.length == 6) {
            try {
                sFoldScreenSubWidth = Integer.parseInt(screenSizes[0]);
                sFoldScreenEdgeWidth = Integer.parseInt(screenSizes[1]);
                sFoldScreenMainWidth = Integer.parseInt(screenSizes[2]);
                sFoldScreenFullWidth = sFoldScreenSubWidth + sFoldScreenEdgeWidth + sFoldScreenMainWidth;
                sFoldScreenFullHeight = Integer.parseInt(screenSizes[3]);
            } catch (NumberFormatException e) {
                Log.e("CoordinationModeUtils", "HW_FOLD_DISP_PROP error");
            }
        }
    }

    private CoordinationModeUtils(Context context) {
        this.mContext = context;
    }

    public static synchronized CoordinationModeUtils getInstance(Context context) {
        CoordinationModeUtils coordinationModeUtils;
        synchronized (CoordinationModeUtils.class) {
            if (sInstance == null) {
                sInstance = new CoordinationModeUtils(context);
            }
            coordinationModeUtils = sInstance;
        }
        return coordinationModeUtils;
    }

    public static boolean isFoldable() {
        return IS_FOLDABLE;
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
        int i = this.mCoordinationState;
        return i == 1 || i == 3;
    }

    public boolean isEnteringCoordinationMode() {
        return this.mCoordinationState == 1;
    }

    public boolean isExitingCoordinationMode() {
        return this.mCoordinationState == 3;
    }

    public void getStackCoordinationModeBounds(boolean isCoordinationPrimary, int rotation, Rect outBounds) {
        if (rotation == 0) {
            calculateRotation0(isCoordinationPrimary, outBounds);
        } else if (rotation == 1) {
            calculateRotation90(isCoordinationPrimary, outBounds);
        } else if (rotation == 2) {
            calculateRotation180(isCoordinationPrimary, outBounds);
        } else if (rotation == 3) {
            calculateRotation270(isCoordinationPrimary, outBounds);
        }
    }

    private void calculateRotation0(boolean isCoordinationPrimary, Rect outBounds) {
        int i = this.mCoordinationCreateMode;
        if (i == 3) {
            if (isCoordinationPrimary) {
                outBounds.set(0, 0, sFoldScreenSubWidth, sFoldScreenFullHeight);
            } else {
                outBounds.set(sFoldScreenSubWidth + sFoldScreenEdgeWidth, 0, sFoldScreenFullWidth, sFoldScreenFullHeight);
            }
        } else if (i != 4 && i != 2) {
        } else {
            if (isCoordinationPrimary) {
                outBounds.set(sFoldScreenSubWidth + sFoldScreenEdgeWidth, 0, sFoldScreenFullWidth, sFoldScreenFullHeight);
            } else {
                outBounds.set(0, 0, sFoldScreenSubWidth, sFoldScreenFullHeight);
            }
        }
    }

    private void calculateRotation90(boolean isCoordinationPrimary, Rect outBounds) {
        int i = this.mCoordinationCreateMode;
        if (i == 3) {
            if (isCoordinationPrimary) {
                outBounds.set(0, sFoldScreenMainWidth + sFoldScreenEdgeWidth, sFoldScreenFullHeight, sFoldScreenFullWidth);
            } else {
                outBounds.set(0, 0, sFoldScreenFullHeight, sFoldScreenMainWidth);
            }
        } else if (i != 4 && i != 2) {
        } else {
            if (isCoordinationPrimary) {
                outBounds.set(0, 0, sFoldScreenFullHeight, sFoldScreenMainWidth);
            } else {
                outBounds.set(0, sFoldScreenMainWidth + sFoldScreenEdgeWidth, sFoldScreenFullHeight, sFoldScreenFullWidth);
            }
        }
    }

    private void calculateRotation180(boolean isCoordinationPrimary, Rect outBounds) {
        int i = this.mCoordinationCreateMode;
        if (i == 3) {
            if (isCoordinationPrimary) {
                outBounds.set(sFoldScreenMainWidth + sFoldScreenEdgeWidth, 0, sFoldScreenFullWidth, sFoldScreenFullHeight);
            } else {
                outBounds.set(0, 0, sFoldScreenMainWidth, sFoldScreenFullHeight);
            }
        } else if (i != 4 && i != 2) {
        } else {
            if (isCoordinationPrimary) {
                outBounds.set(0, 0, sFoldScreenMainWidth, sFoldScreenFullHeight);
            } else {
                outBounds.set(sFoldScreenMainWidth + sFoldScreenEdgeWidth, 0, sFoldScreenFullWidth, sFoldScreenFullHeight);
            }
        }
    }

    private void calculateRotation270(boolean isCoordinationPrimary, Rect outBounds) {
        int i = this.mCoordinationCreateMode;
        if (i == 3) {
            if (isCoordinationPrimary) {
                outBounds.set(0, 0, sFoldScreenFullHeight, sFoldScreenSubWidth);
            } else {
                outBounds.set(sFoldScreenSubWidth + sFoldScreenEdgeWidth, 0, sFoldScreenFullHeight, sFoldScreenFullWidth);
            }
        } else if (i != 4 && i != 2) {
        } else {
            if (isCoordinationPrimary) {
                outBounds.set(sFoldScreenSubWidth + sFoldScreenEdgeWidth, 0, sFoldScreenFullHeight, sFoldScreenFullWidth);
            } else {
                outBounds.set(0, 0, sFoldScreenFullHeight, sFoldScreenSubWidth);
            }
        }
    }

    public static int getFoldScreenMainWidth() {
        return sFoldScreenMainWidth;
    }

    public static int getFoldScreenEdgeWidth() {
        return sFoldScreenEdgeWidth;
    }

    public static int getFoldScreenSubWidth() {
        return sFoldScreenSubWidth;
    }

    public static int getFoldScreenFullWidth() {
        return sFoldScreenFullWidth;
    }

    public static int getFoldScreenFullHeight() {
        return sFoldScreenFullHeight;
    }
}
