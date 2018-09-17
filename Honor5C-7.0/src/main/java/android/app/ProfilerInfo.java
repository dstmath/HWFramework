package android.app;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ProfilerInfo implements Parcelable {
    public static final Creator<ProfilerInfo> CREATOR = null;
    public final boolean autoStopProfiler;
    public ParcelFileDescriptor profileFd;
    public final String profileFile;
    public final int samplingInterval;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.ProfilerInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.ProfilerInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.ProfilerInfo.<clinit>():void");
    }

    public ProfilerInfo(String filename, ParcelFileDescriptor fd, int interval, boolean autoStop) {
        this.profileFile = filename;
        this.profileFd = fd;
        this.samplingInterval = interval;
        this.autoStopProfiler = autoStop;
    }

    public int describeContents() {
        if (this.profileFd != null) {
            return this.profileFd.describeContents();
        }
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 1;
        out.writeString(this.profileFile);
        if (this.profileFd != null) {
            out.writeInt(1);
            this.profileFd.writeToParcel(out, flags);
        } else {
            out.writeInt(0);
        }
        out.writeInt(this.samplingInterval);
        if (!this.autoStopProfiler) {
            i = 0;
        }
        out.writeInt(i);
    }

    private ProfilerInfo(Parcel in) {
        boolean z;
        this.profileFile = in.readString();
        this.profileFd = in.readInt() != 0 ? (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(in) : null;
        this.samplingInterval = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.autoStopProfiler = z;
    }
}
