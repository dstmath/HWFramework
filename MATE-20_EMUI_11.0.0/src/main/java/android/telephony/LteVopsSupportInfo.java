package android.telephony;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

@SystemApi
public final class LteVopsSupportInfo implements Parcelable {
    public static final Parcelable.Creator<LteVopsSupportInfo> CREATOR = new Parcelable.Creator<LteVopsSupportInfo>() {
        /* class android.telephony.LteVopsSupportInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LteVopsSupportInfo createFromParcel(Parcel in) {
            return new LteVopsSupportInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public LteVopsSupportInfo[] newArray(int size) {
            return new LteVopsSupportInfo[size];
        }
    };
    public static final int LTE_STATUS_NOT_AVAILABLE = 1;
    public static final int LTE_STATUS_NOT_SUPPORTED = 3;
    public static final int LTE_STATUS_SUPPORTED = 2;
    private final int mEmcBearerSupport;
    private final int mVopsSupport;

    @Retention(RetentionPolicy.SOURCE)
    public @interface LteVopsStatus {
    }

    public LteVopsSupportInfo(int vops, int emergency) {
        this.mVopsSupport = vops;
        this.mEmcBearerSupport = emergency;
    }

    public int getVopsSupport() {
        return this.mVopsSupport;
    }

    public int getEmcBearerSupport() {
        return this.mEmcBearerSupport;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mVopsSupport);
        out.writeInt(this.mEmcBearerSupport);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof LteVopsSupportInfo)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        LteVopsSupportInfo other = (LteVopsSupportInfo) o;
        if (this.mVopsSupport == other.mVopsSupport && this.mEmcBearerSupport == other.mEmcBearerSupport) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mVopsSupport), Integer.valueOf(this.mEmcBearerSupport));
    }

    public String toString() {
        return "LteVopsSupportInfo :  mVopsSupport = " + this.mVopsSupport + " mEmcBearerSupport = " + this.mEmcBearerSupport;
    }

    private LteVopsSupportInfo(Parcel in) {
        this.mVopsSupport = in.readInt();
        this.mEmcBearerSupport = in.readInt();
    }
}
