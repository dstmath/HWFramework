package com.android.server.lights;

import android.app.ActivityManager;
import android.content.Context;
import android.net.util.NetworkConstants;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings.Secure;
import android.util.Flog;
import android.util.Slog;
import android.view.ViewRootImpl;
import com.android.server.SystemService;
import com.android.server.display.DisplayTransformManager;
import com.android.server.usb.UsbAudioDevice;
import com.huawei.pgmng.common.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LightsService extends SystemService {
    static final boolean DEBUG = false;
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private static final String MAX_BRIGHTNESS_PATH = "/sys/class/leds/lcd_backlight0/max_brightness";
    static final String TAG = "LightsService";
    private static boolean inMirrorLinkBrightnessMode = false;
    private static long mAmountTime = 0;
    protected static boolean mHasShutDown = false;
    private static boolean mIsAutoAdjust = false;
    private static int mLcdBrightness = 100;
    public static int mMaxBrightnessFromKernel = 255;
    private static double mRatio = 1.0d;
    boolean POWER_CURVE_BLIGHT_SUPPORT;
    private boolean mBrightnessConflict = false;
    private int mCurBrightness = 100;
    private Handler mH;
    protected boolean mIsHighPrecision = false;
    final LightImpl[] mLights = new LightImpl[LightsManager.LIGHT_ID_COUNT];
    private int mLimitedMaxBrightness = -1;
    private final LightsManager mService;
    private boolean mVrModeEnabled;
    protected boolean mWriteAutoBrightnessDbEnable = true;

    private final class LightImpl extends Light {
        private int mBrightnessMode;
        private int mColor;
        private int mCurrentBrightness;
        private boolean mFlashing;
        private int mId;
        private boolean mInitialized;
        private int mLastBrightnessMode;
        private int mLastColor;
        private int mMode;
        private int mOffMS;
        private int mOnMS;
        private boolean mUseLowPersistenceForVR;
        private boolean mVrModeEnabled;

        /* synthetic */ LightImpl(LightsService this$0, int id, LightImpl -this2) {
            this(id);
        }

        private LightImpl(int id) {
            this.mId = id;
        }

        public void setLcdRatio(int ratio, boolean autoAdjust) {
            LightsService.mIsAutoAdjust = autoAdjust;
            if (ratio > 100 || ratio < 1) {
                LightsService.mRatio = 1.0d;
            } else {
                LightsService.mRatio = ((double) ratio) / 100.0d;
            }
            Slog.i(LightsService.TAG, "setLcdRatio ratio:" + ratio + " autoAdjust:" + autoAdjust);
            if (!LightsService.this.POWER_CURVE_BLIGHT_SUPPORT) {
                setLightGradualChange(LightsService.mLcdBrightness, 0, true);
            }
        }

        public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
            if (ratioMin == ratioMax && ratioMin == -1) {
                synchronized (this) {
                    if (autoLimit > 0) {
                        LightsService.this.mLimitedMaxBrightness = autoLimit;
                        if (LightsService.this.mCurBrightness > autoLimit && (LightsService.this.POWER_CURVE_BLIGHT_SUPPORT ^ 1) != 0) {
                            setLightGradualChange(autoLimit, 0, true);
                        }
                    } else {
                        LightsService.this.mLimitedMaxBrightness = -1;
                        if (LightsService.mLcdBrightness > 0 && (LightsService.this.POWER_CURVE_BLIGHT_SUPPORT ^ 1) != 0) {
                            setLightGradualChange(LightsService.mLcdBrightness, 0, true);
                        }
                    }
                }
                return;
            }
            Utils.configBrightnessRange(ratioMin, ratioMax, autoLimit);
        }

        public void sendSmartBackLightWithRefreshFrames(int enable, int level, int value, int frames, boolean setAfterRefresh, int enable2, int value2) {
            LightsService.this.sendSmartBackLightWithRefreshFramesImpl(this.mId, enable, level, value, frames, setAfterRefresh, enable2, value2);
        }

        public void writeAutoBrightnessDbEnable(boolean enable) {
            LightsService.this.mWriteAutoBrightnessDbEnable = enable;
            if (enable) {
                LightsService.this.sendUpdateaAutoBrightnessDbMsg();
            }
        }

        public void updateUserId(int userId) {
            LightsService.this.updateCurrentUserId(userId);
        }

        public boolean isHighPrecision() {
            return true;
        }

        public int getMaxBrightnessFromKernel() {
            return LightsService.mMaxBrightnessFromKernel;
        }

        public void updateBrightnessAdjustMode(boolean mode) {
            LightsService.this.updateBrightnessMode(mode);
        }

        public void sendSmartBackLight(int enable, int level, int value) {
            synchronized (this) {
                boolean z;
                if (value > NetworkConstants.ARP_HWTYPE_RESERVED_HI) {
                    value = NetworkConstants.ARP_HWTYPE_RESERVED_HI;
                }
                int lightValue = (((enable & 1) << 24) | ((level & 255) << 16)) | (value & NetworkConstants.ARP_HWTYPE_RESERVED_HI);
                Flog.i(NetdResponseCode.BandwidthControl, "set smart backlight. enable is " + enable + ",level is " + level + ",value is " + value + ",lightValue is " + lightValue);
                LightsService.setLight_native(this.mId, lightValue, 0, 0, 0, 0);
                if (enable == 0) {
                    z = true;
                } else {
                    z = false;
                }
                ViewRootImpl.setEnablePartialUpdate(z);
            }
        }

        public void sendCustomBackLight(int backlight) {
            if (!LightsService.inMirrorLinkBrightnessMode) {
                synchronized (this) {
                    LightsService.setLight_native(this.mId, backlight, 0, 0, 0, 0);
                }
            }
        }

        public void sendAmbientLight(int ambientLight) {
            synchronized (this) {
                LightsService.setLight_native(this.mId, ambientLight, 0, 0, 0, 0);
            }
        }

        public void sendSREWithRefreshFrames(int enable, int ambientLightThreshold, int ambientLight, int frames, boolean setAfterRefresh, int enable2, int ambientLight2) {
            LightsService.this.sendSREWithRefreshFramesImpl(this.mId, enable, ambientLightThreshold, ambientLight, frames, setAfterRefresh, enable2, ambientLight2);
        }

        public void setBrightness(int brightness) {
            if (!LightsService.inMirrorLinkBrightnessMode) {
                setBrightness(brightness, 0);
            }
        }

        public void setMirrorLinkBrightness(int target) {
            synchronized (this) {
                Slog.i(LightsService.TAG, "setMirrorLinkBrightnessStatus  brightness is " + target);
                int brightness = LightsService.this.mapIntoRealBacklightLevel((target * 10000) / 255);
                if (LightsService.this.mIsHighPrecision) {
                    setLightLocked_10000stage(brightness & NetworkConstants.ARP_HWTYPE_RESERVED_HI, 0, 0, 0, 0);
                } else {
                    int color = brightness & 255;
                    setLightLocked(color | (((color << 16) | UsbAudioDevice.kAudioDeviceMetaMask) | (color << 8)), 0, 0, 0, 0);
                }
            }
        }

        public void setMirrorLinkBrightnessStatus(boolean status) {
            Slog.i(LightsService.TAG, "setMirrorLinkBrightnessStatus  status is " + status);
            LightsService.inMirrorLinkBrightnessMode = status;
        }

        /* JADX WARNING: Missing block: B:50:0x00f5, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setBrightness(int brightness, int brightnessMode) {
            this.mCurrentBrightness = brightness;
            if (!LightsService.inMirrorLinkBrightnessMode && !LightsService.mHasShutDown) {
                if (this.mId == 0) {
                    LightsService.this.mBrightnessConflict = true;
                    LightsService.mLcdBrightness = brightness;
                    if (LightsService.this.mLimitedMaxBrightness > 0 && brightness > LightsService.this.mLimitedMaxBrightness) {
                        brightness = LightsService.this.mLimitedMaxBrightness;
                    }
                    LightsService.this.sendUpdateaAutoBrightnessDbMsg();
                    if (brightness == 0 || ((!LightsService.mIsAutoAdjust && LightsService.mRatio >= 1.0d) || (LightsService.this.POWER_CURVE_BLIGHT_SUPPORT ^ 1) == 0)) {
                        LightsService.this.mCurBrightness = brightness;
                    } else {
                        setLightGradualChange(brightness, brightnessMode, false);
                        return;
                    }
                }
                synchronized (this) {
                    if (brightnessMode == 2) {
                        Slog.w(LightsService.TAG, "setBrightness with LOW_PERSISTENCE unexpected #" + this.mId + ": brightness=0x" + Integer.toHexString(brightness));
                        return;
                    }
                    brightness = LightsService.this.mapIntoRealBacklightLevel(brightness);
                    int color;
                    if (LightsService.this.mIsHighPrecision) {
                        color = brightness & NetworkConstants.ARP_HWTYPE_RESERVED_HI;
                        if (this.mId == 2 && LightsService.FRONT_FINGERPRINT_NAVIGATION && LightsService.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                            color = (color * 255) / LightsService.this.getNormalizedMaxBrightness();
                            if (color == 0 && brightness != 0) {
                                color = 1;
                            }
                            color &= 255;
                            Slog.d(LightsService.TAG, "HighPrecision, Set button brihtness:" + color + ", bcaklight brightness:" + brightness);
                            setLightLocked(color, 0, 0, 0, brightnessMode);
                            return;
                        }
                        setLightLocked_10000stage(color, 0, 0, 0, brightnessMode);
                    } else {
                        color = brightness & 255;
                        if (this.mId == 2 && LightsService.FRONT_FINGERPRINT_NAVIGATION && LightsService.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                            Slog.i(LightsService.TAG, "Set button brihtness:" + color);
                            setLightLocked(color, 0, 0, 0, brightnessMode);
                            return;
                        }
                        setLightLocked(color | (((color << 16) | UsbAudioDevice.kAudioDeviceMetaMask) | (color << 8)), 0, 0, 0, brightnessMode);
                    }
                }
            }
        }

        public int getCurrentBrightness() {
            return this.mCurrentBrightness;
        }

        /* JADX WARNING: Missing block: B:53:?, code:
            android.util.Slog.i(com.android.server.lights.LightsService.TAG, "set brightness confict and break...");
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void setLightGradualChange(int brightness, int brightnessMode, boolean isPGset) {
            int tarBrightness = brightness;
            if (LightsService.mRatio < 1.0d) {
                tarBrightness = Utils.getRatioBright(brightness, LightsService.mRatio);
            }
            if (LightsService.mIsAutoAdjust) {
                tarBrightness = Utils.getAutoAdjustBright(tarBrightness);
            }
            if (!isPGset) {
                if (LightsService.this.mCurBrightness == 0 && tarBrightness > 0) {
                    LightsService.mAmountTime = SystemClock.elapsedRealtime();
                }
                if (SystemClock.elapsedRealtime() - LightsService.mAmountTime < 1000) {
                    LightsService.this.mCurBrightness = tarBrightness;
                }
            }
            if (LightsService.this.mLimitedMaxBrightness > 0 && tarBrightness > LightsService.this.mLimitedMaxBrightness) {
                tarBrightness = LightsService.this.mLimitedMaxBrightness;
            }
            int minAmount = 1;
            int brightnessGap = 25;
            if (brightness > 255 || isHighPrecision()) {
                minAmount = 2 * 39;
                brightnessGap = 26 * 39;
            }
            int steps = 20;
            if (LightsService.mRatio < 1.0d) {
                steps = 16;
            }
            int amount = Math.abs(LightsService.this.mCurBrightness - tarBrightness) / steps;
            int regulateTime = 0;
            LightsService.this.mBrightnessConflict = false;
            synchronized (this) {
                while (true) {
                    if (!LightsService.this.mBrightnessConflict && regulateTime >= DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE) {
                        amount = minAmount;
                    }
                    if (LightsService.this.mBrightnessConflict) {
                        if (LightsService.mRatio != 1.0d || LightsService.mLcdBrightness - LightsService.this.mCurBrightness <= brightnessGap) {
                            break;
                        }
                        amount = Math.abs(LightsService.this.mCurBrightness - tarBrightness) / 5;
                        LightsService.this.mBrightnessConflict = false;
                        Slog.i(LightsService.TAG, "confict and set amount = " + amount);
                    }
                    int setValue = Utils.getAnimatedValue(tarBrightness, LightsService.this.mCurBrightness, amount);
                    LightsService.this.mCurBrightness = setValue;
                    setValue = LightsService.this.mapIntoRealBacklightLevel(setValue);
                    if (LightsService.this.mIsHighPrecision) {
                        setLightLocked_10000stage(setValue & NetworkConstants.ARP_HWTYPE_RESERVED_HI, 0, 0, 0, brightnessMode);
                    } else {
                        int color = setValue & 255;
                        setLightLocked(color | (((color << 16) | UsbAudioDevice.kAudioDeviceMetaMask) | (color << 8)), 0, 0, 0, brightnessMode);
                    }
                    if (LightsService.mLcdBrightness != 0) {
                        if (LightsService.this.mCurBrightness != tarBrightness) {
                            SystemClock.sleep(16);
                            regulateTime += 16;
                        }
                        if (LightsService.this.mCurBrightness == tarBrightness) {
                            break;
                        }
                    } else {
                        Slog.w(LightsService.TAG, "synchronized conflict...");
                        break;
                    }
                }
            }
            LightsService.this.mBrightnessConflict = true;
        }

        public void setColor(int color) {
            synchronized (this) {
                setLightLocked(color, 0, 0, 0, 0);
            }
        }

        public void setFlashing(int color, int mode, int onMS, int offMS) {
            synchronized (this) {
                setLightLocked(color, mode, onMS, offMS, 0);
            }
        }

        public void pulse() {
            pulse(UsbAudioDevice.kAudioDeviceClassMask, 7);
        }

        public void pulse(int color, int onMS) {
            synchronized (this) {
                if (this.mColor == 0 && (this.mFlashing ^ 1) != 0) {
                    setLightLocked(color, 2, onMS, 1000, 0);
                    this.mColor = 0;
                    LightsService.this.mH.sendMessageDelayed(Message.obtain(LightsService.this.mH, 1, this), (long) onMS);
                }
            }
        }

        public void turnOff() {
            synchronized (this) {
                setLightLocked(0, 0, 0, 0, 0);
            }
        }

        public void setVrMode(boolean enabled) {
            boolean z = false;
            synchronized (this) {
                if (this.mVrModeEnabled != enabled) {
                    this.mVrModeEnabled = enabled;
                    if (LightsService.this.getVrDisplayMode() == 0) {
                        z = true;
                    }
                    this.mUseLowPersistenceForVR = z;
                    if (shouldBeInLowPersistenceMode()) {
                        this.mLastBrightnessMode = this.mBrightnessMode;
                    }
                }
            }
        }

        private void stopFlashing() {
            synchronized (this) {
                setLightLocked(this.mColor, 0, 0, 0, 0);
            }
        }

        private void setLightLocked(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (shouldBeInLowPersistenceMode()) {
                brightnessMode = 2;
            } else if (brightnessMode == 2) {
                brightnessMode = this.mLastBrightnessMode;
            }
            if (!this.mInitialized || color != this.mColor || mode != this.mMode || onMS != this.mOnMS || offMS != this.mOffMS || this.mBrightnessMode != brightnessMode) {
                this.mInitialized = true;
                this.mLastColor = this.mColor;
                this.mColor = color;
                this.mMode = mode;
                this.mOnMS = onMS;
                this.mOffMS = offMS;
                this.mBrightnessMode = brightnessMode;
                Trace.traceBegin(131072, "setLight(" + this.mId + ", 0x" + Integer.toHexString(color) + ")");
                try {
                    LightsService.setLight_native(this.mId, color, mode, onMS, offMS, brightnessMode);
                } finally {
                    Trace.traceEnd(131072);
                }
            }
        }

        private boolean shouldBeInLowPersistenceMode() {
            return this.mVrModeEnabled ? this.mUseLowPersistenceForVR : false;
        }

        private void setLightLocked_10000stage(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (color != this.mColor || mode != this.mMode || onMS != this.mOnMS || offMS != this.mOffMS) {
                this.mColor = color;
                this.mMode = mode;
                this.mOnMS = onMS;
                this.mOffMS = offMS;
                Trace.traceBegin(131072, "setLight(" + this.mId + ", " + color + ")");
                try {
                    LightsService.setLight_native(LightsManager.LIGHT_ID_BACKLIGHT_10000, color, mode, onMS, offMS, brightnessMode);
                } finally {
                    Trace.traceEnd(131072);
                }
            }
        }

        public int getDeviceActualBrightnessLevel() {
            return LightsService.this.getDeviceActualBrightnessLevelImpl();
        }

        public int getDeviceActualBrightnessNit() {
            return LightsService.this.getDeviceActualBrightnessNitImpl();
        }

        public int getDeviceStandardBrightnessNit() {
            return LightsService.this.getDeviceStandardBrightnessNitImpl();
        }
    }

    private static native void finalize_native();

    protected static native void refreshFrames_native();

    static native void setBackLightMaxLevel_native(int i);

    static native void setHighPrecisionFlag_native(long j, int i);

    static native void setLight_native(int i, int i2, int i3, int i4, int i5, int i6);

    public LightsService(Context context) {
        boolean z = true;
        super(context);
        if (SystemProperties.get("ro.config.blight_power_curve", "").length() <= 0) {
            z = false;
        }
        this.POWER_CURVE_BLIGHT_SUPPORT = z;
        this.mService = new LightsManager() {
            public Light getLight(int id) {
                if (id < 0 || id >= LightsManager.LIGHT_ID_COUNT) {
                    return null;
                }
                return LightsService.this.mLights[id];
            }
        };
        this.mH = new Handler() {
            public void handleMessage(Message msg) {
                msg.obj.stopFlashing();
            }
        };
        mHasShutDown = false;
        for (int i = 0; i < LightsManager.LIGHT_ID_COUNT; i++) {
            this.mLights[i] = new LightImpl(this, i, null);
        }
        getMaxBrightnessFromKerenl();
        setLight_native(3, 0, 0, 0, 0, 0);
    }

    public void onStart() {
        publishLocalService(LightsManager.class, this.mService);
    }

    public void onBootPhase(int phase) {
    }

    private int getVrDisplayMode() {
        return Secure.getIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser());
    }

    protected void finalize() throws Throwable {
        finalize_native();
        super.finalize();
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0035 A:{SYNTHETIC, Splitter: B:20:0x0035} */
    /* JADX WARNING: Removed duplicated region for block: B:36:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002c A:{SYNTHETIC, Splitter: B:15:0x002c} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x003e A:{SYNTHETIC, Splitter: B:25:0x003e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getMaxBrightnessFromKerenl() {
        Throwable th;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(MAX_BRIGHTNESS_PATH)));
            try {
                String tempString = reader2.readLine();
                if (tempString != null) {
                    mMaxBrightnessFromKernel = Integer.parseInt(tempString);
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e) {
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e2) {
                reader = reader2;
                if (reader == null) {
                }
            } catch (IOException e3) {
                reader = reader2;
                if (reader == null) {
                }
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e4) {
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                }
            }
        } catch (IOException e6) {
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e7) {
                }
            }
        } catch (Throwable th3) {
            th = th3;
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
    }

    public void sendSmartBackLightWithRefreshFramesImpl(int id, int enable, int level, int value, int frames, boolean setAfterRefresh, int enable2, int value2) {
    }

    public void sendSREWithRefreshFramesImpl(int id, int enable, int ambientLightThreshold, int ambientLight, int frames, boolean setAfterRefresh, int enable2, int ambientLight2) {
    }

    protected void sendUpdateaAutoBrightnessDbMsg() {
    }

    protected void updateBrightnessMode(boolean mode) {
    }

    protected int getLcdBrightnessMode() {
        return mLcdBrightness;
    }

    protected int mapIntoRealBacklightLevel(int level) {
        return level;
    }

    protected void updateCurrentUserId(int userId) {
    }

    public int getDeviceActualBrightnessLevelImpl() {
        return 0;
    }

    public int getDeviceActualBrightnessNitImpl() {
        return 0;
    }

    public int getDeviceStandardBrightnessNitImpl() {
        return 0;
    }

    protected int getNormalizedMaxBrightness() {
        return mMaxBrightnessFromKernel;
    }
}
