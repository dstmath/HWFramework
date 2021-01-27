package com.android.server.lights;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings;
import android.util.Flog;
import android.util.Slog;
import android.view.SurfaceControl;

public class LightsService extends AbsLightsService {
    static final boolean DEBUG = false;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    static final String TAG = "LightsService";
    private Handler mH = new Handler() {
        /* class com.android.server.lights.LightsService.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            ((LightImpl) msg.obj).stopFlashing();
        }
    };
    protected boolean mIsHighPrecision = false;
    protected int mLcdBrightness = 100;
    final LightImpl[] mLights = new LightImpl[LightsManager.LIGHT_ID_COUNT];
    private final LightsManager mService = new LightsManager() {
        /* class com.android.server.lights.LightsService.AnonymousClass1 */

        @Override // com.android.server.lights.LightsManager
        public Light getLight(int id) {
            if (id < 0 || id >= 265) {
                return null;
            }
            return LightsService.this.mLights[id];
        }
    };
    protected boolean mWriteAutoBrightnessDbEnable = true;

    static native void setLight_native(int i, int i2, int i3, int i4, int i5, int i6);

    private final class LightImpl extends Light {
        private int mBrightnessMode;
        private int mColor;
        private int mCurrentBrightness;
        private final IBinder mDisplayToken;
        private boolean mFlashing;
        private int mId;
        private boolean mInitialized;
        private int mLastBrightnessMode;
        private int mLastColor;
        private int mMode;
        private int mOffMS;
        private int mOnMS;
        private final int mSurfaceControlMaximumBrightness;
        private boolean mUseLowPersistenceForVR;
        private boolean mVrModeEnabled;

        private LightImpl(Context context, int id) {
            PowerManager pm;
            this.mId = id;
            this.mDisplayToken = SurfaceControl.getInternalDisplayToken();
            int maximumBrightness = 0;
            if (SurfaceControl.getDisplayBrightnessSupport(this.mDisplayToken) && (pm = (PowerManager) context.getSystemService(PowerManager.class)) != null) {
                maximumBrightness = pm.getMaximumScreenBrightnessSetting();
            }
            this.mSurfaceControlMaximumBrightness = maximumBrightness;
        }

        @Override // com.android.server.lights.Light
        public void sendSmartBackLightWithRefreshFrames(int enable, int level, int value, int frames, boolean setAfterRefresh, int enable2, int value2) {
            LightsService.this.sendSmartBackLightWithRefreshFramesImpl(this.mId, enable, level, value, frames, setAfterRefresh, enable2, value2);
        }

        @Override // com.android.server.lights.Light
        public void writeAutoBrightnessDbEnable(boolean enable) {
            LightsService lightsService = LightsService.this;
            lightsService.mWriteAutoBrightnessDbEnable = enable;
            if (enable) {
                lightsService.sendUpdateaAutoBrightnessDbMsg();
            }
        }

        @Override // com.android.server.lights.Light
        public void updateUserId(int userId) {
            LightsService.this.updateCurrentUserId(userId);
        }

        @Override // com.android.server.lights.Light
        public void updateBrightnessAdjustMode(boolean mode) {
            LightsService.this.updateBrightnessMode(mode);
        }

        @Override // com.android.server.lights.Light
        public void sendSmartBackLight(int enable, int level, int value) {
            synchronized (this) {
                int value2 = value > 65535 ? 65535 : value;
                int lightValue = (65535 & value2) | ((enable & 1) << 24) | ((level & 255) << 16);
                Flog.i(601, "set smart backlight. enable is " + enable + ",level is " + level + ",value is " + value2 + ",lightValue is " + lightValue);
                LightsService.setLight_native(this.mId, lightValue, 0, 0, 0, 0);
            }
        }

        @Override // com.android.server.lights.Light
        public void setBrightness(int brightness) {
            setBrightness(brightness, 0);
        }

        @Override // com.android.server.lights.Light
        public void setBrightness(int brightness, int brightnessMode) {
            if (!LightsService.this.shouldIgnoreSetBrightness(brightness, brightnessMode)) {
                if (this.mId == 0) {
                    LightsService lightsService = LightsService.this;
                    lightsService.mLcdBrightness = brightness;
                    lightsService.sendUpdateaAutoBrightnessDbMsg();
                }
                synchronized (this) {
                    if (brightnessMode == 2) {
                        Slog.w(LightsService.TAG, "setBrightness with LOW_PERSISTENCE unexpected #" + this.mId + ": brightness=0x" + Integer.toHexString(brightness));
                        return;
                    }
                    this.mCurrentBrightness = brightness;
                    int brightness2 = LightsService.this.mapIntoRealBacklightLevel(brightness);
                    if (!isSetButtonLights(brightness2, brightnessMode, LightsService.this.mIsHighPrecision)) {
                        if (brightnessMode == 0 && !shouldBeInLowPersistenceMode() && this.mSurfaceControlMaximumBrightness == 255 && !LightsService.this.mIsHighPrecision) {
                            SurfaceControl.setDisplayBrightness(this.mDisplayToken, ((float) brightness2) / ((float) this.mSurfaceControlMaximumBrightness));
                        } else if (LightsService.this.mIsHighPrecision) {
                            setLightLocked_10000stage(65535 & brightness2, 0, 0, 0, brightnessMode);
                        } else {
                            int color = brightness2 & 255;
                            setLightLocked(color | -16777216 | (color << 16) | (color << 8), 0, 0, 0, brightnessMode);
                        }
                    }
                }
            }
        }

