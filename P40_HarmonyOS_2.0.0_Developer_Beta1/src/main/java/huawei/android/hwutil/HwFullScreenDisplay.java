package huawei.android.hwutil;

import android.graphics.Rect;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.util.Log;

public class HwFullScreenDisplay {
    private static final float DEFAULT_LARGE_NON_FULL_MAX_RATIO = 1.8555557f;
    private static final float DEFAULT_SMALL_NON_FULL_MAX_RATIO = 1.7777778f;
    private static final float EPSINON = 1.0E-6f;
    private static final float FULLSCREEN_MAX_RATIO_RANGE_DIVIDE = 2.075f;
    private static final String HW_FULLSCREEN_PROP = "ro.config.new_hw_screen_aspect";
    private static final float MIN_FULLSCREEN_MAX_RATIO = 2.0f;
    private static final String TAG = "HwFullScreenDisplay";
    private static boolean isFullScreenDevice;
    private static float sDefaultNonFullAspectRatio;
    private static int sDeviceWidth;
    private static float sExclusionNavBarAspectRatio;
    private static int sNavBarHeight;
    private static int sNotchHeight = 0;
    private static float sScreenAspectRatio;

    static {
        sScreenAspectRatio = 0.0f;
        sExclusionNavBarAspectRatio = 0.0f;
        sDefaultNonFullAspectRatio = 1.86f;
        boolean z = false;
        sNavBarHeight = 0;
        sDeviceWidth = 0;
        String[] aspects = SystemProperties.get(HW_FULLSCREEN_PROP).split(SettingsStringUtil.DELIMITER);
        if (aspects.length == 3) {
            try {
                int mDeviceHeight = Integer.parseInt(aspects[0]);
                int aspectHeight = Integer.parseInt(aspects[1]);
                sDeviceWidth = Integer.parseInt(aspects[2]);
                sScreenAspectRatio = (((float) mDeviceHeight) * 1.0f) / ((float) sDeviceWidth);
                if (sScreenAspectRatio >= 2.0f) {
                    z = true;
                }
                isFullScreenDevice = z;
                sExclusionNavBarAspectRatio = (((float) (aspectHeight - 1)) * 1.0f) / ((float) sDeviceWidth);
                sNavBarHeight = mDeviceHeight - aspectHeight;
                if (sScreenAspectRatio > FULLSCREEN_MAX_RATIO_RANGE_DIVIDE) {
                    sDefaultNonFullAspectRatio = DEFAULT_LARGE_NON_FULL_MAX_RATIO;
                } else if (sScreenAspectRatio >= 2.0f) {
                    sDefaultNonFullAspectRatio = 1.7777778f;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Parser max aspect config error ");
            }
        }
    }

    public static boolean isFullScreenDevice() {
        return isFullScreenDevice;
    }

    public static float getDeviceMaxRatio() {
        return sScreenAspectRatio;
    }

    public static float getDefaultNonFullMaxRatio() {
        return sDefaultNonFullAspectRatio;
    }

    public static float getExclusionNavBarMaxRatio() {
        return sExclusionNavBarAspectRatio;
    }

    public static void getAppDisplayRect(float appMaxRatio, Rect rect, int left, int rotation) {
        if (sDeviceWidth != 0 && appMaxRatio > 1.0f && appMaxRatio < sScreenAspectRatio) {
            int i = sNavBarHeight;
            if (rotation == 0 || rotation == 2) {
                int realBarHeight = (sNavBarHeight * rect.width()) / sDeviceWidth;
                int deviceHeight = (int) (((float) rect.width()) * sScreenAspectRatio);
                if (rect.bottom > deviceHeight - realBarHeight) {
                    rect.set(rect.left, rect.top, rect.width(), deviceHeight - realBarHeight);
                    return;
                }
                return;
            }
            int rectWidth = rect.width();
            if (showInMiddleWhenLand(appMaxRatio)) {
                rectWidth -= left;
            }
            int realBarHeight2 = (sNavBarHeight * rect.height()) / sDeviceWidth;
            int deviceWidth = (int) (((float) rect.height()) * sScreenAspectRatio);
            int moveRight = (deviceWidth - rectWidth) / 2;
            int mustShowWidth = sNotchHeight + realBarHeight2;
            if (showInMiddleWhenLand(appMaxRatio)) {
                if (rectWidth + moveRight > deviceWidth - mustShowWidth) {
                    int newLeft = moveRight - (mustShowWidth - moveRight);
                    rect.set(newLeft > 0 ? newLeft : 0, rect.top, deviceWidth - mustShowWidth, rect.bottom);
                    return;
                }
                rect.set(moveRight, rect.top, rectWidth + moveRight, rect.bottom);
            } else if (rect.right > deviceWidth - mustShowWidth) {
                rect.set(rect.left, rect.top, deviceWidth - mustShowWidth, rect.bottom);
            }
        }
    }

    private static boolean showInMiddleWhenLand(float appMaxRatio) {
        float f = sScreenAspectRatio;
        if (f > FULLSCREEN_MAX_RATIO_RANGE_DIVIDE) {
            return appMaxRatio <= DEFAULT_LARGE_NON_FULL_MAX_RATIO;
        }
        if (f >= 2.0f) {
            return appMaxRatio <= 1.7777778f;
        }
        return false;
    }

    public static void setNotchHeight(int notchHeight) {
        if (sNotchHeight == 0) {
            sNotchHeight = notchHeight;
        }
    }

    public static Rect getTopAppDisplayBounds(float appMaxRatio, int rotation, int screenWidth) {
        Rect rect = new Rect();
        if (appMaxRatio > 1.0f && appMaxRatio < sScreenAspectRatio) {
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
        sScreenAspectRatio = (((float) deviceHeight) * 1.0f) / ((float) deviceWidth);
        sExclusionNavBarAspectRatio = (((float) aspectHeight) * 1.0f) / ((float) deviceWidth);
        sNavBarHeight = deviceHeight - aspectHeight;
        float f = sScreenAspectRatio;
        if (f > FULLSCREEN_MAX_RATIO_RANGE_DIVIDE) {
            sDefaultNonFullAspectRatio = DEFAULT_LARGE_NON_FULL_MAX_RATIO;
        } else if (f >= 2.0f) {
            sDefaultNonFullAspectRatio = 1.7777778f;
        }
    }
}
