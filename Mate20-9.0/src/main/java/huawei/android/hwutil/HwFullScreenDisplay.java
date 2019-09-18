package huawei.android.hwutil;

import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.Log;

public class HwFullScreenDisplay {
    private static final float DEFAULT_LARGE_NON_FULL_MAX_RATIO = 1.8555557f;
    private static final float DEFAULT_SMALL_NON_FULL_MAX_RATIO = 1.7777778f;
    private static final float EPSINON = 1.0E-6f;
    private static final float FULLSCREEN_MAX_RATIO_RANGE_DIVIDE = 2.075f;
    private static final String HW_FULLSCREEN_PROP = "ro.config.new_hw_screen_aspect";
    private static final float MIN_FULLSCREEN_MAX_RATIO = 2.0f;
    private static final String TAG = "HwFullScreenDisplay";
    private static float mDefaultNonFullAspectRatio;
    private static int mDeviceWidth;
    private static float mExclusionNavBarAspectRatio;
    private static int mNavBarHeight;
    private static int mNotchHeight = 0;
    private static float mScreenAspectRatio;

    static {
        mScreenAspectRatio = 0.0f;
        mExclusionNavBarAspectRatio = 0.0f;
        mDefaultNonFullAspectRatio = 1.86f;
        mNavBarHeight = 0;
        mDeviceWidth = 0;
        String[] aspects = SystemProperties.get(HW_FULLSCREEN_PROP).split(":");
        if (aspects.length == 3) {
            try {
                int mDeviceHeight = Integer.parseInt(aspects[0]);
                int aspectHeight = Integer.parseInt(aspects[1]);
                mDeviceWidth = Integer.parseInt(aspects[2]);
                mScreenAspectRatio = (((float) mDeviceHeight) * 1.0f) / ((float) mDeviceWidth);
                mExclusionNavBarAspectRatio = (((float) (aspectHeight - 1)) * 1.0f) / ((float) mDeviceWidth);
                mNavBarHeight = mDeviceHeight - aspectHeight;
                if (mScreenAspectRatio > FULLSCREEN_MAX_RATIO_RANGE_DIVIDE) {
                    mDefaultNonFullAspectRatio = DEFAULT_LARGE_NON_FULL_MAX_RATIO;
                } else if (mScreenAspectRatio >= MIN_FULLSCREEN_MAX_RATIO) {
                    mDefaultNonFullAspectRatio = DEFAULT_SMALL_NON_FULL_MAX_RATIO;
                }
            } catch (Exception e) {
                Log.e(TAG, "Parser max aspect config error ");
            }
        }
    }

    public static boolean isFullScreenDevice() {
        return mScreenAspectRatio >= MIN_FULLSCREEN_MAX_RATIO;
    }

    public static float getDeviceMaxRatio() {
        return mScreenAspectRatio;
    }

    public static float getDefaultNonFullMaxRatio() {
        return mDefaultNonFullAspectRatio;
    }

    public static float getExclusionNavBarMaxRatio() {
        return mExclusionNavBarAspectRatio;
    }

    public static void getAppDisplayRect(float appMaxRatio, Rect rect, int left, int rotation) {
        if (mDeviceWidth != 0 && appMaxRatio > 1.0f && appMaxRatio < mScreenAspectRatio) {
            int i = mNavBarHeight;
            if (rotation == 0 || rotation == 2) {
                int realBarHeight = (mNavBarHeight * rect.width()) / mDeviceWidth;
                int deviceHeight = (int) (((float) rect.width()) * mScreenAspectRatio);
                if (rect.bottom > deviceHeight - realBarHeight) {
                    rect.set(rect.left, rect.top, rect.width(), deviceHeight - realBarHeight);
                }
            } else {
                int rectWidth = rect.width();
                if (showInMiddleWhenLand(appMaxRatio)) {
                    rectWidth -= left;
                }
                int realBarHeight2 = (mNavBarHeight * rect.height()) / mDeviceWidth;
                int deviceWidth = (int) (((float) rect.height()) * mScreenAspectRatio);
                int moveRight = (deviceWidth - rectWidth) / 2;
                int mustShowWidth = mNotchHeight + realBarHeight2;
                if (showInMiddleWhenLand(appMaxRatio)) {
                    if (rectWidth + moveRight > deviceWidth - mustShowWidth) {
                        int newLeft = moveRight - (mustShowWidth - moveRight);
                        rect.set(newLeft > 0 ? newLeft : 0, rect.top, deviceWidth - mustShowWidth, rect.bottom);
                    } else {
                        rect.set(moveRight, rect.top, rectWidth + moveRight, rect.bottom);
                    }
                } else if (rect.right > deviceWidth - mustShowWidth) {
                    rect.set(rect.left, rect.top, deviceWidth - mustShowWidth, rect.bottom);
                }
            }
        }
    }

    private static boolean showInMiddleWhenLand(float appMaxRatio) {
        boolean z = true;
        if (mScreenAspectRatio > FULLSCREEN_MAX_RATIO_RANGE_DIVIDE) {
            if (appMaxRatio > DEFAULT_LARGE_NON_FULL_MAX_RATIO) {
                z = false;
            }
            return z;
        } else if (mScreenAspectRatio < MIN_FULLSCREEN_MAX_RATIO) {
            return false;
        } else {
            if (appMaxRatio > DEFAULT_SMALL_NON_FULL_MAX_RATIO) {
                z = false;
            }
            return z;
        }
    }

    public static void setNotchHeight(int notchHeight) {
        if (mNotchHeight == 0) {
            mNotchHeight = notchHeight;
        }
    }

    public static Rect getTopAppDisplayBounds(float appMaxRatio, int rotation, int screenWidth) {
        Rect rect = new Rect();
        if (appMaxRatio > 1.0f && appMaxRatio < mScreenAspectRatio) {
            int temp = (int) ((((float) screenWidth) * appMaxRatio) + 0.5f);
            if (rotation == 0 || rotation == 2) {
                rect.set(0, 0, screenWidth, temp);
            } else {
                rect.set(0, 0, temp, screenWidth);
            }
            getAppDisplayRect(appMaxRatio, rect, 0, rotation);
        }
        return rect;
    }

    public static void setFullScreenData(int deviceHeight, int aspectHeight, int deviceWidth) {
        mScreenAspectRatio = (((float) deviceHeight) * 1.0f) / ((float) deviceWidth);
        mExclusionNavBarAspectRatio = (((float) aspectHeight) * 1.0f) / ((float) deviceWidth);
        mNavBarHeight = deviceHeight - aspectHeight;
        if (mScreenAspectRatio > FULLSCREEN_MAX_RATIO_RANGE_DIVIDE) {
            mDefaultNonFullAspectRatio = DEFAULT_LARGE_NON_FULL_MAX_RATIO;
        } else if (mScreenAspectRatio >= MIN_FULLSCREEN_MAX_RATIO) {
            mDefaultNonFullAspectRatio = DEFAULT_SMALL_NON_FULL_MAX_RATIO;
        }
    }
}
