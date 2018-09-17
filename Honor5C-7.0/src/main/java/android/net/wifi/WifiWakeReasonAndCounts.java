package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class WifiWakeReasonAndCounts implements Parcelable {
    public static final Creator<WifiWakeReasonAndCounts> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiWakeReasonAndCounts.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiWakeReasonAndCounts.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiWakeReasonAndCounts.<clinit>():void");
    }

    public String toString() {
        int i;
        StringBuffer sb = new StringBuffer();
        sb.append(" totalCmdEventWake ").append(this.totalCmdEventWake);
        sb.append(" totalDriverFwLocalWake ").append(this.totalDriverFwLocalWake);
        sb.append(" totalRxDataWake ").append(this.totalRxDataWake);
        sb.append(" rxUnicast ").append(this.rxUnicast);
        sb.append(" rxMulticast ").append(this.rxMulticast);
        sb.append(" rxBroadcast ").append(this.rxBroadcast);
        sb.append(" icmp ").append(this.icmp);
        sb.append(" icmp6 ").append(this.icmp6);
        sb.append(" icmp6Ra ").append(this.icmp6Ra);
        sb.append(" icmp6Na ").append(this.icmp6Na);
        sb.append(" icmp6Ns ").append(this.icmp6Ns);
        sb.append(" ipv4RxMulticast ").append(this.ipv4RxMulticast);
        sb.append(" ipv6Multicast ").append(this.ipv6Multicast);
        sb.append(" otherRxMulticast ").append(this.otherRxMulticast);
        for (i = 0; i < this.cmdEventWakeCntArray.length; i++) {
            sb.append(" cmdEventWakeCntArray[" + i + "] " + this.cmdEventWakeCntArray[i]);
        }
        for (i = 0; i < this.driverFWLocalWakeCntArray.length; i++) {
            sb.append(" driverFWLocalWakeCntArray[" + i + "] " + this.driverFWLocalWakeCntArray[i]);
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
