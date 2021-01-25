package android.hardware.display;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.IntArray;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;

public abstract class DisplayManagerInternal {

    public interface DisplayPowerCallbacks {
        void acquireSuspendBlocker();

        void onDisplayStateChange(int i);

        void onProximityNegative();

        void onProximityPositive();

        void onStateChanged();

        void releaseSuspendBlocker();
    }

    public interface DisplayTransactionListener {
        void onDisplayTransaction(SurfaceControl.Transaction transaction);
    }

    public abstract void forceDisplayState(int i, int i2);

    public abstract int getCoverModeBrightnessFromLastScreenBrightness();

    public abstract DisplayInfo getDisplayInfo(int i);

    public abstract int getDisplayMode();

    public abstract IBinder getDisplayToken(int i);

    public abstract DisplayedContentSample getDisplayedContentSample(int i, long j, long j2);

    public abstract DisplayedContentSamplingAttributes getDisplayedContentSamplingAttributes(int i);

    public abstract boolean getHwBrightnessData(String str, Bundle bundle, int[] iArr);

    public abstract int getMaxBrightnessForSeekbar();

    public abstract void getNonOverrideDisplayInfo(int i, DisplayInfo displayInfo);

    public abstract boolean getRebootAutoModeEnable();

    public abstract void initPowerManagement(DisplayPowerCallbacks displayPowerCallbacks, Handler handler, SensorManager sensorManager);

    public abstract boolean isProximitySensorAvailable();

    public abstract void onOverlayChanged();

    public abstract void pcDisplayChange(boolean z);

    public abstract void performTraversal(SurfaceControl.Transaction transaction);

    public abstract void performTraversal(SurfaceControl.Transaction transaction, boolean z);

    public abstract void persistBrightnessTrackerState();

    public abstract void registerDisplayTransactionListener(DisplayTransactionListener displayTransactionListener);

    public abstract boolean registerScreenOnUnBlockerCallback(HwFoldScreenManagerInternal.ScreenOnUnblockerCallback screenOnUnblockerCallback);

    public abstract boolean requestPowerState(DisplayPowerRequest displayPowerRequest, boolean z);

    public abstract void requestScreenState();

    public abstract void resetDisplayDelay();

    public abstract SurfaceControl.ScreenshotGraphicBuffer screenshot(int i);

    public abstract void setAodAlpmState(int i);

    public abstract void setBacklightBrightness(PowerManager.BacklightBrightness backlightBrightness);

    public abstract void setBiometricDetectState(int i);

    public abstract void setBrightnessAnimationTime(boolean z, int i);

    public abstract void setBrightnessNoLimit(int i, int i2);

    public abstract void setCameraModeBrightnessLineEnable(boolean z);

    public abstract void setDisplayAccessUIDs(SparseArray<IntArray> sparseArray);

    public abstract void setDisplayInfoOverrideFromWindowManager(int i, DisplayInfo displayInfo);

    public abstract int setDisplayMode(int i, int i2, boolean z);

    public abstract void setDisplayModeChangeDelay(SurfaceControl.Transaction transaction, long j);

    public abstract void setDisplayOffsets(int i, int i2, int i3);

    public abstract void setDisplayProperties(int i, boolean z, float f, int i2, boolean z2);

    public abstract void setDisplayScalingDisabled(int i, boolean z);

    public abstract boolean setDisplayedContentSamplingEnabled(int i, boolean z, int i2, int i3);

    public abstract boolean setHwBrightnessData(String str, Bundle bundle, int[] iArr);

    public abstract void setHwWifiDisplayParameters(HwWifiDisplayParameters hwWifiDisplayParameters);

    public abstract void setKeyguardLockedStatus(boolean z);

    public abstract void setMaxBrightnessFromThermal(int i);

    public abstract void setModeToAutoNoClearOffsetEnable(boolean z);

    public abstract void setPoweroffModeChangeAutoEnable(boolean z);

    public abstract int setScreenBrightnessMappingtoIndoorMax(int i);

    public abstract void setTemporaryScreenBrightnessSettingOverride(int i);

    public abstract void startDawnAnimation();

    public abstract void unregisterDisplayTransactionListener(DisplayTransactionListener displayTransactionListener);

    public abstract void updateAutoBrightnessAdjustFactor(float f);

    public abstract void updateCutoutInfoForRog(int i);

    public abstract void wakeupDisplayModeChange(boolean z);

    public static final class DisplayPowerRequest {
        public static final int POLICY_BRIGHT = 3;
        public static final int POLICY_DIM = 2;
        public static final int POLICY_DOZE = 1;
        public static final int POLICY_OFF = 0;
        public static final int POLICY_VR = 4;
        public boolean blockScreenOn;
        public boolean boostScreenBrightness;
        public boolean brightnessWaitMode;
        public boolean brightnessWaitRet;
        public int dozeScreenBrightness;
        public int dozeScreenState;
        public boolean lowPowerMode;
        public int mScreenChangeReason;
        public int policy;
        public int screenAutoBrightness;
        public float screenAutoBrightnessAdjustmentOverride;
        public int screenBrightnessOverride;
        public float screenLowPowerBrightnessFactor;
        public boolean useAutoBrightness;
        public boolean useProximitySensor;
        public boolean useProximitySensorbyPhone;
        public boolean useSmartBacklight;
        public int userId;

