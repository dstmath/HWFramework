package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class WifiNetworkConnectionStatistics implements Parcelable {
    public static final Creator<WifiNetworkConnectionStatistics> CREATOR = null;
    private static final String TAG = "WifiNetworkConnnectionStatistics";
    public int numConnection;
    public int numUsage;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WifiNetworkConnectionStatistics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WifiNetworkConnectionStatistics.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiNetworkConnectionStatistics.<clinit>():void");
    }

    public WifiNetworkConnectionStatistics(int connection, int usage) {
        this.numConnection = connection;
        this.numUsage = usage;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("c=").append(this.numConnection);
        sbuf.append(" u=").append(this.numUsage);
        return sbuf.toString();
    }

    public WifiNetworkConnectionStatistics(WifiNetworkConnectionStatistics source) {
        this.numConnection = source.numConnection;
        this.numUsage = source.numUsage;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.numConnection);
        dest.writeInt(this.numUsage);
    }
}
