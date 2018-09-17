package android.net.wifi.p2p;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.net.InetAddress;

public class WifiP2pInfo implements Parcelable {
    public static final Creator<WifiP2pInfo> CREATOR = null;
    public boolean groupFormed;
    public InetAddress groupOwnerAddress;
    public boolean isGroupOwner;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.WifiP2pInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.WifiP2pInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pInfo.<clinit>():void");
    }

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
        if (this.groupFormed) {
            b = (byte) 1;
        } else {
            b = (byte) 0;
        }
        dest.writeByte(b);
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