        public DisplayPowerRequest() {
            this.mScreenChangeReason = 0;
            this.policy = 3;
            this.useProximitySensor = false;
            this.screenBrightnessOverride = -1;
            this.useProximitySensorbyPhone = false;
            this.useAutoBrightness = false;
            this.screenAutoBrightnessAdjustmentOverride = Float.NaN;
            this.screenLowPowerBrightnessFactor = 0.5f;
            this.blockScreenOn = false;
            this.dozeScreenBrightness = -1;
            this.dozeScreenState = 0;
            this.brightnessWaitMode = false;
            this.brightnessWaitRet = false;
            this.useSmartBacklight = false;
            this.screenAutoBrightness = 0;
            this.userId = 0;
        }

        public DisplayPowerRequest(DisplayPowerRequest other) {
            this.mScreenChangeReason = 0;
            copyFrom(other);
        }

        public boolean isBrightOrDim() {
            int i = this.policy;
            return i == 3 || i == 2;
        }

        public boolean isVr() {
            return this.policy == 4;
        }

        public void copyFrom(DisplayPowerRequest other) {
            this.policy = other.policy;
            this.useProximitySensor = other.useProximitySensor;
            this.screenBrightnessOverride = other.screenBrightnessOverride;
            this.useProximitySensorbyPhone = other.useProximitySensorbyPhone;
            this.useAutoBrightness = other.useAutoBrightness;
            this.screenAutoBrightnessAdjustmentOverride = other.screenAutoBrightnessAdjustmentOverride;
            this.screenLowPowerBrightnessFactor = other.screenLowPowerBrightnessFactor;
            this.blockScreenOn = other.blockScreenOn;
            this.lowPowerMode = other.lowPowerMode;
            this.boostScreenBrightness = other.boostScreenBrightness;
            this.dozeScreenBrightness = other.dozeScreenBrightness;
            this.dozeScreenState = other.dozeScreenState;
            this.brightnessWaitMode = other.brightnessWaitMode;
            this.brightnessWaitRet = other.brightnessWaitRet;
            this.useSmartBacklight = other.useSmartBacklight;
            this.screenAutoBrightness = other.screenAutoBrightness;
            this.userId = other.userId;
            this.mScreenChangeReason = other.mScreenChangeReason;
        }

        public boolean equals(Object o) {
            return (o instanceof DisplayPowerRequest) && equals((DisplayPowerRequest) o);
        }

        public boolean equals(DisplayPowerRequest other) {
            return other != null && this.policy == other.policy && this.useProximitySensor == other.useProximitySensor && this.useProximitySensorbyPhone == other.useProximitySensorbyPhone && this.screenBrightnessOverride == other.screenBrightnessOverride && this.useAutoBrightness == other.useAutoBrightness && floatEquals(this.screenAutoBrightnessAdjustmentOverride, other.screenAutoBrightnessAdjustmentOverride) && this.screenLowPowerBrightnessFactor == other.screenLowPowerBrightnessFactor && this.blockScreenOn == other.blockScreenOn && this.lowPowerMode == other.lowPowerMode && this.boostScreenBrightness == other.boostScreenBrightness && this.dozeScreenBrightness == other.dozeScreenBrightness && this.dozeScreenState == other.dozeScreenState && this.useSmartBacklight == other.useSmartBacklight && this.brightnessWaitMode == other.brightnessWaitMode && this.brightnessWaitRet == other.brightnessWaitRet && this.screenAutoBrightness == other.screenAutoBrightness && this.userId == other.userId && this.mScreenChangeReason == other.mScreenChangeReason;
        }

        private boolean floatEquals(float f1, float f2) {
            return f1 == f2 || (Float.isNaN(f1) && Float.isNaN(f2));
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            return "policy=" + policyToString(this.policy) + ", useProximitySensor=" + this.useProximitySensor + ", screenBrightnessOverride=" + this.screenBrightnessOverride + ", useProximitySensorbyPhone=" + this.useProximitySensorbyPhone + ", useAutoBrightness=" + this.useAutoBrightness + ", screenAutoBrightnessAdjustmentOverride=" + this.screenAutoBrightnessAdjustmentOverride + ", screenLowPowerBrightnessFactor=" + this.screenLowPowerBrightnessFactor + ", blockScreenOn=" + this.blockScreenOn + ", lowPowerMode=" + this.lowPowerMode + ", boostScreenBrightness=" + this.boostScreenBrightness + ", dozeScreenBrightness=" + this.dozeScreenBrightness + ", dozeScreenState=" + Display.stateToString(this.dozeScreenState) + ", useSmartBacklight=" + this.useSmartBacklight + ", brightnessWaitMode=" + this.brightnessWaitMode + ", brightnessWaitRet=" + this.brightnessWaitRet + ", screenAutoBrightness=" + this.screenAutoBrightness + ", mScreenChangedReason=" + this.mScreenChangeReason + ", userId=" + this.userId;
        }

        public static String policyToString(int policy2) {
            if (policy2 == 0) {
                return "OFF";
            }
            if (policy2 == 1) {
                return "DOZE";
            }
            if (policy2 == 2) {
                return "DIM";
            }
            if (policy2 == 3) {
                return "BRIGHT";
            }
            if (policy2 != 4) {
                return Integer.toString(policy2);
            }
            return "VR";
        }
    }
}
