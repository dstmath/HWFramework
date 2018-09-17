package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class WpsInfo implements Parcelable {
    public static final Creator<WpsInfo> CREATOR = null;
    public static final int DISPLAY = 1;
    public static final int INVALID = 4;
    public static final int KEYPAD = 2;
    public static final int LABEL = 3;
    public static final int PBC = 0;
    public String BSSID;
    public String pin;
    public int setup;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.WpsInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.WpsInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WpsInfo.<clinit>():void");
    }

    public WpsInfo() {
        this.setup = INVALID;
        this.BSSID = null;
        this.pin = null;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(" setup: ").append(this.setup);
        sbuf.append('\n');
        sbuf.append(" BSSID: ").append(this.BSSID);
        sbuf.append('\n');
        sbuf.append(" pin: ").append(this.pin);
        sbuf.append('\n');
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public WpsInfo(WpsInfo source) {
        if (source != null) {
            this.setup = source.setup;
            this.BSSID = source.BSSID;
            this.pin = source.pin;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.setup);
        dest.writeString(this.BSSID);
        dest.writeString(this.pin);
    }
}
