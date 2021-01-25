package com.android.server.wm;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import com.huawei.android.fsm.HwFoldScreenManagerEx;

public class HwRogEx implements IHwRogEx {
    private static final int NEW_DEFAULT_LCD_DENSITY = 530;
    private static final int NEW_DISPLAY_MODE_DEFAULT = 530;
    private static final int NEW_DISPLAY_MODE_LARGE = 600;
    private static final int NEW_DISPLAY_MODE_SMALL = 450;
    private static final int NEW_HIGH_ROG_DPI = 530;
    private static final int NEW_LOW_ROG_DPI = 353;
    private static final int OLD_DISPLAY_MODE_DEFAULT = 480;
    private static final int OLD_DISPLAY_MODE_LARGE = 540;
    private static final int OLD_DISPLAY_MODE_LARGER = 600;
    private static final int OLD_HIGH_ROG_DPI = 480;
    private static final String TAG = "HwRogEx";
    private static volatile HwRogEx sInstance = null;

    private HwRogEx() {
    }

    public static HwRogEx getDefault() {
        if (sInstance == null) {
            synchronized (HwRogEx.class) {
                if (sInstance == null) {
                    sInstance = new HwRogEx();
                    Slog.i(TAG, "APS: new HwRogEx created");
                }
            }
        }
        return sInstance;
    }

    public void initRogModeAndProcessMultiDpi(WindowManagerService wm, Context context) {
        initRogMode(wm, context);
        processDensityChanged(wm, context);
        processMultiDpi(wm);
    }

