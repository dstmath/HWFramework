package android.os.storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.DebugUtils;
import android.util.TimeUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.util.Objects;

public class VolumeRecord implements Parcelable {
    public static final Creator<VolumeRecord> CREATOR = new Creator<VolumeRecord>() {
        public VolumeRecord createFromParcel(Parcel in) {
            return new VolumeRecord(in);
        }

        public VolumeRecord[] newArray(int size) {
            return new VolumeRecord[size];
        }
    };
    public static final String EXTRA_FS_UUID = "android.os.storage.extra.FS_UUID";
    public static final int USER_FLAG_INITED = 1;
    public static final int USER_FLAG_SNOOZED = 2;
    public long createdMillis;
    public final String fsUuid;
    public long lastBenchMillis;
    public long lastTrimMillis;
    public String nickname;
    public String partGuid;
    public final int type;
    public int userFlags;

    public VolumeRecord(int type, String fsUuid) {
        this.type = type;
        this.fsUuid = (String) Preconditions.checkNotNull(fsUuid);
    }

    public VolumeRecord(Parcel parcel) {
        this.type = parcel.readInt();
        this.fsUuid = parcel.readString();
        this.partGuid = parcel.readString();
        this.nickname = parcel.readString();
        this.userFlags = parcel.readInt();
        this.createdMillis = parcel.readLong();
        this.lastTrimMillis = parcel.readLong();
        this.lastBenchMillis = parcel.readLong();
    }

    public int getType() {
        return this.type;
    }

    public String getFsUuid() {
        return this.fsUuid;
    }

    public String getNickname() {
        return this.nickname;
    }

    public boolean isInited() {
        return (this.userFlags & 1) != 0;
    }

    public boolean isSnoozed() {
        return (this.userFlags & 2) != 0;
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("VolumeRecord:");
        pw.increaseIndent();
        pw.printPair("type", DebugUtils.valueToString(VolumeInfo.class, "TYPE_", this.type));
        pw.printPair("fsUuid", this.fsUuid);
        pw.printPair("partGuid", this.partGuid);
        pw.println();
        pw.printPair("nickname", this.nickname);
        pw.printPair("userFlags", DebugUtils.flagsToString(VolumeRecord.class, "USER_FLAG_", this.userFlags));
        pw.println();
        pw.printPair("createdMillis", TimeUtils.formatForLogging(this.createdMillis));
        pw.printPair("lastTrimMillis", TimeUtils.formatForLogging(this.lastTrimMillis));
        pw.printPair("lastBenchMillis", TimeUtils.formatForLogging(this.lastBenchMillis));
        pw.decreaseIndent();
        pw.println();
    }

    public VolumeRecord clone() {
        Parcel temp = Parcel.obtain();
        try {
            writeToParcel(temp, 0);
            temp.setDataPosition(0);
            VolumeRecord volumeRecord = (VolumeRecord) CREATOR.createFromParcel(temp);
            return volumeRecord;
        } finally {
            temp.recycle();
        }
    }

    public boolean equals(Object o) {
        if (o instanceof VolumeRecord) {
            return Objects.equals(this.fsUuid, ((VolumeRecord) o).fsUuid);
        }
        return false;
    }

    public int hashCode() {
        return this.fsUuid.hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.type);
        parcel.writeString(this.fsUuid);
        parcel.writeString(this.partGuid);
        parcel.writeString(this.nickname);
        parcel.writeInt(this.userFlags);
        parcel.writeLong(this.createdMillis);
        parcel.writeLong(this.lastTrimMillis);
        parcel.writeLong(this.lastBenchMillis);
    }
}