        @Override // com.android.server.lights.Light
        public void setColor(int color) {
            synchronized (this) {
                setLightLocked(color, 0, 0, 0, 0);
            }
        }

        @Override // com.android.server.lights.Light
        public void setFlashing(int color, int mode, int onMS, int offMS) {
            synchronized (this) {
                setLightLocked(color, mode, onMS, offMS, 0);
            }
        }

        @Override // com.android.server.lights.Light
        public void pulse() {
            pulse(16777215, 7);
        }

        @Override // com.android.server.lights.Light
        public void pulse(int color, int onMS) {
            synchronized (this) {
                if (this.mColor == 0 && !this.mFlashing) {
                    setLightLocked(color, 2, onMS, 1000, 0);
                    this.mColor = 0;
                    LightsService.this.mH.sendMessageDelayed(Message.obtain(LightsService.this.mH, 1, this), (long) onMS);
                }
            }
        }

        @Override // com.android.server.lights.Light
        public void turnOff() {
            synchronized (this) {
                setLightLocked(0, 0, 0, 0, 0);
            }
        }

        @Override // com.android.server.lights.Light
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
        /* access modifiers changed from: public */
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
            return this.mVrModeEnabled && this.mUseLowPersistenceForVR;
        }

        private void setLightLocked_10000stage(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (color != this.mColor || mode != this.mMode || onMS != this.mOnMS || offMS != this.mOffMS) {
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

        @Override // com.android.server.lights.Light
        public int getDeviceActualBrightnessLevel() {
            return LightsService.this.getDeviceActualBrightnessLevelImpl();
        }

        @Override // com.android.server.lights.Light
        public int getDeviceActualBrightnessNit() {
            return LightsService.this.getDeviceActualBrightnessNitImpl();
        }

        @Override // com.android.server.lights.Light
        public int getDeviceStandardBrightnessNit() {
            return LightsService.this.getDeviceStandardBrightnessNitImpl();
        }

        @Override // com.android.server.lights.Light
        public boolean setHwBrightnessData(String name, Bundle data, int[] result) {
            return LightsService.this.setHwBrightnessDataImpl(name, data, result);
        }

        @Override // com.android.server.lights.Light
        public boolean getHwBrightnessData(String name, Bundle data, int[] result) {
            return LightsService.this.getHwBrightnessDataImpl(name, data, result);
        }

        @Override // com.android.server.lights.Light
        public void setMirrorLinkBrightness(int target) {
            synchronized (this) {
                Slog.i(LightsService.TAG, "setMirrorLinkBrightnessStatus  brightness is " + target);
                int brightness = LightsService.this.mapIntoRealBacklightLevel((target * 10000) / 255);
                if (LightsService.this.mIsHighPrecision) {
                    setLightLocked_10000stage(brightness & 65535, 0, 0, 0, 0);
                } else {
                    int color = brightness & 255;
                    setLightLocked(-16777216 | (color << 16) | (color << 8) | color, 0, 0, 0, 0);
                }
            }
        }

        @Override // com.android.server.lights.Light
        public void setMirrorLinkBrightnessStatus(boolean status) {
            LightsService.this.setMirrorLinkBrightnessStatusInternal(status);
        }

        @Override // com.android.server.lights.Light
        public int getCurrentBrightness() {
            return this.mCurrentBrightness;
        }

        private boolean isSetButtonLights(int brightness, int brightnessMode, boolean isHighPrecision) {
            int color;
            if (this.mId != 2 || !LightsService.FRONT_FINGERPRINT_NAVIGATION || LightsService.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1) {
                return false;
            }
            if (isHighPrecision) {
                int color2 = ((brightness & 65535) * 255) / LightsService.this.getNormalizedMaxBrightness();
                if (color2 == 0 && brightness != 0) {
                    color2 = 1;
                }
                color = color2 & 255;
            } else {
                color = brightness & 255;
            }
            Slog.d(LightsService.TAG, "Set button brihtness:" + color + ", bcaklight:" + brightness + ", high:" + isHighPrecision);
            setLightLocked(color, 0, 0, 0, brightnessMode);
            return true;
        }
    }

    public LightsService(Context context) {
        super(context);
        for (int i = 0; i < 265; i++) {
            this.mLights[i] = new LightImpl(context, i);
        }
        setLight_native(3, 0, 0, 0, 0, 0);
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        publishLocalService(LightsManager.class, this.mService);
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getVrDisplayMode() {
        return Settings.Secure.getIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser());
    }
}
