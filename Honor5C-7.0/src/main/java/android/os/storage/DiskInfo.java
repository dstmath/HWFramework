package android.os.storage;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.Downloads.Impl;
import android.text.TextUtils;
import android.util.DebugUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.io.CharArrayWriter;
import java.util.Objects;

public class DiskInfo implements Parcelable {
    public static final String ACTION_DISK_SCANNED = "android.os.storage.action.DISK_SCANNED";
    public static final Creator<DiskInfo> CREATOR = null;
    public static final String EXTRA_DISK_ID = "android.os.storage.extra.DISK_ID";
    public static final String EXTRA_VOLUME_COUNT = "android.os.storage.extra.VOLUME_COUNT";
    public static final int FLAG_ADOPTABLE = 1;
    public static final int FLAG_DEFAULT_PRIMARY = 2;
    public static final int FLAG_SD = 4;
    public static final int FLAG_USB = 8;
    public final int flags;
    public final String id;
    public String label;
    public long size;
    public String sysPath;
    public int volumeCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.storage.DiskInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.storage.DiskInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.storage.DiskInfo.<clinit>():void");
    }

    public DiskInfo(String id, int flags) {
        this.id = (String) Preconditions.checkNotNull(id);
        this.flags = flags;
    }

    public DiskInfo(Parcel parcel) {
        this.id = parcel.readString();
        this.flags = parcel.readInt();
        this.size = parcel.readLong();
        this.label = parcel.readString();
        this.volumeCount = parcel.readInt();
        this.sysPath = parcel.readString();
    }

    public String getId() {
        return this.id;
    }

    private boolean isInteresting(String label) {
        if (TextUtils.isEmpty(label) || label.equalsIgnoreCase("ata") || label.toLowerCase().contains("generic") || label.toLowerCase().startsWith(Context.USB_SERVICE) || label.toLowerCase().startsWith("multiple")) {
            return false;
        }
        return true;
    }

    public String getDescription() {
        Resources res = Resources.getSystem();
        Object[] objArr;
        if ((this.flags & FLAG_SD) != 0) {
            if (!isInteresting(this.label)) {
                return res.getString(17040561);
            }
            objArr = new Object[FLAG_ADOPTABLE];
            objArr[0] = this.label;
            return res.getString(17040562, objArr);
        } else if ((this.flags & FLAG_USB) == 0) {
            return null;
        } else {
            if (!isInteresting(this.label)) {
                return res.getString(17040563);
            }
            objArr = new Object[FLAG_ADOPTABLE];
            objArr[0] = this.label;
            return res.getString(17040564, objArr);
        }
    }

    public boolean isAdoptable() {
        return (this.flags & FLAG_ADOPTABLE) != 0;
    }

    public boolean isDefaultPrimary() {
        return (this.flags & FLAG_DEFAULT_PRIMARY) != 0;
    }

    public boolean isSd() {
        return (this.flags & FLAG_SD) != 0;
    }

    public boolean isUsb() {
        return (this.flags & FLAG_USB) != 0;
    }

    public String toString() {
        CharArrayWriter writer = new CharArrayWriter();
        dump(new IndentingPrintWriter(writer, "    ", 80));
        return writer.toString();
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("DiskInfo{" + this.id + "}:");
        pw.increaseIndent();
        pw.printPair(Impl.COLUMN_FLAGS, DebugUtils.flagsToString(getClass(), "FLAG_", this.flags));
        pw.printPair("size", Long.valueOf(this.size));
        pw.printPair(PhoneLookupColumns.LABEL, this.label);
        pw.println();
        pw.printPair("sysPath", this.sysPath);
        pw.decreaseIndent();
        pw.println();
    }

    public DiskInfo clone() {
        Parcel temp = Parcel.obtain();
        try {
            writeToParcel(temp, 0);
            temp.setDataPosition(0);
            DiskInfo diskInfo = (DiskInfo) CREATOR.createFromParcel(temp);
            return diskInfo;
        } finally {
            temp.recycle();
        }
    }

    public boolean equals(Object o) {
        if (o instanceof DiskInfo) {
            return Objects.equals(this.id, ((DiskInfo) o).id);
        }
        return false;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.id);
        parcel.writeInt(this.flags);
        parcel.writeLong(this.size);
        parcel.writeString(this.label);
        parcel.writeInt(this.volumeCount);
        parcel.writeString(this.sysPath);
    }
}
