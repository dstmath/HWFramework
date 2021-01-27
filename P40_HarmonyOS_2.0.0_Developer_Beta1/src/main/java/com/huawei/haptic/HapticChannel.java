package com.huawei.haptic;

import com.huawei.annotation.HwSystemApi;
import com.huawei.haptic.HwHapticChannel;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class HapticChannel {
    public static final int HAPTIC_CHANNEL_A = 1;
    public static final int HAPTIC_CHANNEL_B = 2;
    public static final int HAPTIC_CHANNEL_DEFAULT = 0;
    public static final int SLICE_TYPE_CONTINUOUS = 2;
    public static final int SLICE_TYPE_TRANSIENT = 1;
    private final int mChannelId;
    private int mDuration;
    private List<HapticSlice> mHapticSlices = new ArrayList();
    private final HapticCurve mIntensityCurve;
    private final HapticCurve mSharpnessCurve;

    protected HapticChannel(Builder builder) {
        this.mChannelId = builder.mChannelId;
        this.mHapticSlices = builder.mHapticSlices;
        this.mIntensityCurve = builder.mIntensityCurve;
        this.mSharpnessCurve = builder.mSharpnessCurve;
        for (HapticSlice slice : this.mHapticSlices) {
            this.mDuration += slice.mDuration;
        }
    }

    public int getDuration() {
        return this.mDuration;
    }

    public int getChannelId() {
        return this.mChannelId;
    }

    static HwHapticChannel createHwHapticChannel(HapticChannel channel) {
        if (channel == null) {
            return null;
        }
        HwHapticChannel hwHapticChannel = new HwHapticChannel();
        for (HapticSlice slice : channel.mHapticSlices) {
            hwHapticChannel.mHapticSlices.add(new HwHapticChannel.HwHapticSlice(slice.mTimeStamp, slice.mType, slice.mDuration, slice.mIntensity, slice.mSharpness));
        }
        hwHapticChannel.mDuration = channel.mDuration;
        hwHapticChannel.mChannelId = channel.mChannelId;
        hwHapticChannel.mIntensityCurve = HapticCurve.createHwHapticCurve(channel.mIntensityCurve);
        hwHapticChannel.mSharpnessCurve = HapticCurve.createHwHapticCurve(channel.mSharpnessCurve);
        return hwHapticChannel;
    }

    public static class HapticSlice {
        private final int mDuration;
        private final float mIntensity;
        private final float mSharpness;
        private final int mTimeStamp;
        private final int mType;

        public HapticSlice(int time, int type, int duration, float intensity, float sharpness) {
            this.mTimeStamp = time;
            this.mType = type;
            this.mDuration = duration;
            this.mIntensity = intensity;
            this.mSharpness = sharpness;
        }
    }

    public static class Builder {
        private int mChannelId;
        private final List<HapticSlice> mHapticSlices = new ArrayList();
        private HapticCurve mIntensityCurve;
        private HapticCurve mSharpnessCurve;

        public Builder addSlice(int time, int type, int duration, float intensity, float sharpness) {
            this.mHapticSlices.add(new HapticSlice(time, type, duration, intensity, sharpness));
            return this;
        }

        public Builder addSlice(HapticSlice slice) {
            this.mHapticSlices.add(slice);
            return this;
        }

        public Builder setIntensityCurve(HapticCurve curve) {
            this.mIntensityCurve = curve;
            return this;
        }

        public Builder setSharpnessCurve(HapticCurve curve) {
            this.mSharpnessCurve = curve;
            return this;
        }

        public Builder setChannelId(int channelId) {
            this.mChannelId = channelId;
            return this;
        }

        public HapticChannel build() {
            return new HapticChannel(this);
        }
    }
}
