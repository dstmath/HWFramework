package android.net.wifi.p2p;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class WifiP2pInfo implements Parcelable {
    public static final Creator<WifiP2pInfo> CREATOR = new Creator<WifiP2pInfo>() {
        public WifiP2pInfo createFromParcel(Parcel in) {
            boolean z = false;
            WifiP2pInfo info = new WifiP2pInfo();
            info.groupFormed = in.readByte() == (byte) 1;
            if (in.readByte() == (byte) 1) {
                z = true;
            }
            info.isGroupOwner = z;
            if (in.readByte() == (byte) 1) {
                try {
                    info.groupOwnerAddress = InetAddress.getByAddress(in.createByteArray());
                } catch (UnknownHostException e) {
                }
            }
            return info;
        }

        public WifiP2pInfo[] newArray(int size) {
            return new WifiP2pInfo[size];
        }
    };
    public boolean groupFormed;
    public InetAddress groupOwnerAddress;
    public boolean isGroupOwner;

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("groupFormed: ").append(this.groupFormed).append(" isGroupOwner: ").append(this.isGroupOwner).append(" groupOwnerAddress: ").append(this.groupOwnerAddress);
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public WifiP2pInfo(WifiP2pInfo source) {
        if (source != null) {
            this.groupFormed = source.groupFormed;
            this.isGroupOwner = source.isGroupOwner;
            this.groupOwnerAddress = source.groupOwnerAddress;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        byte b;
        dest.writeByte(this.groupFormed ? (byte) 1 : (byte) 0);
        if (this.isGroupOwner) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        dest.writeByte(b);
        if (this.groupOwnerAddress != null) {
            dest.writeByte((byte) 1);
            dest.writeByteArray(this.groupOwnerAddress.getAddress());
            return;
        }
        dest.writeByte((byte) 0);
    }
}
