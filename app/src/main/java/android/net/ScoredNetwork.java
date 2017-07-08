package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public class ScoredNetwork implements Parcelable {
    public static final Creator<ScoredNetwork> CREATOR = null;
    public final boolean meteredHint;
    public final NetworkKey networkKey;
    public final RssiCurve rssiCurve;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.ScoredNetwork.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.ScoredNetwork.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.ScoredNetwork.<clinit>():void");
    }

    public ScoredNetwork(NetworkKey networkKey, RssiCurve rssiCurve) {
        this(networkKey, rssiCurve, false);
    }

    public ScoredNetwork(NetworkKey networkKey, RssiCurve rssiCurve, boolean meteredHint) {
        this.networkKey = networkKey;
        this.rssiCurve = rssiCurve;
        this.meteredHint = meteredHint;
    }

    private ScoredNetwork(Parcel in) {
        boolean z;
        this.networkKey = (NetworkKey) NetworkKey.CREATOR.createFromParcel(in);
        if (in.readByte() == (byte) 1) {
            this.rssiCurve = (RssiCurve) RssiCurve.CREATOR.createFromParcel(in);
        } else {
            this.rssiCurve = null;
        }
        if (in.readByte() != null) {
            z = true;
        } else {
            z = false;
        }
        this.meteredHint = z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 1;
        this.networkKey.writeToParcel(out, flags);
        if (this.rssiCurve != null) {
            out.writeByte((byte) 1);
            this.rssiCurve.writeToParcel(out, flags);
        } else {
            out.writeByte((byte) 0);
        }
        if (!this.meteredHint) {
            i = 0;
        }
        out.writeByte((byte) i);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScoredNetwork that = (ScoredNetwork) o;
        if (Objects.equals(this.networkKey, that.networkKey) && Objects.equals(this.rssiCurve, that.rssiCurve)) {
            z = Objects.equals(Boolean.valueOf(this.meteredHint), Boolean.valueOf(that.meteredHint));
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.networkKey, this.rssiCurve, Boolean.valueOf(this.meteredHint)});
    }

    public String toString() {
        return "ScoredNetwork[key=" + this.networkKey + ",score=" + this.rssiCurve + ",meteredHint=" + this.meteredHint + "]";
    }
}
