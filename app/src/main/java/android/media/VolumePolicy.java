package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class VolumePolicy implements Parcelable {
    public static final Creator<VolumePolicy> CREATOR = null;
    public static final VolumePolicy DEFAULT = null;
    public final boolean doNotDisturbWhenSilent;
    public final int vibrateToSilentDebounce;
    public final boolean volumeDownToEnterSilent;
    public final boolean volumeUpToExitSilent;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.VolumePolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.VolumePolicy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.VolumePolicy.<clinit>():void");
    }

    public VolumePolicy(boolean volumeDownToEnterSilent, boolean volumeUpToExitSilent, boolean doNotDisturbWhenSilent, int vibrateToSilentDebounce) {
        this.volumeDownToEnterSilent = volumeDownToEnterSilent;
        this.volumeUpToExitSilent = volumeUpToExitSilent;
        this.doNotDisturbWhenSilent = doNotDisturbWhenSilent;
        this.vibrateToSilentDebounce = vibrateToSilentDebounce;
    }

    public String toString() {
        return "VolumePolicy[volumeDownToEnterSilent=" + this.volumeDownToEnterSilent + ",volumeUpToExitSilent=" + this.volumeUpToExitSilent + ",doNotDisturbWhenSilent=" + this.doNotDisturbWhenSilent + ",vibrateToSilentDebounce=" + this.vibrateToSilentDebounce + "]";
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Boolean.valueOf(this.volumeDownToEnterSilent), Boolean.valueOf(this.volumeUpToExitSilent), Boolean.valueOf(this.doNotDisturbWhenSilent), Integer.valueOf(this.vibrateToSilentDebounce)});
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (!(o instanceof VolumePolicy)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        VolumePolicy other = (VolumePolicy) o;
        if (other.volumeDownToEnterSilent != this.volumeDownToEnterSilent || other.volumeUpToExitSilent != this.volumeUpToExitSilent || other.doNotDisturbWhenSilent != this.doNotDisturbWhenSilent) {
            z = false;
        } else if (other.vibrateToSilentDebounce != this.vibrateToSilentDebounce) {
            z = false;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        if (this.volumeDownToEnterSilent) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.volumeUpToExitSilent) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.doNotDisturbWhenSilent) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.vibrateToSilentDebounce);
    }
}
