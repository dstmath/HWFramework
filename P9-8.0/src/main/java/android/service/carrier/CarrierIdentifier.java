package android.service.carrier;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CarrierIdentifier implements Parcelable {
    public static final Creator<CarrierIdentifier> CREATOR = new Creator<CarrierIdentifier>() {
        public CarrierIdentifier createFromParcel(Parcel parcel) {
            return new CarrierIdentifier(parcel);
        }

        public CarrierIdentifier[] newArray(int i) {
            return new CarrierIdentifier[i];
        }
    };
    private String mGid1;
    private String mGid2;
    private String mImsi;
    private String mMcc;
    private String mMnc;
    private String mSpn;

    public interface MatchType {
        public static final int ALL = 0;
        public static final int GID1 = 3;
        public static final int GID2 = 4;
        public static final int IMSI_PREFIX = 2;
        public static final int SPN = 1;
    }

    public CarrierIdentifier(String mcc, String mnc, String spn, String imsi, String gid1, String gid2) {
        this.mMcc = mcc;
        this.mMnc = mnc;
        this.mSpn = spn;
        this.mImsi = imsi;
        this.mGid1 = gid1;
        this.mGid2 = gid2;
    }

    public CarrierIdentifier(Parcel parcel) {
        readFromParcel(parcel);
    }

    public String getMcc() {
        return this.mMcc;
    }

    public String getMnc() {
        return this.mMnc;
    }

    public String getSpn() {
        return this.mSpn;
    }

    public String getImsi() {
        return this.mImsi;
    }

    public String getGid1() {
        return this.mGid1;
    }

    public String getGid2() {
        return this.mGid2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mMcc);
        out.writeString(this.mMnc);
        out.writeString(this.mSpn);
        out.writeString(this.mImsi);
        out.writeString(this.mGid1);
        out.writeString(this.mGid2);
    }

    public String toString() {
        return "CarrierIdentifier{mcc=" + this.mMcc + ",mnc=" + this.mMnc + ",spn=" + this.mSpn + ",imsi=" + this.mImsi + ",gid1=" + this.mGid1 + ",gid2=" + this.mGid2 + "}";
    }

    public void readFromParcel(Parcel in) {
        this.mMcc = in.readString();
        this.mMnc = in.readString();
        this.mSpn = in.readString();
        this.mImsi = in.readString();
        this.mGid1 = in.readString();
        this.mGid2 = in.readString();
    }
}
