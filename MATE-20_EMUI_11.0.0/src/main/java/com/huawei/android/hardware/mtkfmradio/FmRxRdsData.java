package com.huawei.android.hardware.mtkfmradio;

import android.media.BuildConfig;
import android.os.Parcel;
import android.os.Parcelable;

/* access modifiers changed from: package-private */
public class FmRxRdsData implements Parcelable {
    public static final Parcelable.Creator<FmRxRdsData> CREATOR = new Parcelable.Creator<FmRxRdsData>() {
        /* class com.huawei.android.hardware.mtkfmradio.FmRxRdsData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FmRxRdsData createFromParcel(Parcel in) {
            FmRxRdsData result = new FmRxRdsData();
            if (in != null) {
                result.mPsString = in.readString();
                result.mRtTextString = in.readString();
                result.mPrgmId = in.readInt();
                result.mPrgmType = in.readInt();
            }
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public FmRxRdsData[] newArray(int size) {
            return new FmRxRdsData[size];
        }
    };
    private static final String TAG = "FmRxRdsData";
    private int mPrgmId;
    private int mPrgmType;
    private String mPsString = BuildConfig.FLAVOR;
    private String mRtTextString = BuildConfig.FLAVOR;

    public void setPrgmServices(String ps) {
        if (ps != null && this.mPsString.compareTo(ps) != 0) {
            this.mPsString = ps;
        }
    }

    public String getPrgmServices() {
        return this.mPsString;
    }

    public void setRadioText(String rt) {
        if (rt != null && this.mRtTextString.compareTo(rt) != 0) {
            this.mRtTextString = rt;
        }
    }

    public String getRadioText() {
        return this.mRtTextString;
    }

    public void setPrgmType(int type) {
        this.mPrgmType = type;
    }

    public int getPrgmType() {
        return this.mPrgmType;
    }

    public void setPrgmId(int id) {
        this.mPrgmId = id;
    }

    public int getPrgmId() {
        return this.mPrgmId;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeString(this.mPsString);
            dest.writeString(this.mRtTextString);
            dest.writeInt(this.mPrgmId);
            dest.writeInt(this.mPrgmType);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(" PsString: ");
        sbuf.append(this.mPsString);
        sbuf.append('\n');
        sbuf.append(" RtTextString: ");
        sbuf.append(this.mRtTextString);
        sbuf.append("\n");
        sbuf.append(" PrgmId: ");
        sbuf.append(this.mPrgmId);
        sbuf.append('\n');
        sbuf.append(" PrgmType: ");
        sbuf.append(this.mPrgmType);
        sbuf.append("\n");
        return sbuf.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
