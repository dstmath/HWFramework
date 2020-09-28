package com.huawei.android.hardware.mtkfmradio;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;

public class MtkFmConfig implements BaseFmConfig, Parcelable {
    private static final int CONVERT_RATE = 10;
    public static final Parcelable.Creator<MtkFmConfig> CREATOR = new Parcelable.Creator<MtkFmConfig>() {
        /* class com.huawei.android.hardware.mtkfmradio.MtkFmConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkFmConfig createFromParcel(Parcel in) {
            MtkFmConfig result = new MtkFmConfig();
            if (in != null) {
                result.mRadioBand = in.readInt();
                result.mEmphasis = in.readInt();
                result.mChSpacing = in.readInt();
                result.mRdsStd = in.readInt();
                result.mBandLowerLimit = in.readInt();
                result.mBandUpperLimit = in.readInt();
            }
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public MtkFmConfig[] newArray(int size) {
            return new MtkFmConfig[size];
        }
    };
    private static final String TAG = "FmConfig";
    private int mBandLowerLimit;
    private int mBandUpperLimit;
    private int mChSpacing;
    private int mEmphasis;
    private int mRadioBand;
    private int mRdsStd;

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getRadioBand() {
        return this.mRadioBand;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setRadioBand(int band) {
        this.mRadioBand = band;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getEmphasis() {
        return this.mEmphasis;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setEmphasis(int emp) {
        this.mEmphasis = emp;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getChSpacing() {
        return this.mChSpacing;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setChSpacing(int spacing) {
        this.mChSpacing = spacing;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getRdsStd() {
        return this.mRdsStd;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setRdsStd(int rdsStandard) {
        this.mRdsStd = rdsStandard;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getLowerLimit() {
        return this.mBandLowerLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setLowerLimit(int lowLimit) {
        this.mBandLowerLimit = lowLimit / 10;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getUpperLimit() {
        return this.mBandUpperLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setUpperLimit(int upLimit) {
        this.mBandUpperLimit = upLimit / 10;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public boolean fmConfigure(int fd) {
        return false;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeInt(this.mRadioBand);
            dest.writeInt(this.mEmphasis);
            dest.writeInt(this.mChSpacing);
            dest.writeInt(this.mRdsStd);
            dest.writeInt(this.mBandLowerLimit);
            dest.writeInt(this.mBandUpperLimit);
        }
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(" RadioBand: ");
        sbuf.append(this.mRadioBand);
        sbuf.append('\n');
        sbuf.append(" Emphasis: ");
        sbuf.append(this.mEmphasis);
        sbuf.append("\n");
        sbuf.append(" ChSpacing: ");
        sbuf.append(this.mChSpacing);
        sbuf.append('\n');
        sbuf.append(" RdsStd: ");
        sbuf.append(this.mRdsStd);
        sbuf.append("\n");
        sbuf.append(" BandLowerLimit: ");
        sbuf.append(this.mBandLowerLimit);
        sbuf.append('\n');
        sbuf.append(" BandUpperLimit: ");
        sbuf.append(this.mBandUpperLimit);
        sbuf.append("\n");
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }
}
