package android.content.pm;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class KeySet implements Parcelable {
    public static final Creator<KeySet> CREATOR = null;
    private IBinder token;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.KeySet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.KeySet.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.KeySet.<clinit>():void");
    }

    public KeySet(IBinder token) {
        if (token == null) {
            throw new NullPointerException("null value for KeySet IBinder token");
        }
        this.token = token;
    }

    public IBinder getToken() {
        return this.token;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof KeySet)) {
            return false;
        }
        if (this.token == ((KeySet) o).token) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.token.hashCode();
    }

    private static KeySet readFromParcel(Parcel in) {
        return new KeySet(in.readStrongBinder());
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(this.token);
    }

    public int describeContents() {
        return 0;
    }
}
