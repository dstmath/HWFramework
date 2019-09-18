package android.hardware.display;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class BrightnessChangeEvent implements Parcelable {
    public static final Parcelable.Creator<BrightnessChangeEvent> CREATOR = new Parcelable.Creator<BrightnessChangeEvent>() {
        public BrightnessChangeEvent createFromParcel(Parcel source) {
            return new BrightnessChangeEvent(source);
        }

        public BrightnessChangeEvent[] newArray(int size) {
            return new BrightnessChangeEvent[size];
        }
    };
    public final float batteryLevel;
    public final float brightness;
    public final int colorTemperature;
    public final boolean isDefaultBrightnessConfig;
    public final boolean isUserSetBrightness;
    public final float lastBrightness;
    public final long[] luxTimestamps;
    public final float[] luxValues;
    public final boolean nightMode;
    public final String packageName;
    public final float powerBrightnessFactor;
    public final long timeStamp;
    public final int userId;

    public static class Builder {
        private float mBatteryLevel;
        private float mBrightness;
        private int mColorTemperature;
        private boolean mIsDefaultBrightnessConfig;
        private boolean mIsUserSetBrightness;
        private float mLastBrightness;
        private long[] mLuxTimestamps;
        private float[] mLuxValues;
        private boolean mNightMode;
        private String mPackageName;
        private float mPowerBrightnessFactor;
        private long mTimeStamp;
        private int mUserId;

        public Builder setBrightness(float brightness) {
            this.mBrightness = brightness;
            return this;
        }

        public Builder setTimeStamp(long timeStamp) {
            this.mTimeStamp = timeStamp;
            return this;
        }

        public Builder setPackageName(String packageName) {
            this.mPackageName = packageName;
            return this;
        }

        public Builder setUserId(int userId) {
            this.mUserId = userId;
            return this;
        }

        public Builder setLuxValues(float[] luxValues) {
            this.mLuxValues = luxValues;
            return this;
        }

        public Builder setLuxTimestamps(long[] luxTimestamps) {
            this.mLuxTimestamps = luxTimestamps;
            return this;
        }

        public Builder setBatteryLevel(float batteryLevel) {
            this.mBatteryLevel = batteryLevel;
            return this;
        }

        public Builder setPowerBrightnessFactor(float powerBrightnessFactor) {
            this.mPowerBrightnessFactor = powerBrightnessFactor;
            return this;
        }

        public Builder setNightMode(boolean nightMode) {
            this.mNightMode = nightMode;
            return this;
        }

        public Builder setColorTemperature(int colorTemperature) {
            this.mColorTemperature = colorTemperature;
            return this;
        }

        public Builder setLastBrightness(float lastBrightness) {
            this.mLastBrightness = lastBrightness;
            return this;
        }

        public Builder setIsDefaultBrightnessConfig(boolean isDefaultBrightnessConfig) {
            this.mIsDefaultBrightnessConfig = isDefaultBrightnessConfig;
            return this;
        }

        public Builder setUserBrightnessPoint(boolean isUserSetBrightness) {
            this.mIsUserSetBrightness = isUserSetBrightness;
            return this;
        }

        public BrightnessChangeEvent build() {
            BrightnessChangeEvent brightnessChangeEvent = new BrightnessChangeEvent(this.mBrightness, this.mTimeStamp, this.mPackageName, this.mUserId, this.mLuxValues, this.mLuxTimestamps, this.mBatteryLevel, this.mPowerBrightnessFactor, this.mNightMode, this.mColorTemperature, this.mLastBrightness, this.mIsDefaultBrightnessConfig, this.mIsUserSetBrightness);
            return brightnessChangeEvent;
        }
    }

    private BrightnessChangeEvent(float brightness2, long timeStamp2, String packageName2, int userId2, float[] luxValues2, long[] luxTimestamps2, float batteryLevel2, float powerBrightnessFactor2, boolean nightMode2, int colorTemperature2, float lastBrightness2, boolean isDefaultBrightnessConfig2, boolean isUserSetBrightness2) {
        this.brightness = brightness2;
        this.timeStamp = timeStamp2;
        this.packageName = packageName2;
        this.userId = userId2;
        this.luxValues = luxValues2;
        this.luxTimestamps = luxTimestamps2;
        this.batteryLevel = batteryLevel2;
        this.powerBrightnessFactor = powerBrightnessFactor2;
        this.nightMode = nightMode2;
        this.colorTemperature = colorTemperature2;
        this.lastBrightness = lastBrightness2;
        this.isDefaultBrightnessConfig = isDefaultBrightnessConfig2;
        this.isUserSetBrightness = isUserSetBrightness2;
    }

    public BrightnessChangeEvent(BrightnessChangeEvent other, boolean redactPackage) {
        this.brightness = other.brightness;
        this.timeStamp = other.timeStamp;
        this.packageName = redactPackage ? null : other.packageName;
        this.userId = other.userId;
        this.luxValues = other.luxValues;
        this.luxTimestamps = other.luxTimestamps;
        this.batteryLevel = other.batteryLevel;
        this.powerBrightnessFactor = other.powerBrightnessFactor;
        this.nightMode = other.nightMode;
        this.colorTemperature = other.colorTemperature;
        this.lastBrightness = other.lastBrightness;
        this.isDefaultBrightnessConfig = other.isDefaultBrightnessConfig;
        this.isUserSetBrightness = other.isUserSetBrightness;
    }

    private BrightnessChangeEvent(Parcel source) {
        this.brightness = source.readFloat();
        this.timeStamp = source.readLong();
        this.packageName = source.readString();
        this.userId = source.readInt();
        this.luxValues = source.createFloatArray();
        this.luxTimestamps = source.createLongArray();
        this.batteryLevel = source.readFloat();
        this.powerBrightnessFactor = source.readFloat();
        this.nightMode = source.readBoolean();
        this.colorTemperature = source.readInt();
        this.lastBrightness = source.readFloat();
        this.isDefaultBrightnessConfig = source.readBoolean();
        this.isUserSetBrightness = source.readBoolean();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.brightness);
        dest.writeLong(this.timeStamp);
        dest.writeString(this.packageName);
        dest.writeInt(this.userId);
        dest.writeFloatArray(this.luxValues);
        dest.writeLongArray(this.luxTimestamps);
        dest.writeFloat(this.batteryLevel);
        dest.writeFloat(this.powerBrightnessFactor);
        dest.writeBoolean(this.nightMode);
        dest.writeInt(this.colorTemperature);
        dest.writeFloat(this.lastBrightness);
        dest.writeBoolean(this.isDefaultBrightnessConfig);
        dest.writeBoolean(this.isUserSetBrightness);
    }
}
