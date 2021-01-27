package com.huawei.haptic;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class HwHapticChannel implements Parcelable {
    public static final Parcelable.Creator<HwHapticChannel> CREATOR = new Parcelable.Creator<HwHapticChannel>() {
        /* class com.huawei.haptic.HwHapticChannel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwHapticChannel createFromParcel(Parcel in) {
            return new HwHapticChannel(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwHapticChannel[] newArray(int size) {
            return new HwHapticChannel[size];
        }
    };
    public static final int HAPTIC_CHANNEL_A = 1;
    public static final int HAPTIC_CHANNEL_B = 2;
    public static final int HAPTIC_CHANNEL_DEFAULT = 0;
    public static final int SLICE_TYPE_CONTINUOUS = 2;
    public static final int SLICE_TYPE_TRANSIENT = 1;
    public int mChannelId;
    public int mDuration;
    public List<HwHapticSlice> mHapticSlices = new ArrayList();
    public HwHapticCurve mIntensityCurve;
    public HwHapticCurve mSharpnessCurve;

    public HwHapticChannel() {
    }

    public HwHapticChannel(Parcel in) {
        this.mHapticSlices = in.readArrayList(null);
        this.mIntensityCurve = (HwHapticCurve) in.readParcelable(null);
        this.mSharpnessCurve = (HwHapticCurve) in.readParcelable(null);
        this.mChannelId = in.readInt();
        for (HwHapticSlice slice : this.mHapticSlices) {
            this.mDuration += slice.mDuration;
        }
    }

    protected HwHapticChannel(Builder builder) {
        this.mHapticSlices = builder.mHapticSlices;
        this.mIntensityCurve = builder.mIntensityCurve;
        this.mSharpnessCurve = builder.mSharpnessCurve;
        this.mChannelId = builder.mChannelId;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(this.mHapticSlices);
        out.writeParcelable(this.mIntensityCurve, 0);
        out.writeParcelable(this.mSharpnessCurve, 0);
        out.writeInt(this.mChannelId);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public int getChannelId() {
        return this.mChannelId;
    }

    public static class HwHapticSlice implements Parcelable {
        public static final Parcelable.Creator<HwHapticSlice> CREATOR = new Parcelable.Creator<HwHapticSlice>() {
            /* class com.huawei.haptic.HwHapticChannel.HwHapticSlice.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public HwHapticSlice createFromParcel(Parcel in) {
                return new HwHapticSlice(in);
            }

            @Override // android.os.Parcelable.Creator
            public HwHapticSlice[] newArray(int size) {
                if (size > 64) {
                    return new HwHapticSlice[0];
                }
                return new HwHapticSlice[size];
            }
        };
        public final int mDuration;
        public final float mIntensity;
        public final float mSharpness;
        public final int mTimeStamp;
        public final int mType;

        public HwHapticSlice(int time, int type, int duration, float intensity, float sharpness) {
            this.mTimeStamp = time;
            this.mType = type;
            this.mDuration = duration;
            this.mIntensity = intensity;
            this.mSharpness = sharpness;
        }

        public HwHapticSlice(Parcel in) {
            this.mTimeStamp = in.readInt();
            this.mType = in.readInt();
            this.mDuration = in.readInt();
            this.mIntensity = in.readFloat();
            this.mSharpness = in.readFloat();
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mTimeStamp);
            out.writeInt(this.mType);
            out.writeInt(this.mDuration);
            out.writeFloat(this.mIntensity);
            out.writeFloat(this.mSharpness);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }
    }

    public static class Builder {
        private int mChannelId;
        private final List<HwHapticSlice> mHapticSlices = new ArrayList();
        private HwHapticCurve mIntensityCurve;
        private HwHapticCurve mSharpnessCurve;

        public Builder addSlice(int time, int type, int duration, float intensity, float sharpness) {
            this.mHapticSlices.add(new HwHapticSlice(time, type, duration, intensity, sharpness));
            return this;
        }

        public Builder addSlice(HwHapticSlice slice) {
            this.mHapticSlices.add(slice);
            return this;
        }

        public Builder setIntensityCurve(HwHapticCurve curve) {
            this.mIntensityCurve = curve;
            return this;
        }

        public Builder setSharpnessCurve(HwHapticCurve curve) {
            this.mSharpnessCurve = curve;
            return this;
        }

        public Builder setChannelId(int channelId) {
            this.mChannelId = channelId;
            return this;
        }

        public HwHapticChannel build() {
            return new HwHapticChannel(this);
        }
    }
}
