package android.util;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SmsManager;
import com.huawei.hwaps.IHwApsImpl;

public final class CoordinationModeUtils {
    public static final String COORDINATION_CREATE_MODE = "coordination_create_mode";
    public static final int COORDINATION_MODE_EXITING_RIGHT = 2;
    public static final int FINISH_ENTERING_COORDINATION_MODE = 2;
    public static final int FINISH_EXITING_COORDINATION_MODE = 4;
    public static final String HW_FOLD_DISP_PROP = "ro.config.hw_fold_disp";
    private static final boolean IS_FOLDABLE = (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty() || !SystemProperties.get(HwFoldScreenState.DEBUG_HW_FOLD_DISP_PROP).isEmpty());
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
    private Context mContext;
    int mCoordinationCreateMode = 0;
    int mCoordinationState = 0;

    static {
        String[] screenSizes = SystemProperties.get("ro.config.hw_fold_disp").split(SmsManager.REGEX_PREFIX_DELIMITER);
        if (screenSizes.length == 6) {
            try {
                mFoldScreenSubWidth = Integer.parseInt(screenSizes[0]);
                mFoldScreenEdgeWidth = Integer.parseInt(screenSizes[1]);
                mFoldScreenMainWidth = Integer.parseInt(screenSizes[2]);
                mFoldScreenFullWidth = mFoldScreenSubWidth + mFoldScreenEdgeWidth + mFoldScreenMainWidth;
                mFoldScreenFullHeight = Integer.parseInt(screenSizes[3]);
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
            if (mInstance == null) {
                mInstance = new CoordinationModeUtils(context);
            }
            coordinationModeUtils = mInstance;
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
            int i = this.mCoordinationCreateMode;
            if (i == 3) {
                if (isCoordinationPrimary) {
                    outBounds.set(0, 0, mFoldScreenSubWidth, mFoldScreenFullHeight);
                } else {
                    outBounds.set(mFoldScreenSubWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullWidth, mFoldScreenFullHeight);
                }
            } else if (i == 4 || i == 2) {
                if (isCoordinationPrimary) {
                    outBounds.set(mFoldScreenSubWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullWidth, mFoldScreenFullHeight);
                } else {
                    outBounds.set(0, 0, mFoldScreenSubWidth, mFoldScreenFullHeight);
                }
            }
        } else if (rotation == 1) {
            int i2 = this.mCoordinationCreateMode;
            if (i2 == 3) {
                if (isCoordinationPrimary) {
                    outBounds.set(0, mFoldScreenMainWidth + mFoldScreenEdgeWidth, mFoldScreenFullHeight, mFoldScreenFullWidth);
                } else {
                    outBounds.set(0, 0, mFoldScreenFullHeight, mFoldScreenMainWidth);
                }
            } else if (i2 == 4 || i2 == 2) {
                if (isCoordinationPrimary) {
                    outBounds.set(0, 0, mFoldScreenFullHeight, mFoldScreenMainWidth);
                } else {
                    outBounds.set(0, mFoldScreenMainWidth + mFoldScreenEdgeWidth, mFoldScreenFullHeight, mFoldScreenFullWidth);
                }
            }
        } else if (rotation == 2) {
            int i3 = this.mCoordinationCreateMode;
            if (i3 == 3) {
                if (isCoordinationPrimary) {
                    outBounds.set(mFoldScreenMainWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullWidth, mFoldScreenFullHeight);
                } else {
                    outBounds.set(0, 0, mFoldScreenMainWidth, mFoldScreenFullHeight);
                }
            } else if (i3 == 4 || i3 == 2) {
                if (isCoordinationPrimary) {
                    outBounds.set(0, 0, mFoldScreenMainWidth, mFoldScreenFullHeight);
                } else {
                    outBounds.set(mFoldScreenMainWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullWidth, mFoldScreenFullHeight);
                }
            }
        } else if (rotation == 3) {
            int i4 = this.mCoordinationCreateMode;
            if (i4 == 3) {
                if (isCoordinationPrimary) {
                    outBounds.set(0, 0, mFoldScreenFullHeight, mFoldScreenSubWidth);
                } else {
                    outBounds.set(mFoldScreenSubWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullHeight, mFoldScreenFullWidth);
                }
            } else if (i4 == 4 || i4 == 2) {
                if (isCoordinationPrimary) {
                    outBounds.set(mFoldScreenSubWidth + mFoldScreenEdgeWidth, 0, mFoldScreenFullHeight, mFoldScreenFullWidth);
                } else {
                    outBounds.set(0, 0, mFoldScreenFullHeight, mFoldScreenSubWidth);
                }
            }
        }
        outBounds.scale(getRogRatio());
    }

    private static float getRogRatio() {
        IHwApsImpl apsImpl = HwFrameworkFactory.getHwApsImpl();
        if (apsImpl != null) {
            return apsImpl.getRogRatio();
        }
        return 1.0f;
    }

    private static int scaleSizeInRog(int originSize) {
        return (int) ((((float) originSize) * getRogRatio()) + 0.5f);
    }

    public static int getFoldScreenMainWidth() {
        return scaleSizeInRog(mFoldScreenMainWidth);
    }

    public static int getFoldScreenEdgeWidth() {
        return scaleSizeInRog(mFoldScreenEdgeWidth);
    }

    public static int getFoldScreenSubWidth() {
        return scaleSizeInRog(mFoldScreenSubWidth);
    }

    public static int getFoldScreenFullWidth() {
        return scaleSizeInRog(mFoldScreenFullWidth);
    }

    public static int getFoldScreenFullHeight() {
        return scaleSizeInRog(mFoldScreenFullHeight);
    }
}
