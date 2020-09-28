package android.telephony.euicc;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public final class EuiccInfo implements Parcelable {
    public static final Parcelable.Creator<EuiccInfo> CREATOR = new Parcelable.Creator<EuiccInfo>() {
        /* class android.telephony.euicc.EuiccInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EuiccInfo createFromParcel(Parcel in) {
            return new EuiccInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public EuiccInfo[] newArray(int size) {
            return new EuiccInfo[size];
        }
    };
    @UnsupportedAppUsage
    private final String osVersion;

    public String getOsVersion() {
        return this.osVersion;
    }

    public EuiccInfo(String osVersion2) {
        this.osVersion = osVersion2;
    }

    private EuiccInfo(Parcel in) {
        this.osVersion = in.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.osVersion);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
