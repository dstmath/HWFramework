package android.net.wifi.p2p.nsd;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.List;

public class WifiP2pServiceInfo implements Parcelable {
    public static final Creator<WifiP2pServiceInfo> CREATOR = null;
    public static final int SERVICE_TYPE_ALL = 0;
    public static final int SERVICE_TYPE_BONJOUR = 1;
    public static final int SERVICE_TYPE_UPNP = 2;
    public static final int SERVICE_TYPE_VENDOR_SPECIFIC = 255;
    public static final int SERVICE_TYPE_WS_DISCOVERY = 3;
    private List<String> mQueryList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.nsd.WifiP2pServiceInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.nsd.WifiP2pServiceInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.nsd.WifiP2pServiceInfo.<clinit>():void");
    }

    protected WifiP2pServiceInfo(List<String> queryList) {
        if (queryList == null) {
            throw new IllegalArgumentException("query list cannot be null");
        }
        this.mQueryList = queryList;
    }

    public List<String> getSupplicantQueryList() {
        return this.mQueryList;
    }

    static String bin2HexStr(byte[] data) {
        StringBuffer sb = new StringBuffer();
        int i = SERVICE_TYPE_ALL;
        int length = data.length;
        while (i < length) {
            try {
                String s = Integer.toHexString(data[i] & SERVICE_TYPE_VENDOR_SPECIFIC);
                if (s.length() == SERVICE_TYPE_BONJOUR) {
                    sb.append('0');
                }
                sb.append(s);
                i += SERVICE_TYPE_BONJOUR;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WifiP2pServiceInfo)) {
            return false;
        }
        return this.mQueryList.equals(((WifiP2pServiceInfo) o).mQueryList);
    }

    public int hashCode() {
        return (this.mQueryList == null ? SERVICE_TYPE_ALL : this.mQueryList.hashCode()) + 527;
    }

    public int describeContents() {
        return SERVICE_TYPE_ALL;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.mQueryList);
    }
}
