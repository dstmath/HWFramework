package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;
import java.util.regex.Pattern;

public class WifiKey implements Parcelable {
    private static final Pattern BSSID_PATTERN = null;
    public static final Creator<WifiKey> CREATOR = null;
    private static final Pattern SSID_PATTERN = null;
    public final String bssid;
    public final String ssid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.WifiKey.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.WifiKey.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.WifiKey.<clinit>():void");
    }

    public WifiKey(String ssid, String bssid) {
        if (!SSID_PATTERN.matcher(ssid).matches()) {
            throw new IllegalArgumentException("Invalid ssid: " + ssid);
        } else if (BSSID_PATTERN.matcher(bssid).matches()) {
            this.ssid = ssid;
            this.bssid = bssid;
        } else {
            throw new IllegalArgumentException("Invalid bssid: " + bssid);
        }
    }

    private WifiKey(Parcel in) {
        this.ssid = in.readString();
        this.bssid = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.ssid);
        out.writeString(this.bssid);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WifiKey wifiKey = (WifiKey) o;
        if (Objects.equals(this.ssid, wifiKey.ssid)) {
            z = Objects.equals(this.bssid, wifiKey.bssid);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.ssid, this.bssid});
    }

    public String toString() {
        return "WifiKey[SSID=" + this.ssid + ",BSSID=" + this.bssid + "]";
    }
}
