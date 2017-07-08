package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NetworkQuotaInfo implements Parcelable {
    public static final Creator<NetworkQuotaInfo> CREATOR = null;
    public static final long NO_LIMIT = -1;
    private final long mEstimatedBytes;
    private final long mHardLimitBytes;
    private final long mSoftLimitBytes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkQuotaInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkQuotaInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkQuotaInfo.<clinit>():void");
    }

    public NetworkQuotaInfo(long estimatedBytes, long softLimitBytes, long hardLimitBytes) {
        this.mEstimatedBytes = estimatedBytes;
        this.mSoftLimitBytes = softLimitBytes;
        this.mHardLimitBytes = hardLimitBytes;
    }

    public NetworkQuotaInfo(Parcel in) {
        this.mEstimatedBytes = in.readLong();
        this.mSoftLimitBytes = in.readLong();
        this.mHardLimitBytes = in.readLong();
    }

    public long getEstimatedBytes() {
        return this.mEstimatedBytes;
    }

    public long getSoftLimitBytes() {
        return this.mSoftLimitBytes;
    }

    public long getHardLimitBytes() {
        return this.mHardLimitBytes;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mEstimatedBytes);
        out.writeLong(this.mSoftLimitBytes);
        out.writeLong(this.mHardLimitBytes);
    }
}
