package android.os.storage;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.DebugUtils;
import com.android.internal.R;
import com.android.internal.app.DumpHeapActivity;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.io.CharArrayWriter;
import java.util.Objects;

public class DiskInfo implements Parcelable {
    public static final String ACTION_DISK_SCANNED = "android.os.storage.action.DISK_SCANNED";
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static final Parcelable.Creator<DiskInfo> CREATOR = new Parcelable.Creator<DiskInfo>() {
        /* class android.os.storage.DiskInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DiskInfo createFromParcel(Parcel in) {
            return new DiskInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public DiskInfo[] newArray(int size) {
            return new DiskInfo[size];
        }
    };
    public static final String EXTRA_DISK_ID = "android.os.storage.extra.DISK_ID";
    public static final String EXTRA_VOLUME_COUNT = "android.os.storage.extra.VOLUME_COUNT";
    public static final int FLAG_ADOPTABLE = 1;
    public static final int FLAG_DEFAULT_PRIMARY = 2;
    public static final int FLAG_HONOR_EARL = 128;
    public static final int FLAG_HUAWEI_EARL = 64;
    public static final int FLAG_SD = 4;
    public static final int FLAG_USB = 8;
    @UnsupportedAppUsage
    public final int flags;
    public final String id;
    @UnsupportedAppUsage
    public String label;
    @UnsupportedAppUsage
    public long size;
    public String sysPath;
    public int volumeCount;

    public DiskInfo(String id2, int flags2) {
        this.id = (String) Preconditions.checkNotNull(id2);
        this.flags = flags2;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public DiskInfo(Parcel parcel) {
        this.id = parcel.readString();
        this.flags = parcel.readInt();
        this.size = parcel.readLong();
        this.label = parcel.readString();
        this.volumeCount = parcel.readInt();
        this.sysPath = parcel.readString();
    }

    @UnsupportedAppUsage
    public String getId() {
        return this.id;
    }

    private boolean isInteresting(String label2) {
        if (!TextUtils.isEmpty(label2) && !label2.equalsIgnoreCase("ata") && !label2.toLowerCase().contains("generic") && !label2.toLowerCase().startsWith(Context.USB_SERVICE) && !label2.toLowerCase().startsWith("multiple")) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    public String getDescription() {
        Resources res = Resources.getSystem();
        int i = this.flags;
        if ((i & 4) != 0) {
            if (isInteresting(this.label)) {
                return res.getString(R.string.storage_sd_card_label, this.label);
            }
            return res.getString(R.string.storage_sd_card);
        } else if ((i & 8) == 0) {
            return null;
        } else {
            if ((i & 64) != 0) {
                return res.getString(com.android.hwext.internal.R.string.hw_storage_usb_earl);
            }
            if ((i & 128) != 0) {
                return res.getString(com.android.hwext.internal.R.string.hw_storage_usb_honor);
            }
            if (isInteresting(this.label)) {
                return res.getString(R.string.storage_usb_drive_label, this.label);
            }
            return res.getString(R.string.storage_usb_drive);
        }
    }

    public String getShortDescription() {
        Resources res = Resources.getSystem();
        if (isSd()) {
            return res.getString(R.string.storage_sd_card);
        }
        if (isUsb()) {
            return res.getString(R.string.storage_usb_drive);
        }
        return null;
    }

    @UnsupportedAppUsage
    public boolean isAdoptable() {
        return (this.flags & 1) != 0;
    }

    @UnsupportedAppUsage
    public boolean isDefaultPrimary() {
        return (this.flags & 2) != 0;
    }

    @UnsupportedAppUsage
    public boolean isSd() {
        return (this.flags & 4) != 0;
    }

    @UnsupportedAppUsage
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
        pw.printPair(DumpHeapActivity.KEY_SIZE, Long.valueOf(this.size));
        pw.printPair("label", this.label);
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
            return CREATOR.createFromParcel(temp);
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags2) {
        parcel.writeString(this.id);
        parcel.writeInt(this.flags);
        parcel.writeLong(this.size);
        parcel.writeString(this.label);
        parcel.writeInt(this.volumeCount);
        parcel.writeString(this.sysPath);
    }
}
