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

public abstract class DisplayManagerInternal {

    public interface DisplayPowerCallbacks {
        void acquireSuspendBlocker();

        void hwBrightnessOnStateChanged(String str, int i, int i2, Bundle bundle);

        void onDisplayStateChange(int i);

        void onProximityNegative();

        void onProximityPositive();

        void onStateChanged();

        void releaseSuspendBlocker();
    }

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
        public int policy;
        public int screenAutoBrightness;
        public float screenAutoBrightnessAdjustmentOverride;
        public int screenBrightnessOverride;
        public float screenLowPowerBrightnessFactor;
        public boolean skipWaitKeyguardDismiss;
        public boolean useAutoBrightness;
        public boolean useProximitySensor;
        public boolean useProximitySensorbyPhone;
        public boolean useSmartBacklight;
        public int userId;

        public DisplayPowerRequest() {
            this.policy = 3;
            this.useProximitySensor = false;
            this.screenBrightnessOverride = -1;
            this.useProximitySensorbyPhone = false;
            this.useAutoBrightness = false;
            this.screenAutoBrightnessAdjustmentOverride = Float.NaN;
            this.screenLowPowerBrightnessFactor = 0.5f;
            this.blockScreenOn = false;
            this.brightnessWaitMode = false;
            this.brightnessWaitRet = false;
            this.skipWaitKeyguardDismiss = false;
            this.dozeScreenBrightness = -1;
            this.dozeScreenState = 0;
            this.useSmartBacklight = false;
            this.screenAutoBrightness = 0;
            this.userId = 0;
        }

        public DisplayPowerRequest(DisplayPowerRequest other) {
            copyFrom(other);
        }

        public boolean isBrightOrDim() {
            return this.policy == 3 || this.policy == 2;
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
            this.brightnessWaitMode = other.brightnessWaitMode;
            this.brightnessWaitRet = other.brightnessWaitRet;
            this.skipWaitKeyguardDismiss = other.skipWaitKeyguardDismiss;
            this.dozeScreenState = other.dozeScreenState;
            this.useSmartBacklight = other.useSmartBacklight;
            this.screenAutoBrightness = other.screenAutoBrightness;
            this.userId = other.userId;
        }

        public boolean equals(Object o) {
            return (o instanceof DisplayPowerRequest) && equals((DisplayPowerRequest) o);
        }

        public boolean equals(DisplayPowerRequest other) {
            return other != null && this.policy == other.policy && this.useProximitySensor == other.useProximitySensor && this.useProximitySensorbyPhone == other.useProximitySensorbyPhone && this.screenBrightnessOverride == other.screenBrightnessOverride && this.useAutoBrightness == other.useAutoBrightness && floatEquals(this.screenAutoBrightnessAdjustmentOverride, other.screenAutoBrightnessAdjustmentOverride) && this.screenLowPowerBrightnessFactor == other.screenLowPowerBrightnessFactor && this.blockScreenOn == other.blockScreenOn && this.lowPowerMode == other.lowPowerMode && this.boostScreenBrightness == other.boostScreenBrightness && this.dozeScreenBrightness == other.dozeScreenBrightness && this.dozeScreenState == other.dozeScreenState && this.useSmartBacklight == other.useSmartBacklight && this.brightnessWaitMode == other.brightnessWaitMode && this.brightnessWaitRet == other.brightnessWaitRet && this.skipWaitKeyguardDismiss == other.skipWaitKeyguardDismiss && this.screenAutoBrightness == other.screenAutoBrightness && this.userId == other.userId;
        }

