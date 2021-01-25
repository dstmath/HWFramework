package android.os;

import android.annotation.SystemApi;
import android.os.Parcelable;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@SystemApi
public final class BatterySaverPolicyConfig implements Parcelable {
    public static final Parcelable.Creator<BatterySaverPolicyConfig> CREATOR = new Parcelable.Creator<BatterySaverPolicyConfig>() {
        /* class android.os.BatterySaverPolicyConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BatterySaverPolicyConfig createFromParcel(Parcel in) {
            return new BatterySaverPolicyConfig(in);
        }

        @Override // android.os.Parcelable.Creator
        public BatterySaverPolicyConfig[] newArray(int size) {
            return new BatterySaverPolicyConfig[size];
        }
    };
    private final float mAdjustBrightnessFactor;
    private final boolean mAdvertiseIsEnabled;
    private final boolean mDeferFullBackup;
    private final boolean mDeferKeyValueBackup;
    private final Map<String, String> mDeviceSpecificSettings;
    private final boolean mDisableAnimation;
    private final boolean mDisableAod;
    private final boolean mDisableLaunchBoost;
    private final boolean mDisableOptionalSensors;
    private final boolean mDisableSoundTrigger;
    private final boolean mDisableVibration;
    private final boolean mEnableAdjustBrightness;
    private final boolean mEnableDataSaver;
    private final boolean mEnableFirewall;
    private final boolean mEnableNightMode;
    private final boolean mEnableQuickDoze;
    private final boolean mForceAllAppsStandby;
    private final boolean mForceBackgroundCheck;
    private final int mLocationMode;

    private BatterySaverPolicyConfig(Builder in) {
        this.mAdjustBrightnessFactor = Math.max(0.0f, Math.min(in.mAdjustBrightnessFactor, 1.0f));
        this.mAdvertiseIsEnabled = in.mAdvertiseIsEnabled;
        this.mDeferFullBackup = in.mDeferFullBackup;
        this.mDeferKeyValueBackup = in.mDeferKeyValueBackup;
        this.mDeviceSpecificSettings = Collections.unmodifiableMap(new ArrayMap(in.mDeviceSpecificSettings));
        this.mDisableAnimation = in.mDisableAnimation;
        this.mDisableAod = in.mDisableAod;
        this.mDisableLaunchBoost = in.mDisableLaunchBoost;
        this.mDisableOptionalSensors = in.mDisableOptionalSensors;
        this.mDisableSoundTrigger = in.mDisableSoundTrigger;
        this.mDisableVibration = in.mDisableVibration;
        this.mEnableAdjustBrightness = in.mEnableAdjustBrightness;
        this.mEnableDataSaver = in.mEnableDataSaver;
        this.mEnableFirewall = in.mEnableFirewall;
        this.mEnableNightMode = in.mEnableNightMode;
        this.mEnableQuickDoze = in.mEnableQuickDoze;
        this.mForceAllAppsStandby = in.mForceAllAppsStandby;
        this.mForceBackgroundCheck = in.mForceBackgroundCheck;
        this.mLocationMode = Math.max(0, Math.min(in.mLocationMode, 4));
    }

    private BatterySaverPolicyConfig(Parcel in) {
        this.mAdjustBrightnessFactor = Math.max(0.0f, Math.min(in.readFloat(), 1.0f));
        this.mAdvertiseIsEnabled = in.readBoolean();
        this.mDeferFullBackup = in.readBoolean();
        this.mDeferKeyValueBackup = in.readBoolean();
        int size = in.readInt();
        Map<String, String> deviceSpecificSettings = new ArrayMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = TextUtils.emptyIfNull(in.readString());
            String val = TextUtils.emptyIfNull(in.readString());
            if (!key.trim().isEmpty()) {
                deviceSpecificSettings.put(key, val);
            }
        }
        this.mDeviceSpecificSettings = Collections.unmodifiableMap(deviceSpecificSettings);
        this.mDisableAnimation = in.readBoolean();
        this.mDisableAod = in.readBoolean();
        this.mDisableLaunchBoost = in.readBoolean();
        this.mDisableOptionalSensors = in.readBoolean();
        this.mDisableSoundTrigger = in.readBoolean();
        this.mDisableVibration = in.readBoolean();
        this.mEnableAdjustBrightness = in.readBoolean();
        this.mEnableDataSaver = in.readBoolean();
        this.mEnableFirewall = in.readBoolean();
        this.mEnableNightMode = in.readBoolean();
        this.mEnableQuickDoze = in.readBoolean();
        this.mForceAllAppsStandby = in.readBoolean();
        this.mForceBackgroundCheck = in.readBoolean();
        this.mLocationMode = Math.max(0, Math.min(in.readInt(), 4));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.mAdjustBrightnessFactor);
        dest.writeBoolean(this.mAdvertiseIsEnabled);
        dest.writeBoolean(this.mDeferFullBackup);
        dest.writeBoolean(this.mDeferKeyValueBackup);
        Set<Map.Entry<String, String>> entries = this.mDeviceSpecificSettings.entrySet();
        dest.writeInt(entries.size());
        for (Map.Entry<String, String> entry : entries) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeBoolean(this.mDisableAnimation);
        dest.writeBoolean(this.mDisableAod);
        dest.writeBoolean(this.mDisableLaunchBoost);
        dest.writeBoolean(this.mDisableOptionalSensors);
        dest.writeBoolean(this.mDisableSoundTrigger);
        dest.writeBoolean(this.mDisableVibration);
        dest.writeBoolean(this.mEnableAdjustBrightness);
        dest.writeBoolean(this.mEnableDataSaver);
        dest.writeBoolean(this.mEnableFirewall);
        dest.writeBoolean(this.mEnableNightMode);
        dest.writeBoolean(this.mEnableQuickDoze);
        dest.writeBoolean(this.mForceAllAppsStandby);
        dest.writeBoolean(this.mForceBackgroundCheck);
        dest.writeInt(this.mLocationMode);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : this.mDeviceSpecificSettings.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("adjust_brightness_disabled=");
        sb2.append(!this.mEnableAdjustBrightness);
        sb2.append(",adjust_brightness_factor=");
        sb2.append(this.mAdjustBrightnessFactor);
        sb2.append(",advertise_is_enabled=");
        sb2.append(this.mAdvertiseIsEnabled);
        sb2.append(",animation_disabled=");
        sb2.append(this.mDisableAnimation);
        sb2.append(",aod_disabled=");
        sb2.append(this.mDisableAod);
        sb2.append(",datasaver_disabled=");
        sb2.append(!this.mEnableDataSaver);
        sb2.append(",enable_night_mode=");
        sb2.append(this.mEnableNightMode);
        sb2.append(",firewall_disabled=");
        sb2.append(!this.mEnableFirewall);
        sb2.append(",force_all_apps_standby=");
        sb2.append(this.mForceAllAppsStandby);
        sb2.append(",force_background_check=");
        sb2.append(this.mForceBackgroundCheck);
        sb2.append(",fullbackup_deferred=");
        sb2.append(this.mDeferFullBackup);
        sb2.append(",gps_mode=");
        sb2.append(this.mLocationMode);
        sb2.append(",keyvaluebackup_deferred=");
        sb2.append(this.mDeferKeyValueBackup);
        sb2.append(",launch_boost_disabled=");
        sb2.append(this.mDisableLaunchBoost);
        sb2.append(",optional_sensors_disabled=");
        sb2.append(this.mDisableOptionalSensors);
        sb2.append(",quick_doze_enabled=");
        sb2.append(this.mEnableQuickDoze);
        sb2.append(",soundtrigger_disabled=");
        sb2.append(this.mDisableSoundTrigger);
        sb2.append(",vibration_disabled=");
        sb2.append(this.mDisableVibration);
        sb2.append(SmsManager.REGEX_PREFIX_DELIMITER);
        sb2.append(sb.toString());
        return sb2.toString();
    }

    public float getAdjustBrightnessFactor() {
        return this.mAdjustBrightnessFactor;
    }

    public boolean getAdvertiseIsEnabled() {
        return this.mAdvertiseIsEnabled;
    }

    public boolean getDeferFullBackup() {
        return this.mDeferFullBackup;
    }

    public boolean getDeferKeyValueBackup() {
        return this.mDeferKeyValueBackup;
    }

    public Map<String, String> getDeviceSpecificSettings() {
        return this.mDeviceSpecificSettings;
    }

    public boolean getDisableAnimation() {
        return this.mDisableAnimation;
    }

    public boolean getDisableAod() {
        return this.mDisableAod;
    }

    public boolean getDisableLaunchBoost() {
        return this.mDisableLaunchBoost;
    }

    public boolean getDisableOptionalSensors() {
        return this.mDisableOptionalSensors;
    }

    public boolean getDisableSoundTrigger() {
        return this.mDisableSoundTrigger;
    }

    public boolean getDisableVibration() {
        return this.mDisableVibration;
    }

    public boolean getEnableAdjustBrightness() {
        return this.mEnableAdjustBrightness;
    }

    public boolean getEnableDataSaver() {
        return this.mEnableDataSaver;
    }

    public boolean getEnableFirewall() {
        return this.mEnableFirewall;
    }

    public boolean getEnableNightMode() {
        return this.mEnableNightMode;
    }

    public boolean getEnableQuickDoze() {
        return this.mEnableQuickDoze;
    }

    public boolean getForceAllAppsStandby() {
        return this.mForceAllAppsStandby;
    }

    public boolean getForceBackgroundCheck() {
        return this.mForceBackgroundCheck;
    }

    public int getLocationMode() {
        return this.mLocationMode;
    }

    public static final class Builder {
        private float mAdjustBrightnessFactor = 1.0f;
        private boolean mAdvertiseIsEnabled = false;
        private boolean mDeferFullBackup = false;
        private boolean mDeferKeyValueBackup = false;
        private final ArrayMap<String, String> mDeviceSpecificSettings = new ArrayMap<>();
        private boolean mDisableAnimation = false;
        private boolean mDisableAod = false;
        private boolean mDisableLaunchBoost = false;
        private boolean mDisableOptionalSensors = false;
        private boolean mDisableSoundTrigger = false;
        private boolean mDisableVibration = false;
        private boolean mEnableAdjustBrightness = false;
        private boolean mEnableDataSaver = false;
        private boolean mEnableFirewall = false;
        private boolean mEnableNightMode = false;
        private boolean mEnableQuickDoze = false;
        private boolean mForceAllAppsStandby = false;
        private boolean mForceBackgroundCheck = false;
        private int mLocationMode = 0;

        public Builder setAdjustBrightnessFactor(float adjustBrightnessFactor) {
            this.mAdjustBrightnessFactor = adjustBrightnessFactor;
            return this;
        }

        public Builder setAdvertiseIsEnabled(boolean advertiseIsEnabled) {
            this.mAdvertiseIsEnabled = advertiseIsEnabled;
            return this;
        }

        public Builder setDeferFullBackup(boolean deferFullBackup) {
            this.mDeferFullBackup = deferFullBackup;
            return this;
        }

        public Builder setDeferKeyValueBackup(boolean deferKeyValueBackup) {
            this.mDeferKeyValueBackup = deferKeyValueBackup;
            return this;
        }

        public Builder addDeviceSpecificSetting(String key, String value) {
            if (key != null) {
                String key2 = key.trim();
                if (!TextUtils.isEmpty(key2)) {
                    this.mDeviceSpecificSettings.put(key2, TextUtils.emptyIfNull(value));
                    return this;
                }
                throw new IllegalArgumentException("Key cannot be empty");
            }
            throw new IllegalArgumentException("Key cannot be null");
        }

        public Builder setDisableAnimation(boolean disableAnimation) {
            this.mDisableAnimation = disableAnimation;
            return this;
        }

        public Builder setDisableAod(boolean disableAod) {
            this.mDisableAod = disableAod;
            return this;
        }

        public Builder setDisableLaunchBoost(boolean disableLaunchBoost) {
            this.mDisableLaunchBoost = disableLaunchBoost;
            return this;
        }

        public Builder setDisableOptionalSensors(boolean disableOptionalSensors) {
            this.mDisableOptionalSensors = disableOptionalSensors;
            return this;
        }

        public Builder setDisableSoundTrigger(boolean disableSoundTrigger) {
            this.mDisableSoundTrigger = disableSoundTrigger;
            return this;
        }

        public Builder setDisableVibration(boolean disableVibration) {
            this.mDisableVibration = disableVibration;
            return this;
        }

        public Builder setEnableAdjustBrightness(boolean enableAdjustBrightness) {
            this.mEnableAdjustBrightness = enableAdjustBrightness;
            return this;
        }

        public Builder setEnableDataSaver(boolean enableDataSaver) {
            this.mEnableDataSaver = enableDataSaver;
            return this;
        }

        public Builder setEnableFirewall(boolean enableFirewall) {
            this.mEnableFirewall = enableFirewall;
            return this;
        }

        public Builder setEnableNightMode(boolean enableNightMode) {
            this.mEnableNightMode = enableNightMode;
            return this;
        }

        public Builder setEnableQuickDoze(boolean enableQuickDoze) {
            this.mEnableQuickDoze = enableQuickDoze;
            return this;
        }

        public Builder setForceAllAppsStandby(boolean forceAllAppsStandby) {
            this.mForceAllAppsStandby = forceAllAppsStandby;
            return this;
        }

        public Builder setForceBackgroundCheck(boolean forceBackgroundCheck) {
            this.mForceBackgroundCheck = forceBackgroundCheck;
            return this;
        }

        public Builder setLocationMode(int locationMode) {
            this.mLocationMode = locationMode;
            return this;
        }

        public BatterySaverPolicyConfig build() {
            return new BatterySaverPolicyConfig(this);
        }
    }
}
