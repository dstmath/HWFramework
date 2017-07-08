package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UidTraffic implements Cloneable, Parcelable {
    public static final Creator<UidTraffic> CREATOR = null;
    private final int mAppUid;
    private long mRxBytes;
    private long mTxBytes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.bluetooth.UidTraffic.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.bluetooth.UidTraffic.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.bluetooth.UidTraffic.<clinit>():void");
    }

    public UidTraffic(int appUid) {
        this.mAppUid = appUid;
    }

    public UidTraffic(int appUid, long rx, long tx) {
        this.mAppUid = appUid;
        this.mRxBytes = rx;
        this.mTxBytes = tx;
    }

    UidTraffic(Parcel in) {
        this.mAppUid = in.readInt();
        this.mRxBytes = in.readLong();
        this.mTxBytes = in.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAppUid);
        dest.writeLong(this.mRxBytes);
        dest.writeLong(this.mTxBytes);
    }

    public void setRxBytes(long bytes) {
        this.mRxBytes = bytes;
    }

    public void setTxBytes(long bytes) {
        this.mTxBytes = bytes;
    }

    public void addRxBytes(long bytes) {
        this.mRxBytes += bytes;
    }

    public void addTxBytes(long bytes) {
        this.mTxBytes += bytes;
    }

    public int getUid() {
        return this.mAppUid;
    }

    public long getRxBytes() {
        return this.mRxBytes;
    }

    public long getTxBytes() {
        return this.mTxBytes;
    }

    public int describeContents() {
        return 0;
    }

    public UidTraffic clone() {
        return new UidTraffic(this.mAppUid, this.mRxBytes, this.mTxBytes);
    }

    public String toString() {
        return "UidTraffic{mAppUid=" + this.mAppUid + ", mRxBytes=" + this.mRxBytes + ", mTxBytes=" + this.mTxBytes + '}';
    }
}
