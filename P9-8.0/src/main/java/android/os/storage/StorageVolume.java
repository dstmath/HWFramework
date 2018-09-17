package android.os.storage;

import android.content.Context;
import android.content.Intent;
import android.net.ProxyInfo;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.io.CharArrayWriter;
import java.io.File;

public final class StorageVolume implements Parcelable {
    private static final String ACTION_OPEN_EXTERNAL_DIRECTORY = "android.os.storage.action.OPEN_EXTERNAL_DIRECTORY";
    public static final Creator<StorageVolume> CREATOR = new Creator<StorageVolume>() {
        public StorageVolume createFromParcel(Parcel in) {
            return new StorageVolume(in, null);
        }

        public StorageVolume[] newArray(int size) {
            return new StorageVolume[size];
        }
    };
    public static final String EXTRA_DIRECTORY_NAME = "android.os.storage.extra.DIRECTORY_NAME";
    public static final String EXTRA_STORAGE_VOLUME = "android.os.storage.extra.STORAGE_VOLUME";
    public static final int STORAGE_ID_INVALID = 0;
    public static final int STORAGE_ID_PRIMARY = 65537;
    private final boolean mAllowMassStorage;
    private final String mDescription;
    private final boolean mEmulated;
    private final String mFsUuid;
    private final String mId;
    private final long mMaxFileSize;
    private final long mMtpReserveSize;
    private final UserHandle mOwner;
    private final File mPath;
    private final boolean mPrimary;
    private final boolean mRemovable;
    private final String mState;
    private final int mStorageId;

    /* synthetic */ StorageVolume(Parcel in, StorageVolume -this1) {
        this(in);
    }

    public StorageVolume(String id, int storageId, File path, String description, boolean primary, boolean removable, boolean emulated, long mtpReserveSize, boolean allowMassStorage, long maxFileSize, UserHandle owner, String fsUuid, String state) {
        this.mId = (String) Preconditions.checkNotNull(id);
        this.mStorageId = storageId;
        this.mPath = (File) Preconditions.checkNotNull(path);
        this.mDescription = (String) Preconditions.checkNotNull(description);
        this.mPrimary = primary;
        this.mRemovable = removable;
        this.mEmulated = emulated;
        this.mMtpReserveSize = mtpReserveSize;
        this.mAllowMassStorage = allowMassStorage;
        this.mMaxFileSize = maxFileSize;
        this.mOwner = (UserHandle) Preconditions.checkNotNull(owner);
        this.mFsUuid = fsUuid;
        this.mState = (String) Preconditions.checkNotNull(state);
    }

    private StorageVolume(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mId = in.readString();
        this.mStorageId = in.readInt();
        this.mPath = new File(in.readString());
        this.mDescription = in.readString();
        this.mPrimary = in.readInt() != 0;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mRemovable = z;
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mEmulated = z;
        this.mMtpReserveSize = in.readLong();
        if (in.readInt() == 0) {
            z2 = false;
        }
        this.mAllowMassStorage = z2;
        this.mMaxFileSize = in.readLong();
        this.mOwner = (UserHandle) in.readParcelable(null);
        this.mFsUuid = in.readString();
        this.mState = in.readString();
    }

    public String getId() {
        return this.mId;
    }

    public String getPath() {
        return this.mPath.toString();
    }

    public File getPathFile() {
        return this.mPath;
    }

    public String getDescription(Context context) {
        return this.mDescription;
    }

    public boolean isPrimary() {
        return this.mPrimary;
    }

    public boolean isRemovable() {
        return this.mRemovable;
    }

    public boolean isEmulated() {
        return this.mEmulated;
    }

    public int getStorageId() {
        return this.mStorageId;
    }

    public int getMtpReserveSpace() {
        return (int) (this.mMtpReserveSize / 1048576);
    }

    public boolean allowMassStorage() {
        return this.mAllowMassStorage;
    }

    public long getMaxFileSize() {
        return this.mMaxFileSize;
    }

    public UserHandle getOwner() {
        return this.mOwner;
    }

    public String getUuid() {
        return this.mFsUuid;
    }

    public int getFatVolumeId() {
        if (this.mFsUuid == null || this.mFsUuid.length() != 9) {
            return -1;
        }
        try {
            return (int) Long.parseLong(this.mFsUuid.replace("-", ProxyInfo.LOCAL_EXCL_LIST), 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getUserLabel() {
        return this.mDescription;
    }

    public String getState() {
        return this.mState;
    }

    public Intent createAccessIntent(String directoryName) {
        if ((isPrimary() && directoryName == null) || (directoryName != null && (Environment.isStandardDirectory(directoryName) ^ 1) != 0)) {
            return null;
        }
        Intent intent = new Intent(ACTION_OPEN_EXTERNAL_DIRECTORY);
        intent.putExtra(EXTRA_STORAGE_VOLUME, (Parcelable) this);
        intent.putExtra(EXTRA_DIRECTORY_NAME, directoryName);
        return intent;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof StorageVolume) || this.mPath == null) {
            return false;
        }
        return this.mPath.equals(((StorageVolume) obj).mPath);
    }

    public int hashCode() {
        return this.mPath.hashCode();
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder("StorageVolume: ").append(this.mDescription);
        if (this.mFsUuid != null) {
            buffer.append(" (").append(this.mFsUuid).append(")");
        }
        return buffer.toString();
    }

    public String dump() {
        CharArrayWriter writer = new CharArrayWriter();
        dump(new IndentingPrintWriter(writer, "    ", 80));
        return writer.toString();
    }

    public void dump(IndentingPrintWriter pw) {
        pw.println("StorageVolume:");
        pw.increaseIndent();
        pw.printPair("mId", this.mId);
        pw.printPair("mStorageId", Integer.valueOf(this.mStorageId));
        pw.printPair("mPath", this.mPath);
        pw.printPair("mDescription", this.mDescription);
        pw.printPair("mPrimary", Boolean.valueOf(this.mPrimary));
        pw.printPair("mRemovable", Boolean.valueOf(this.mRemovable));
        pw.printPair("mEmulated", Boolean.valueOf(this.mEmulated));
        pw.printPair("mMtpReserveSize", Long.valueOf(this.mMtpReserveSize));
        pw.printPair("mAllowMassStorage", Boolean.valueOf(this.mAllowMassStorage));
        pw.printPair("mMaxFileSize", Long.valueOf(this.mMaxFileSize));
        pw.printPair("mOwner", this.mOwner);
        pw.printPair("mFsUuid", this.mFsUuid);
        pw.printPair("mState", this.mState);
        pw.decreaseIndent();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2 = 1;
        parcel.writeString(this.mId);
        parcel.writeInt(this.mStorageId);
        parcel.writeString(this.mPath.toString());
        parcel.writeString(this.mDescription);
        parcel.writeInt(this.mPrimary ? 1 : 0);
        if (this.mRemovable) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mEmulated) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        parcel.writeLong(this.mMtpReserveSize);
        if (!this.mAllowMassStorage) {
            i2 = 0;
        }
        parcel.writeInt(i2);
        parcel.writeLong(this.mMaxFileSize);
        parcel.writeParcelable(this.mOwner, flags);
        parcel.writeString(this.mFsUuid);
        parcel.writeString(this.mState);
    }
}
