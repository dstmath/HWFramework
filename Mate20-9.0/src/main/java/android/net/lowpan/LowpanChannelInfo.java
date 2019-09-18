package android.net.lowpan;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class LowpanChannelInfo implements Parcelable {
    public static final Parcelable.Creator<LowpanChannelInfo> CREATOR = new Parcelable.Creator<LowpanChannelInfo>() {
        public LowpanChannelInfo createFromParcel(Parcel in) {
            LowpanChannelInfo info = new LowpanChannelInfo();
            int unused = info.mIndex = in.readInt();
            String unused2 = info.mName = in.readString();
            float unused3 = info.mSpectrumCenterFrequency = in.readFloat();
            float unused4 = info.mSpectrumBandwidth = in.readFloat();
            int unused5 = info.mMaxTransmitPower = in.readInt();
            boolean unused6 = info.mIsMaskedByRegulatoryDomain = in.readBoolean();
            return info;
        }

        public LowpanChannelInfo[] newArray(int size) {
            return new LowpanChannelInfo[size];
        }
    };
    public static final float UNKNOWN_BANDWIDTH = 0.0f;
    public static final float UNKNOWN_FREQUENCY = 0.0f;
    public static final int UNKNOWN_POWER = Integer.MAX_VALUE;
    /* access modifiers changed from: private */
    public int mIndex;
    /* access modifiers changed from: private */
    public boolean mIsMaskedByRegulatoryDomain;
    /* access modifiers changed from: private */
    public int mMaxTransmitPower;
    /* access modifiers changed from: private */
    public String mName;
    /* access modifiers changed from: private */
    public float mSpectrumBandwidth;
    /* access modifiers changed from: private */
    public float mSpectrumCenterFrequency;

    public static LowpanChannelInfo getChannelInfoForIeee802154Page0(int index) {
        LowpanChannelInfo info = new LowpanChannelInfo();
        if (index < 0) {
            info = null;
        } else if (index == 0) {
            info.mSpectrumCenterFrequency = 8.6830003E8f;
            info.mSpectrumBandwidth = 600000.0f;
        } else if (index < 11) {
            info.mSpectrumCenterFrequency = 9.04E8f + (2000000.0f * ((float) index));
            info.mSpectrumBandwidth = 0.0f;
        } else if (index < 26) {
            info.mSpectrumCenterFrequency = 2.34999987E9f + (5000000.0f * ((float) index));
            info.mSpectrumBandwidth = 2000000.0f;
        } else {
            info = null;
        }
        info.mName = Integer.toString(index);
        return info;
    }

    private LowpanChannelInfo() {
        this.mIndex = 0;
        this.mName = null;
        this.mSpectrumCenterFrequency = 0.0f;
        this.mSpectrumBandwidth = 0.0f;
        this.mMaxTransmitPower = Integer.MAX_VALUE;
        this.mIsMaskedByRegulatoryDomain = false;
    }

    private LowpanChannelInfo(int index, String name, float cf, float bw) {
        this.mIndex = 0;
        this.mName = null;
        this.mSpectrumCenterFrequency = 0.0f;
        this.mSpectrumBandwidth = 0.0f;
        this.mMaxTransmitPower = Integer.MAX_VALUE;
        this.mIsMaskedByRegulatoryDomain = false;
        this.mIndex = index;
        this.mName = name;
        this.mSpectrumCenterFrequency = cf;
        this.mSpectrumBandwidth = bw;
    }

    public String getName() {
        return this.mName;
    }

    public int getIndex() {
        return this.mIndex;
    }

    public int getMaxTransmitPower() {
        return this.mMaxTransmitPower;
    }

    public boolean isMaskedByRegulatoryDomain() {
        return this.mIsMaskedByRegulatoryDomain;
    }

    public float getSpectrumCenterFrequency() {
        return this.mSpectrumCenterFrequency;
    }

    public float getSpectrumBandwidth() {
        return this.mSpectrumBandwidth;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Channel ");
        sb.append(this.mIndex);
        if (this.mName != null && !this.mName.equals(Integer.toString(this.mIndex))) {
            sb.append(" (");
            sb.append(this.mName);
            sb.append(")");
        }
        if (this.mSpectrumCenterFrequency > 0.0f) {
            if (this.mSpectrumCenterFrequency > 1.0E9f) {
                sb.append(", SpectrumCenterFrequency: ");
                sb.append(this.mSpectrumCenterFrequency / 1.0E9f);
                sb.append("GHz");
            } else if (this.mSpectrumCenterFrequency > 1000000.0f) {
                sb.append(", SpectrumCenterFrequency: ");
                sb.append(this.mSpectrumCenterFrequency / 1000000.0f);
                sb.append("MHz");
            } else {
                sb.append(", SpectrumCenterFrequency: ");
                sb.append(this.mSpectrumCenterFrequency / 1000.0f);
                sb.append("kHz");
            }
        }
        if (this.mSpectrumBandwidth > 0.0f) {
            if (this.mSpectrumBandwidth > 1.0E9f) {
                sb.append(", SpectrumBandwidth: ");
                sb.append(this.mSpectrumBandwidth / 1.0E9f);
                sb.append("GHz");
            } else if (this.mSpectrumBandwidth > 1000000.0f) {
                sb.append(", SpectrumBandwidth: ");
                sb.append(this.mSpectrumBandwidth / 1000000.0f);
                sb.append("MHz");
            } else {
                sb.append(", SpectrumBandwidth: ");
                sb.append(this.mSpectrumBandwidth / 1000.0f);
                sb.append("kHz");
            }
        }
        if (this.mMaxTransmitPower != Integer.MAX_VALUE) {
            sb.append(", MaxTransmitPower: ");
            sb.append(this.mMaxTransmitPower);
            sb.append("dBm");
        }
        return sb.toString();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof LowpanChannelInfo)) {
            return false;
        }
        LowpanChannelInfo rhs = (LowpanChannelInfo) obj;
        if (Objects.equals(this.mName, rhs.mName) && this.mIndex == rhs.mIndex && this.mIsMaskedByRegulatoryDomain == rhs.mIsMaskedByRegulatoryDomain && this.mSpectrumCenterFrequency == rhs.mSpectrumCenterFrequency && this.mSpectrumBandwidth == rhs.mSpectrumBandwidth && this.mMaxTransmitPower == rhs.mMaxTransmitPower) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mName, Integer.valueOf(this.mIndex), Boolean.valueOf(this.mIsMaskedByRegulatoryDomain), Float.valueOf(this.mSpectrumCenterFrequency), Float.valueOf(this.mSpectrumBandwidth), Integer.valueOf(this.mMaxTransmitPower)});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mIndex);
        dest.writeString(this.mName);
        dest.writeFloat(this.mSpectrumCenterFrequency);
        dest.writeFloat(this.mSpectrumBandwidth);
        dest.writeInt(this.mMaxTransmitPower);
        dest.writeBoolean(this.mIsMaskedByRegulatoryDomain);
    }
}