        private boolean floatEquals(float f1, float f2) {
            return f1 == f2 || (Float.isNaN(f1) && Float.isNaN(f2));
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            return "policy=" + policyToString(this.policy) + ", useProximitySensor=" + this.useProximitySensor + ", screenBrightnessOverride=" + this.screenBrightnessOverride + ", useProximitySensorbyPhone=" + this.useProximitySensorbyPhone + ", useAutoBrightness=" + this.useAutoBrightness + ", screenAutoBrightnessAdjustmentOverride=" + this.screenAutoBrightnessAdjustmentOverride + ", screenLowPowerBrightnessFactor=" + this.screenLowPowerBrightnessFactor + ", blockScreenOn=" + this.blockScreenOn + ", lowPowerMode=" + this.lowPowerMode + ", boostScreenBrightness=" + this.boostScreenBrightness + ", dozeScreenBrightness=" + this.dozeScreenBrightness + ", dozeScreenState=" + Display.stateToString(this.dozeScreenState) + ", useSmartBacklight=" + this.useSmartBacklight + ", brightnessWaitMode=" + this.brightnessWaitMode + ", brightnessWaitRet=" + this.brightnessWaitRet + ", skipWaitKeyguardDismiss=" + this.skipWaitKeyguardDismiss + ", screenAutoBrightness=" + this.screenAutoBrightness + ", userId=" + this.userId;
        }

        public static String policyToString(int policy2) {
            switch (policy2) {
                case 0:
                    return "OFF";
                case 1:
                    return "DOZE";
                case 2:
                    return "DIM";
                case 3:
                    return "BRIGHT";
                case 4:
                    return "VR";
                default:
                    return Integer.toString(policy2);
            }
        }
    }

    public interface DisplayTransactionListener {
        void onDisplayTransaction();
    }

    public abstract void forceDisplayState(int i, int i2);

    public abstract int getCoverModeBrightnessFromLastScreenBrightness();

    public abstract DisplayInfo getDisplayInfo(int i);

    public abstract int getDisplayMode();

    public abstract IBinder getDisplayToken(int i);

    public abstract int getMaxBrightnessForSeekbar();

    public abstract void getNonOverrideDisplayInfo(int i, DisplayInfo displayInfo);

    public abstract boolean getRebootAutoModeEnable();

    public abstract boolean hwBrightnessGetData(String str, Bundle bundle, int[] iArr);

    public abstract boolean hwBrightnessSetData(String str, Bundle bundle, int[] iArr);

    public abstract void initPowerManagement(DisplayPowerCallbacks displayPowerCallbacks, Handler handler, SensorManager sensorManager);

    public abstract boolean isProximitySensorAvailable();

    public abstract boolean isSystemSuspending();

    public abstract boolean isUidPresentOnDisplay(int i, int i2);

    public abstract void onOverlayChanged();

    public abstract void pcDisplayChange(boolean z);

    public abstract void performTraversal(SurfaceControl.Transaction transaction);

    public abstract void persistBrightnessTrackerState();

    public abstract void registerDisplayTransactionListener(DisplayTransactionListener displayTransactionListener);

    public abstract boolean requestPowerState(DisplayPowerRequest displayPowerRequest, boolean z);

    public abstract void setAodAlpmState(int i);

    public abstract void setBacklightBrightness(PowerManager.BacklightBrightness backlightBrightness);

    public abstract void setBrightnessAnimationTime(boolean z, int i);

    public abstract void setBrightnessNoLimit(int i, int i2);

    public abstract void setCameraModeBrightnessLineEnable(boolean z);

    public abstract void setDisplayAccessUIDs(SparseArray<IntArray> sparseArray);

    public abstract void setDisplayInfoOverrideFromWindowManager(int i, DisplayInfo displayInfo);

    public abstract int setDisplayMode(int i);

    public abstract void setDisplayOffsets(int i, int i2, int i3);

    public abstract void setDisplayProperties(int i, boolean z, float f, int i2, boolean z2);

    public abstract void setKeyguardLockedStatus(boolean z);

    public abstract void setMaxBrightnessFromThermal(int i);

    public abstract void setModeToAutoNoClearOffsetEnable(boolean z);

    public abstract void setPoweroffModeChangeAutoEnable(boolean z);

    public abstract int setScreenBrightnessMappingtoIndoorMax(int i);

    public abstract void setTemporaryScreenBrightnessSettingOverride(int i);

    public abstract void suspendSystem(boolean z, boolean z2);

    public abstract void unregisterDisplayTransactionListener(DisplayTransactionListener displayTransactionListener);

    public abstract void updateAutoBrightnessAdjustFactor(float f);

    public abstract void updateCutoutInfoForRog(int i);
}
