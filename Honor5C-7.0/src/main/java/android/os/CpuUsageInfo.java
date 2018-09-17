package android.os;

import android.os.Parcelable.Creator;

public final class CpuUsageInfo implements Parcelable {
    public static final Creator<CpuUsageInfo> CREATOR = null;
    private long mActive;
    private long mTotal;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.CpuUsageInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.CpuUsageInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.CpuUsageInfo.<clinit>():void");
    }

    public CpuUsageInfo(long activeTime, long totalTime) {
        this.mActive = activeTime;
        this.mTotal = totalTime;
    }

    private CpuUsageInfo(Parcel in) {
        readFromParcel(in);
    }

    public long getActive() {
        return this.mActive;
    }

    public long getTotal() {
        return this.mTotal;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mActive);
        out.writeLong(this.mTotal);
    }

    private void readFromParcel(Parcel in) {
        this.mActive = in.readLong();
        this.mTotal = in.readLong();
    }
}
