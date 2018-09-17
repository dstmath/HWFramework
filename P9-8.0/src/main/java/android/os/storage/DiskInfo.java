package android.os.storage;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PerformanceCollector;
import android.text.TextUtils;
import android.util.DebugUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.io.CharArrayWriter;
import java.util.Objects;

public class DiskInfo implements Parcelable {
    public static final String ACTION_DISK_SCANNED = "android.os.storage.action.DISK_SCANNED";
    public static final Creator<DiskInfo> CREATOR = new Creator<DiskInfo>() {
        public DiskInfo createFromParcel(Parcel in) {
            return new DiskInfo(in);
        }

        public DiskInfo[] newArray(int size) {
            return new DiskInfo[size];
        }
    };
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
        if ((this.flags & 4) != 0) {
            if (!isInteresting(this.label)) {
                return res.getString(17041084);
            }
            return res.getString(17041085, this.label);
        } else if ((this.flags & 8) == 0) {
            return null;
        } else {
            if (!isInteresting(this.label)) {
                return res.getString(17041087);
            }
            return res.getString(17041088, this.label);
        }
    }

    public boolean isAdoptable() {
        return (this.flags & 1) != 0;
    }

    public boolean isDefaultPrimary() {
        return (this.flags & 2) != 0;
    }

    public boolean isSd() {
        return (this.flags & 4) != 0;
    }

    public boolean isUsb() {
        return (this.flags & 8) != 0;
    }

    public String toString() {
        CharArrayWriter writer = new CharArrayWriter();
        dump(new IndentingPrintWriter(writer, "    ", 80));
        return writer.toString();
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("DiskInfo{" + this.id + "}:");
        pw.increaseIndent();
        pw.printPair("flags", DebugUtils.flagsToString(getClass(), "FLAG_", this.flags));
        pw.printPair("size", Long.valueOf(this.size));
        pw.printPair(PerformanceCollector.METRIC_KEY_LABEL, this.label);
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
