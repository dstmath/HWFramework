package com.android.server.lights;

import android.app.ActivityManager;
import android.content.Context;
import android.net.util.NetworkConstants;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings;
import android.util.Flog;
import android.util.Slog;
import com.android.server.NetworkManagementService;
import com.android.server.SystemService;
import com.android.server.policy.HwPolicyFactory;
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
    /* access modifiers changed from: private */
    public static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    /* access modifiers changed from: private */
    public static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private static final String MAX_BRIGHTNESS_PATH = "/sys/class/leds/lcd_backlight0/max_brightness";
    static final String TAG = "LightsService";
    /* access modifiers changed from: private */
    public static boolean inMirrorLinkBrightnessMode = false;
    /* access modifiers changed from: private */
    public static long mAmountTime = 0;
    protected static boolean mHasShutDown = false;
    /* access modifiers changed from: private */
    public static boolean mIsAutoAdjust = false;
    /* access modifiers changed from: private */
    public static int mLcdBrightness = 100;
    public static int mMaxBrightnessFromKernel = 255;
    /* access modifiers changed from: private */
    public static double mRatio = 1.0d;
    boolean POWER_CURVE_BLIGHT_SUPPORT;
    /* access modifiers changed from: private */
    public boolean mBrightnessConflict;
    /* access modifiers changed from: private */
    public int mCurBrightness = 100;
    /* access modifiers changed from: private */
    public Handler mH;
    protected boolean mIsHighPrecision;
    final LightImpl[] mLights = new LightImpl[LightsManager.LIGHT_ID_COUNT];
    /* access modifiers changed from: private */
    public int mLimitedMaxBrightness;
    private final LightsManager mService;
    private boolean mVrModeEnabled;
    protected boolean mWriteAutoBrightnessDbEnable;

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

        private LightImpl(int id) {
            this.mId = id;
        }

        public void setLcdRatio(int ratio, boolean autoAdjust) {
            boolean unused = LightsService.mIsAutoAdjust = autoAdjust;
            if (ratio > 100 || ratio < 1) {
                double unused2 = LightsService.mRatio = 1.0d;
            } else {
                double unused3 = LightsService.mRatio = ((double) ratio) / 100.0d;
            }
            Slog.i(LightsService.TAG, "setLcdRatio ratio:" + ratio + " autoAdjust:" + autoAdjust);
            if (!LightsService.this.POWER_CURVE_BLIGHT_SUPPORT) {
                setLightGradualChange(LightsService.mLcdBrightness, 0, true);
            }
        }

        public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
            if (ratioMin == ratioMax && ratioMin == -1 && !LightsService.this.POWER_CURVE_BLIGHT_SUPPORT) {
                synchronized (this) {
                    if (autoLimit > 0) {
                        try {
                            int unused = LightsService.this.mLimitedMaxBrightness = autoLimit;
                            if (LightsService.this.mCurBrightness > autoLimit && !LightsService.this.POWER_CURVE_BLIGHT_SUPPORT) {
                                setLightGradualChange(autoLimit, 0, true);
                            }
                        } catch (Throwable th) {
                            throw th;
                        }
                    } else {
                        int unused2 = LightsService.this.mLimitedMaxBrightness = -1;
                        if (LightsService.mLcdBrightness > 0 && !LightsService.this.POWER_CURVE_BLIGHT_SUPPORT) {
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

        public int getMaxBrightnessFromKernel() {
            return LightsService.mMaxBrightnessFromKernel;
        }

        public void updateBrightnessAdjustMode(boolean mode) {
            LightsService.this.updateBrightnessMode(mode);
        }

        public void sendSmartBackLight(int enable, int level, int value) {
            synchronized (this) {
                int value2 = value > 65535 ? 65535 : value;
                int lightValue = (65535 & value2) | ((enable & 1) << 24) | ((level & 255) << 16);
                Flog.i(NetworkManagementService.NetdResponseCode.BandwidthControl, "set smart backlight. enable is " + enable + ",level is " + level + ",value is " + value2 + ",lightValue is " + lightValue);
                LightsService.setLight_native(this.mId, lightValue, 0, 0, 0, 0);
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
                    setLightLocked(-16777216 | (color << 16) | (color << 8) | color, 0, 0, 0, 0);
                }
            }
        }

        public void setMirrorLinkBrightnessStatus(boolean status) {
            Slog.i(LightsService.TAG, "setMirrorLinkBrightnessStatus  status is " + status);
            boolean unused = LightsService.inMirrorLinkBrightnessMode = status;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:65:0x0153, code lost:
            return;
         */
        public void setBrightness(int brightness, int brightnessMode) {
            this.mCurrentBrightness = brightness;
            if (!LightsService.inMirrorLinkBrightnessMode) {
                if (!LightsService.mHasShutDown || !HwPolicyFactory.isHwFastShutdownEnable() || brightness <= 0) {
                    if (this.mId == 0) {
                        boolean unused = LightsService.this.mBrightnessConflict = true;
                        int unused2 = LightsService.mLcdBrightness = brightness;
                        if (LightsService.this.mLimitedMaxBrightness > 0 && brightness > LightsService.this.mLimitedMaxBrightness) {
                            brightness = LightsService.this.mLimitedMaxBrightness;
                        }
                        LightsService.this.sendUpdateaAutoBrightnessDbMsg();
                        if (brightness == 0 || ((!LightsService.mIsAutoAdjust && LightsService.mRatio >= 1.0d) || LightsService.this.POWER_CURVE_BLIGHT_SUPPORT)) {
                            int unused3 = LightsService.this.mCurBrightness = brightness;
                        } else {
                            setLightGradualChange(brightness, brightnessMode, false);
                            return;
                        }
                    }
                    synchronized (this) {
                        if (brightnessMode == 2) {
                            try {
                                Slog.w(LightsService.TAG, "setBrightness with LOW_PERSISTENCE unexpected #" + this.mId + ": brightness=0x" + Integer.toHexString(brightness));
                            } catch (Throwable th) {
                                throw th;
                            }
                        } else {
                            int brightness2 = LightsService.this.mapIntoRealBacklightLevel(brightness);
                            if (LightsService.this.mIsHighPrecision) {
                                int color = 65535 & brightness2;
                                if (this.mId == 2 && LightsService.FRONT_FINGERPRINT_NAVIGATION && LightsService.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                                    int color2 = (color * 255) / LightsService.this.getNormalizedMaxBrightness();
                                    if (color2 == 0 && brightness2 != 0) {
                                        color2 = 1;
                                    }
                                    int color3 = color2 & 255;
                                    Slog.d(LightsService.TAG, "HighPrecision, Set button brihtness:" + color3 + ", bcaklight brightness:" + brightness2);
                                    setLightLocked(color3, 0, 0, 0, brightnessMode);
                                    return;
                                }
                                setLightLocked_10000stage(color, 0, 0, 0, brightnessMode);
                            } else {
                                int color4 = brightness2 & 255;
                                if (this.mId == 2 && LightsService.FRONT_FINGERPRINT_NAVIGATION && LightsService.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                                    Slog.i(LightsService.TAG, "Set button brihtness:" + color4);
                                    setLightLocked(color4, 0, 0, 0, brightnessMode);
                                    return;
                                }
                                setLightLocked(-16777216 | (color4 << 16) | (color4 << 8) | color4, 0, 0, 0, brightnessMode);
                            }
                        }
                    }
                } else {
                    Slog.i(LightsService.TAG, "Ignore brightness " + brightness + " during fast shutdown");
                }
            }
        }

        public int getCurrentBrightness() {
            return this.mCurrentBrightness;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:53:0x0159, code lost:
            r1 = r18;
         */
        private void setLightGradualChange(int brightness, int brightnessMode, boolean isPGset) {
            int amount;
            boolean z;
            int regulateTime;
            int tarBrightness = brightness;
            if (LightsService.mRatio < 1.0d) {
                tarBrightness = Utils.getRatioBright(tarBrightness, LightsService.mRatio);
            }
            if (LightsService.mIsAutoAdjust) {
                tarBrightness = Utils.getAutoAdjustBright(tarBrightness);
            }
            if (!isPGset) {
                if (LightsService.this.mCurBrightness == 0 && tarBrightness > 0) {
                    long unused = LightsService.mAmountTime = SystemClock.elapsedRealtime();
                }
                if (SystemClock.elapsedRealtime() - LightsService.mAmountTime < 1000) {
                    int unused2 = LightsService.this.mCurBrightness = tarBrightness;
                }
            }
            if (LightsService.this.mLimitedMaxBrightness > 0 && tarBrightness > LightsService.this.mLimitedMaxBrightness) {
                tarBrightness = LightsService.this.mLimitedMaxBrightness;
            }
            int tarBrightness2 = tarBrightness;
            int minAmount = (1 + 1) * 39;
            int brightnessGap = (25 + 1) * 39;
            int steps = 20;
            if (LightsService.mRatio < 1.0d) {
                steps = 16;
            }
            int amount2 = Math.abs(LightsService.this.mCurBrightness - tarBrightness2) / steps;
            boolean z2 = false;
            boolean unused3 = LightsService.this.mBrightnessConflict = false;
            synchronized (this) {
                int regulateTime2 = 0;
                int amount3 = amount2;
                while (true) {
                    try {
                        if (!LightsService.this.mBrightnessConflict && regulateTime2 >= 200) {
                            amount3 = minAmount;
                        }
                        if (LightsService.this.mBrightnessConflict) {
                            if (LightsService.mRatio != 1.0d || LightsService.mLcdBrightness - LightsService.this.mCurBrightness <= brightnessGap) {
                                Slog.i(LightsService.TAG, "set brightness confict and break...");
                            } else {
                                amount3 = Math.abs(LightsService.this.mCurBrightness - tarBrightness2) / 5;
                                boolean unused4 = LightsService.this.mBrightnessConflict = z2;
                                Slog.i(LightsService.TAG, "confict and set amount = " + amount3);
                            }
                        }
                        int amount4 = amount3;
                        try {
                            int setValue = Utils.getAnimatedValue(tarBrightness2, LightsService.this.mCurBrightness, amount4);
                            int unused5 = LightsService.this.mCurBrightness = setValue;
                            int setValue2 = LightsService.this.mapIntoRealBacklightLevel(setValue);
                            if (LightsService.this.mIsHighPrecision) {
                                amount = amount4;
                                regulateTime = regulateTime2;
                                z = z2;
                                try {
                                    setLightLocked_10000stage(setValue2 & NetworkConstants.ARP_HWTYPE_RESERVED_HI, 0, 0, 0, brightnessMode);
                                } catch (Throwable th) {
                                    th = th;
                                    int i = regulateTime;
                                    throw th;
                                }
                            } else {
                                amount = amount4;
                                regulateTime = regulateTime2;
                                z = z2;
                                int color = setValue2 & 255;
                                setLightLocked(-16777216 | (color << 16) | (color << 8) | color, 0, 0, 0, brightnessMode);
                            }
                            if (LightsService.mLcdBrightness == 0) {
                                Slog.w(LightsService.TAG, "synchronized conflict...");
                                int i2 = regulateTime;
                                break;
                            }
                            if (LightsService.this.mCurBrightness != tarBrightness2) {
                                SystemClock.sleep(16);
                                regulateTime2 = regulateTime + 16;
                            } else {
                                regulateTime2 = regulateTime;
                            }
                            try {
                                if (LightsService.this.mCurBrightness == tarBrightness2) {
                                    break;
                                }
                                z2 = z;
                                amount3 = amount;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i3 = regulateTime2;
                            int i4 = amount4;
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        int i5 = regulateTime2;
                        throw th;
                    }
                }
                Slog.i(LightsService.TAG, "set brightness confict and break...");
                try {
                    boolean unused6 = LightsService.this.mBrightnessConflict = true;
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
            }
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
                if (this.mColor == 0 && !this.mFlashing) {
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
            synchronized (this) {
                if (this.mVrModeEnabled != enabled) {
                    this.mVrModeEnabled = enabled;
                    this.mUseLowPersistenceForVR = LightsService.this.getVrDisplayMode() == 0;
                    if (shouldBeInLowPersistenceMode()) {
                        this.mLastBrightnessMode = this.mBrightnessMode;
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public void stopFlashing() {
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
            return this.mVrModeEnabled && this.mUseLowPersistenceForVR;
        }

        private void setLightLocked_10000stage(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (!(color == this.mColor && mode == this.mMode && onMS == this.mOnMS && offMS == this.mOffMS)) {
                this.mColor = color;
                this.mMode = mode;
                this.mOnMS = onMS;
                this.mOffMS = offMS;
                Trace.traceBegin(131072, "setLight(" + this.mId + ", " + color + ")");
                if (LightsService.this.isLightsBypassed()) {
                    Slog.i(LightsService.TAG, "getLightsBypass() return true, setLight_native bypassed");
                    return;
                }
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

        public boolean hwBrightnessSetData(String name, Bundle data, int[] result) {
            return LightsService.this.hwBrightnessSetDataImpl(name, data, result);
        }

        public boolean hwBrightnessGetData(String name, Bundle data, int[] result) {
            return LightsService.this.hwBrightnessGetDataImpl(name, data, result);
        }
    }

    private static native void finalize_native();

    protected static native void refreshFrames_native();

    static native void setBackLightMaxLevel_native(int i);

    static native void setHighPrecisionFlag_native(long j, int i);

    static native void setLight_native(int i, int i2, int i3, int i4, int i5, int i6);

    public LightsService(Context context) {
        super(context);
        this.mIsHighPrecision = false;
        boolean z = true;
        this.mWriteAutoBrightnessDbEnable = true;
        this.mLimitedMaxBrightness = -1;
        this.mBrightnessConflict = false;
        this.POWER_CURVE_BLIGHT_SUPPORT = SystemProperties.get("ro.config.blight_power_curve", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS).length() <= 0 ? false : z;
        this.mService = new LightsManager() {
            public Light getLight(int id) {
                if (id < 0 || id >= 265) {
                    return null;
                }
                return LightsService.this.mLights[id];
            }
        };
        this.mH = new Handler() {
            public void handleMessage(Message msg) {
                ((LightImpl) msg.obj).stopFlashing();
            }
        };
        mHasShutDown = false;
        for (int i = 0; i < 265; i++) {
            this.mLights[i] = new LightImpl(i);
        }
        getMaxBrightnessFromKerenl();
        setLight_native(3, 0, 0, 0, 0, 0);
    }

    public void onStart() {
        publishLocalService(LightsManager.class, this.mService);
    }

    public void onBootPhase(int phase) {
    }

    /* access modifiers changed from: private */
    public int getVrDisplayMode() {
        return Settings.Secure.getIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser());
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        finalize_native();
        super.finalize();
    }

    public void getMaxBrightnessFromKerenl() {
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(MAX_BRIGHTNESS_PATH)));
            String readLine = reader2.readLine();
            String tempString = readLine;
            if (readLine != null) {
                mMaxBrightnessFromKernel = Integer.parseInt(tempString);
            }
            try {
                reader2.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e2) {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e3) {
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    public void sendSmartBackLightWithRefreshFramesImpl(int id, int enable, int level, int value, int frames, boolean setAfterRefresh, int enable2, int value2) {
    }

    public void sendSREWithRefreshFramesImpl(int id, int enable, int ambientLightThreshold, int ambientLight, int frames, boolean setAfterRefresh, int enable2, int ambientLight2) {
    }

    /* access modifiers changed from: protected */
    public void sendUpdateaAutoBrightnessDbMsg() {
    }

    /* access modifiers changed from: protected */
    public void updateBrightnessMode(boolean mode) {
    }

    /* access modifiers changed from: protected */
    public int getLcdBrightnessMode() {
        return mLcdBrightness;
    }

    /* access modifiers changed from: protected */
    public int mapIntoRealBacklightLevel(int level) {
        return level;
    }

    /* access modifiers changed from: protected */
    public void updateCurrentUserId(int userId) {
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

    /* access modifiers changed from: protected */
    public int getNormalizedMaxBrightness() {
        return mMaxBrightnessFromKernel;
    }

    public boolean hwBrightnessSetDataImpl(String name, Bundle data, int[] result) {
        return false;
    }

    public boolean hwBrightnessGetDataImpl(String name, Bundle data, int[] result) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isLightsBypassed() {
        return false;
    }
}
