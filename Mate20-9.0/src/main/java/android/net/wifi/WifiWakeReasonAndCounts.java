package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;

public class WifiWakeReasonAndCounts implements Parcelable {
    public static final Parcelable.Creator<WifiWakeReasonAndCounts> CREATOR = new Parcelable.Creator<WifiWakeReasonAndCounts>() {
        public WifiWakeReasonAndCounts createFromParcel(Parcel in) {
            WifiWakeReasonAndCounts counts = new WifiWakeReasonAndCounts();
            counts.totalCmdEventWake = in.readInt();
            counts.totalDriverFwLocalWake = in.readInt();
            counts.totalRxDataWake = in.readInt();
            counts.rxUnicast = in.readInt();
            counts.rxMulticast = in.readInt();
            counts.rxBroadcast = in.readInt();
            counts.icmp = in.readInt();
            counts.icmp6 = in.readInt();
            counts.icmp6Ra = in.readInt();
            counts.icmp6Na = in.readInt();
            counts.icmp6Ns = in.readInt();
            counts.ipv4RxMulticast = in.readInt();
            counts.ipv6Multicast = in.readInt();
            counts.otherRxMulticast = in.readInt();
            in.readIntArray(counts.cmdEventWakeCntArray);
            in.readIntArray(counts.driverFWLocalWakeCntArray);
            return counts;
        }

        public WifiWakeReasonAndCounts[] newArray(int size) {
            return new WifiWakeReasonAndCounts[size];
        }
    };
    private static final String TAG = "WifiWakeReasonAndCounts";
    public int[] cmdEventWakeCntArray;
    public int[] driverFWLocalWakeCntArray;
    public int icmp;
    public int icmp6;
    public int icmp6Na;
    public int icmp6Ns;
    public int icmp6Ra;
    public int ipv4RxMulticast;
    public int ipv6Multicast;
    public int otherRxMulticast;
    public int rxBroadcast;
    public int rxMulticast;
    public int rxUnicast;
    public int totalCmdEventWake;
    public int totalDriverFwLocalWake;
    public int totalRxDataWake;

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" totalCmdEventWake ");
        sb.append(this.totalCmdEventWake);
        sb.append(" totalDriverFwLocalWake ");
        sb.append(this.totalDriverFwLocalWake);
        sb.append(" totalRxDataWake ");
        sb.append(this.totalRxDataWake);
        sb.append(" rxUnicast ");
        sb.append(this.rxUnicast);
        sb.append(" rxMulticast ");
        sb.append(this.rxMulticast);
        sb.append(" rxBroadcast ");
        sb.append(this.rxBroadcast);
        sb.append(" icmp ");
        sb.append(this.icmp);
        sb.append(" icmp6 ");
        sb.append(this.icmp6);
        sb.append(" icmp6Ra ");
        sb.append(this.icmp6Ra);
        sb.append(" icmp6Na ");
        sb.append(this.icmp6Na);
        sb.append(" icmp6Ns ");
        sb.append(this.icmp6Ns);
        sb.append(" ipv4RxMulticast ");
        sb.append(this.ipv4RxMulticast);
        sb.append(" ipv6Multicast ");
        sb.append(this.ipv6Multicast);
        sb.append(" otherRxMulticast ");
        sb.append(this.otherRxMulticast);
        for (int i = 0; i < this.cmdEventWakeCntArray.length; i++) {
            sb.append(" cmdEventWakeCntArray[" + i + "] " + this.cmdEventWakeCntArray[i]);
        }
        for (int i2 = 0; i2 < this.driverFWLocalWakeCntArray.length; i2++) {
            sb.append(" driverFWLocalWakeCntArray[" + i2 + "] " + this.driverFWLocalWakeCntArray[i2]);
        }
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.totalCmdEventWake);
        dest.writeInt(this.totalDriverFwLocalWake);
        dest.writeInt(this.totalRxDataWake);
        dest.writeInt(this.rxUnicast);
        dest.writeInt(this.rxMulticast);
        dest.writeInt(this.rxBroadcast);
        dest.writeInt(this.icmp);
        dest.writeInt(this.icmp6);
        dest.writeInt(this.icmp6Ra);
        dest.writeInt(this.icmp6Na);
        dest.writeInt(this.icmp6Ns);
        dest.writeInt(this.ipv4RxMulticast);
        dest.writeInt(this.ipv6Multicast);
        dest.writeInt(this.otherRxMulticast);
        dest.writeIntArray(this.cmdEventWakeCntArray);
        dest.writeIntArray(this.driverFWLocalWakeCntArray);
    }
}
