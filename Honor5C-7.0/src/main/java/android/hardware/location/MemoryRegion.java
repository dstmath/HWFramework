package android.hardware.location;

import android.app.backup.FullBackup;
import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class MemoryRegion implements Parcelable {
    public static final Creator<MemoryRegion> CREATOR = null;
    private boolean mIsExecutable;
    private boolean mIsReadable;
    private boolean mIsWritable;
    private int mSizeBytes;
    private int mSizeBytesFree;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.location.MemoryRegion.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.location.MemoryRegion.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.MemoryRegion.<clinit>():void");
    }

    public int getCapacityBytes() {
        return this.mSizeBytes;
    }

    public int getFreeCapacityBytes() {
        return this.mSizeBytesFree;
    }

    public boolean isReadable() {
        return this.mIsReadable;
    }

    public boolean isWritable() {
        return this.mIsWritable;
    }

    public boolean isExecutable() {
        return this.mIsExecutable;
    }

    public String toString() {
        String mask = ProxyInfo.LOCAL_EXCL_LIST;
        if (isReadable()) {
            mask = mask + FullBackup.ROOT_TREE_TOKEN;
        } else {
            mask = mask + "-";
        }
        if (isWritable()) {
            mask = mask + "w";
        } else {
            mask = mask + "-";
        }
        if (isExecutable()) {
            mask = mask + "x";
        } else {
            mask = mask + "-";
        }
        return "[ " + this.mSizeBytesFree + "/ " + this.mSizeBytes + " ] : " + mask;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.mSizeBytes);
        dest.writeInt(this.mSizeBytesFree);
        if (this.mIsReadable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mIsWritable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mIsExecutable) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    public MemoryRegion(Parcel source) {
        boolean z;
        boolean z2 = true;
        this.mSizeBytes = source.readInt();
        this.mSizeBytesFree = source.readInt();
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsReadable = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsWritable = z;
        if (source.readInt() == 0) {
            z2 = false;
        }
        this.mIsExecutable = z2;
    }
}