    private void processMultiDpi(WindowManagerService wm) {
        int dpi = SystemProperties.getInt("persist.sys.dpi", 0);
        if (SystemProperties.getInt("persist.sys.rog.width", 0) > 0) {
            dpi = SystemProperties.getInt("persist.sys.realdpi", SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0))));
        }
        if (dpi > 0) {
            wm.setForcedDisplayDensityForUser(0, dpi, UserHandle.myUserId());
        }
    }

    private void processDensityChanged(WindowManagerService wm, Context context) {
        int realdpi;
        if (wm != null) {
            if (!isDensityChanged()) {
                Slog.d(TAG, "density not change and do nothing");
            } else if (!isChangeRogOrDisplaymode()) {
                Slog.d(TAG, "user not change rog and displaymode, do nothing");
            } else {
                int oldDisplayDpi = SystemProperties.getInt("persist.sys.dpi", 0);
                int newDisplayDpi = getNewDisplayModeDpi(oldDisplayDpi);
                if (oldDisplayDpi == newDisplayDpi) {
                    Slog.d(TAG, "old density equals new density, do nothing");
                    return;
                }
                if (oldDisplayDpi != 0) {
                    new Configuration().extraConfig.setDensityDPI(newDisplayDpi);
                    realdpi = SystemProperties.getInt("persist.sys.realdpi", newDisplayDpi);
                } else {
                    realdpi = getRogRealDpi();
                    SystemProperties.set("persist.sys.realdpi", Integer.toString(realdpi));
                }
                Settings.Secure.putStringForUser(context.getContentResolver(), "display_density_forced", Integer.toString(realdpi), UserHandle.myUserId());
                Slog.d(TAG, "recompute and enable dpi and realdpi");
            }
        }
    }

    private boolean isDensityChanged() {
        if ("ELS".equals(SystemProperties.get("ro.build.product", ""))) {
            boolean isFirstboot = SystemProperties.getInt("persist.sys.aps.firstboot", 1) > 0;
            int currentSfDensity = SystemProperties.getInt("ro.sf.lcd_density", 0);
            if (isFirstboot) {
                SystemProperties.set("persist.sys.sf.last_lcd_density", Integer.toString(currentSfDensity));
            } else {
                int originSfDensity = SystemProperties.getInt("persist.sys.sf.last_lcd_density", 0);
                if (currentSfDensity == 530 && currentSfDensity != originSfDensity) {
                    SystemProperties.set("persist.sys.sf.last_lcd_density", Integer.toString(currentSfDensity));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isChangeRogOrDisplaymode() {
        if (SystemProperties.getInt("persist.sys.realdpi", 0) == 0 && SystemProperties.getInt("persist.sys.dpi", 0) == 0) {
            return false;
        }
        return true;
    }

    private int getRogRealDpi() {
        if (SystemProperties.getInt("persist.sys.realdpi", 0) >= 480) {
            return 530;
        }
        return NEW_LOW_ROG_DPI;
    }

    private int getNewDisplayModeDpi(int originDpi) {
        if (originDpi == 0) {
            return SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        }
        if (originDpi >= 600) {
            return 600;
        }
        if (originDpi < OLD_DISPLAY_MODE_LARGE && originDpi < 480) {
            return NEW_DISPLAY_MODE_SMALL;
        }
        return 530;
    }

    private void initRogMode(WindowManagerService wm, Context context) {
        if (wm != null) {
            if (!SystemProperties.get("ro.runmode", "normal").equals("factory")) {
                if (SystemProperties.getInt("sys.bopd", 0) != 1) {
                    if (SystemProperties.getInt("persist.sys.aps.firstboot", 1) > 0 || SystemProperties.getInt("persist.sys.rog.width", 0) == 0) {
                        int initWidth = SystemProperties.getInt("sys.rog.width", 0);
                        int initHeight = SystemProperties.getInt("sys.rog.height", 0);
                        int initDensity = SystemProperties.getInt("sys.rog.density", 0);
                        if (initWidth != 0 && initHeight != 0 && initDensity != 0) {
                            int density = getRealDpiBasedRog(initDensity);
                            SystemProperties.set("persist.sys.realdpi", Integer.toString(density));
                            SystemProperties.set("persist.sys.rog.width", Integer.toString(initWidth));
                            SystemProperties.set("persist.sys.rog.height", Integer.toString(initHeight));
                            ContentResolver contentResolver = context.getContentResolver();
                            Settings.Global.putString(contentResolver, "display_size_forced", initWidth + "," + initHeight);
                            Settings.Secure.putStringForUser(context.getContentResolver(), "display_density_forced", Integer.toString(density), UserHandle.myUserId());
                            SystemProperties.set("persist.sys.rog.configmode", "1");
                            Slog.i(TAG, "initRogMode and setForcedDisplaySize, initWidth = " + initWidth + "; initHeight = " + initHeight + "; density = " + density);
                            Settings.Global.putInt(context.getContentResolver(), "aps_display_resolution", 2);
                            Settings.Global.putInt(context.getContentResolver(), "low_resolution_switch", 1);
                        }
                    }
                }
            }
            String productName = SystemProperties.get("ro.build.product", "");
            boolean isFoldableScreen = HwFoldScreenManagerEx.isFoldable();
            if ("TAH".equals(productName) && isFoldableScreen) {
                int rogWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
                Rect fullRect = HwFoldScreenState.getScreenPhysicalRect(1);
                Settings.Global.putInt(context.getContentResolver(), "aps_display_style", 0);
                Slog.i(TAG, "APS: HwRogEx, TAH, rogWidth =  " + rogWidth + ", fullRect = " + fullRect);
                if (rogWidth != 0 && rogWidth != fullRect.height()) {
                    int targetWidth = fullRect.width();
                    int targetHeight = fullRect.height();
                    int targetDensity = SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.lcd_density", 0));
                    ContentResolver contentResolver2 = context.getContentResolver();
                    Settings.Global.putString(contentResolver2, "display_size_forced", targetWidth + "," + targetHeight);
                    Settings.Secure.putStringForUser(context.getContentResolver(), "display_density_forced", Integer.toString(targetDensity), UserHandle.myUserId());
                    SystemProperties.set("persist.sys.rog.configmode", "0");
                    SystemProperties.set("persist.sys.realdpi", Integer.toString(targetDensity));
                    SystemProperties.set("persist.sys.rog.width", Integer.toString(targetHeight));
                    SystemProperties.set("persist.sys.rog.height", Integer.toString(targetWidth));
                }
            }
        }
    }

    private int getRealDpiBasedRog(int rogDpi) {
        int originLcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        return (SystemProperties.getInt("persist.sys.dpi", originLcdDpi) * rogDpi) / originLcdDpi;
    }
}
