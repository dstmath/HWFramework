package android.telephony.euicc;

import android.os.Parcel;
import android.os.Parcelable;

public final class EuiccInfo implements Parcelable {
    public static final Parcelable.Creator<EuiccInfo> CREATOR = new Parcelable.Creator<EuiccInfo>() {
        public EuiccInfo createFromParcel(Parcel in) {
            return new EuiccInfo(in);
        }

        public EuiccInfo[] newArray(int size) {
            return new EuiccInfo[size];
        }
    };
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

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.osVersion);
    }

    public int describeContents() {
        return 0;
    }
}
