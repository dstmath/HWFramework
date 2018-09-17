package android.hardware.display;

import android.hardware.SensorManager;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager.BacklightBrightness;
import android.util.IntArray;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;

public abstract class DisplayManagerInternal {

    public interface DisplayPowerCallbacks {
        void acquireSuspendBlocker();

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
        public boolean brightnessSetByUser;
        public boolean brightnessWaitMode;
        public boolean brightnessWaitRet;
        public int dozeScreenBrightness;
        public int dozeScreenState;
        public boolean lowPowerMode;
        public int policy;
        public int screenAutoBrightness;
        public float screenAutoBrightnessAdjustment;
        public int screenBrightness;
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
            this.useProximitySensorbyPhone = false;
            this.screenBrightness = 255;
            this.screenAutoBrightnessAdjustment = TonemapCurve.LEVEL_BLACK;
            this.screenLowPowerBrightnessFactor = 0.5f;
            this.useAutoBrightness = false;
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
            this.useProximitySensorbyPhone = other.useProximitySensorbyPhone;
            this.screenBrightness = other.screenBrightness;
            this.screenAutoBrightnessAdjustment = other.screenAutoBrightnessAdjustment;
            this.screenLowPowerBrightnessFactor = other.screenLowPowerBrightnessFactor;
            this.brightnessSetByUser = other.brightnessSetByUser;
            this.useAutoBrightness = other.useAutoBrightness;
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
            if (o instanceof DisplayPowerRequest) {
                return equals((DisplayPowerRequest) o);
            }
            return false;
        }

        public boolean equals(DisplayPowerRequest other) {
            return other != null && this.policy == other.policy && this.useProximitySensor == other.useProximitySensor && this.useProximitySensorbyPhone == other.useProximitySensorbyPhone && this.screenBrightness == other.screenBrightness && this.screenAutoBrightnessAdjustment == other.screenAutoBrightnessAdjustment && this.screenLowPowerBrightnessFactor == other.screenLowPowerBrightnessFactor && this.brightnessSetByUser == other.brightnessSetByUser && this.useAutoBrightness == other.useAutoBrightness && this.blockScreenOn == other.blockScreenOn && this.lowPowerMode == other.lowPowerMode && this.boostScreenBrightness == other.boostScreenBrightness && this.dozeScreenBrightness == other.dozeScreenBrightness && this.dozeScreenState == other.dozeScreenState && this.useSmartBacklight == other.useSmartBacklight && this.brightnessWaitMode == other.brightnessWaitMode && this.brightnessWaitRet == other.brightnessWaitRet && this.skipWaitKeyguardDismiss == other.skipWaitKeyguardDismiss && this.screenAutoBrightness == other.screenAutoBrightness && this.userId == other.userId;
        }

        public int hashCode() {
            return 0;
        }

        public String toString() {
            return "policy=" + policyToString(this.policy) + ", useProximitySensor=" + this.useProximitySensor + ", useProximitySensorbyPhone=" + this.useProximitySensorbyPhone + ", screenBrightness=" + this.screenBrightness + ", screenAutoBrightnessAdjustment=" + this.screenAutoBrightnessAdjustment + ", screenLowPowerBrightnessFactor=" + this.screenLowPowerBrightnessFactor + ", brightnessSetByUser=" + this.brightnessSetByUser + ", useAutoBrightness=" + this.useAutoBrightness + ", blockScreenOn=" + this.blockScreenOn + ", lowPowerMode=" + this.lowPowerMode + ", boostScreenBrightness=" + this.boostScreenBrightness + ", dozeScreenBrightness=" + this.dozeScreenBrightness + ", dozeScreenState=" + Display.stateToString(this.dozeScreenState) + ", useSmartBacklight=" + this.useSmartBacklight + ", brightnessWaitMode=" + this.brightnessWaitMode + ", brightnessWaitRet=" + this.brightnessWaitRet + ", skipWaitKeyguardDismiss=" + this.skipWaitKeyguardDismiss + ", screenAutoBrightness=" + this.screenAutoBrightness + ", userId=" + this.userId;
        }

        public static String policyToString(int policy) {
            switch (policy) {
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
                    return Integer.toString(policy);
            }
        }
    }

    public interface DisplayTransactionListener {
        void onDisplayTransaction();
    }

    public abstract void forceDisplayState(int i, int i2);

    public abstract int getCoverModeBrightnessFromLastScreenBrightness();

    public abstract DisplayInfo getDisplayInfo(int i);

    public abstract IBinder getDisplayToken(int i);

    public abstract int getMaxBrightnessForSeekbar();

    public abstract void getNonOverrideDisplayInfo(int i, DisplayInfo displayInfo);

    public abstract boolean getRebootAutoModeEnable();

    public abstract void initPowerManagement(DisplayPowerCallbacks displayPowerCallbacks, Handler handler, SensorManager sensorManager);

    public abstract boolean isProximitySensorAvailable();

    public abstract boolean isUidPresentOnDisplay(int i, int i2);

    public abstract void pcDisplayChange(boolean z);

    public abstract void performTraversalInTransactionFromWindowManager();

    public abstract void registerDisplayTransactionListener(DisplayTransactionListener displayTransactionListener);

    public abstract boolean requestPowerState(DisplayPowerRequest displayPowerRequest, boolean z);

    public abstract void setAodAlpmState(int i);

    public abstract void setBacklightBrightness(BacklightBrightness backlightBrightness);

    public abstract void setBrightnessAnimationTime(boolean z, int i);

    public abstract void setCameraModeBrightnessLineEnable(boolean z);

    public abstract void setDisplayAccessUIDs(SparseArray<IntArray> sparseArray);

    public abstract void setDisplayInfoOverrideFromWindowManager(int i, DisplayInfo displayInfo);

    public abstract void setDisplayOffsets(int i, int i2, int i3);

    public abstract void setDisplayProperties(int i, boolean z, float f, int i2, boolean z2);

    public abstract void setKeyguardLockedStatus(boolean z);

    public abstract void setMaxBrightnessFromThermal(int i);

    public abstract void setModeToAutoNoClearOffsetEnable(boolean z);

    public abstract void setPoweroffModeChangeAutoEnable(boolean z);

    public abstract int setScreenBrightnessMappingtoIndoorMax(int i);

    public abstract void unregisterDisplayTransactionListener(DisplayTransactionListener displayTransactionListener);

    public abstract void updateAutoBrightnessAdjustFactor(float f);
}
