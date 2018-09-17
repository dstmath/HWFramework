package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public class NetworkKey implements Parcelable {
    public static final Creator<NetworkKey> CREATOR = null;
    public static final int TYPE_WIFI = 1;
    public final int type;
    public final WifiKey wifiKey;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkKey.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkKey.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkKey.<clinit>():void");
    }

    public NetworkKey(WifiKey wifiKey) {
        this.type = TYPE_WIFI;
        this.wifiKey = wifiKey;
    }

    private NetworkKey(Parcel in) {
        this.type = in.readInt();
        switch (this.type) {
            case TYPE_WIFI /*1*/:
                this.wifiKey = (WifiKey) WifiKey.CREATOR.createFromParcel(in);
            default:
                throw new IllegalArgumentException("Parcel has unknown type: " + this.type);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.type);
        switch (this.type) {
            case TYPE_WIFI /*1*/:
                this.wifiKey.writeToParcel(out, flags);
            default:
                throw new IllegalStateException("NetworkKey has unknown type " + this.type);
        }
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkKey that = (NetworkKey) o;
        if (this.type == that.type) {
            z = Objects.equals(this.wifiKey, that.wifiKey);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.type), this.wifiKey});
    }

    public String toString() {
        switch (this.type) {
            case TYPE_WIFI /*1*/:
                return this.wifiKey.toString();
            default:
                return "InvalidKey";
        }
    }
}
