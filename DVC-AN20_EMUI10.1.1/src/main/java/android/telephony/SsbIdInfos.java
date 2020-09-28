package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class SsbIdInfos implements Parcelable {
    public static final Parcelable.Creator<SsbIdInfos> CREATOR = new Parcelable.Creator<SsbIdInfos>() {
        /* class android.telephony.SsbIdInfos.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SsbIdInfos createFromParcel(Parcel in) {
            return new SsbIdInfos(in);
        }

        @Override // android.os.Parcelable.Creator
        public SsbIdInfos[] newArray(int size) {
            return new SsbIdInfos[size];
        }
    };
    private int mSsbId;
    private int mSsbRsrp;

    public SsbIdInfos() {
    }

    @UnsupportedAppUsage
    public SsbIdInfos(int ssbId, int ssbRsrp) {
        this.mSsbId = ssbId;
        this.mSsbRsrp = ssbRsrp;
    }

    public SsbIdInfos(Parcel in) {
        this.mSsbId = in.readInt();
        this.mSsbRsrp = in.readInt();
    }

    @UnsupportedAppUsage
    public void setSsbId(int ssbId) {
        this.mSsbId = ssbId;
    }

    public int getSsbId() {
        return this.mSsbId;
    }

    @UnsupportedAppUsage
    public void setSsbRsrp(int ssbRsrp) {
        this.mSsbRsrp = ssbRsrp;
    }

    public int getSsbRsrp() {
        return this.mSsbRsrp;
    }

    public String toString() {
        String str;
        synchronized (this) {
            str = "[" + "ssbId: **" + ", ssbRsrp: " + this.mSsbRsrp + "]";
        }
        return str;
    }

    public int describeContents() {
        return 0;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mSsbId), Integer.valueOf(this.mSsbRsrp));
    }

    public boolean equals(Object object) {
        if (!(object instanceof SsbIdInfos)) {
            return false;
        }
        SsbIdInfos other = (SsbIdInfos) object;
        if (this.mSsbId == other.mSsbId && this.mSsbRsrp == other.mSsbRsrp) {
            return true;
        }
        return false;
    }

    public void writeToParcel(Parcel dest, int flags) {
        synchronized (this) {
            dest.writeInt(this.mSsbId);
            dest.writeInt(this.mSsbRsrp);
        }
    }
}
