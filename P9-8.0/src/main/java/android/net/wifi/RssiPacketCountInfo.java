package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class RssiPacketCountInfo implements Parcelable {
    public static final Creator<RssiPacketCountInfo> CREATOR = new Creator<RssiPacketCountInfo>() {
        public RssiPacketCountInfo createFromParcel(Parcel in) {
            return new RssiPacketCountInfo(in, null);
        }

        public RssiPacketCountInfo[] newArray(int size) {
            return new RssiPacketCountInfo[size];
        }
    };
    public int rssi;
    public int rxgood;
    public int txbad;
    public int txgood;

    /* synthetic */ RssiPacketCountInfo(Parcel in, RssiPacketCountInfo -this1) {
        this(in);
    }

    public RssiPacketCountInfo() {
        this.rxgood = 0;
        this.txbad = 0;
        this.txgood = 0;
        this.rssi = 0;
    }

    private RssiPacketCountInfo(Parcel in) {
        this.rssi = in.readInt();
        this.txgood = in.readInt();
        this.txbad = in.readInt();
        this.rxgood = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.rssi);
        out.writeInt(this.txgood);
        out.writeInt(this.txbad);
        out.writeInt(this.rxgood);
    }

    public int describeContents() {
        return 0;
    }
}
