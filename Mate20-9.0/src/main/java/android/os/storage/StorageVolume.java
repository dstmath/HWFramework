package android.os.storage;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.io.CharArrayWriter;
import java.io.File;

public final class StorageVolume implements Parcelable {
    private static final String ACTION_OPEN_EXTERNAL_DIRECTORY = "android.os.storage.action.OPEN_EXTERNAL_DIRECTORY";
    public static final Parcelable.Creator<StorageVolume> CREATOR = new Parcelable.Creator<StorageVolume>() {
        public StorageVolume createFromParcel(Parcel in) {
            return new StorageVolume(in);
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
    private final File mInternalPath;
    private final long mMaxFileSize;
    private final UserHandle mOwner;
    private final File mPath;
    private final boolean mPrimary;
    private final boolean mRemovable;
    private final String mState;

    public static final class ScopedAccessProviderContract {
        public static final String AUTHORITY = "com.android.documentsui.scopedAccess";
        public static final String COL_DIRECTORY = "directory";
        public static final String COL_GRANTED = "granted";
        public static final String COL_PACKAGE = "package_name";
        public static final String COL_VOLUME_UUID = "volume_uuid";
        public static final String TABLE_PACKAGES = "packages";
        public static final String[] TABLE_PACKAGES_COLUMNS = {COL_PACKAGE};
        public static final int TABLE_PACKAGES_COL_PACKAGE = 0;
        public static final String TABLE_PERMISSIONS = "permissions";
        public static final String[] TABLE_PERMISSIONS_COLUMNS = {COL_PACKAGE, COL_VOLUME_UUID, "directory", COL_GRANTED};
        public static final int TABLE_PERMISSIONS_COL_DIRECTORY = 2;
        public static final int TABLE_PERMISSIONS_COL_GRANTED = 3;
        public static final int TABLE_PERMISSIONS_COL_PACKAGE = 0;
        public static final int TABLE_PERMISSIONS_COL_VOLUME_UUID = 1;

        private ScopedAccessProviderContract() {
            throw new UnsupportedOperationException("contains constants only");
        }
    }

    public StorageVolume(String id, File path, File internalPath, String description, boolean primary, boolean removable, boolean emulated, boolean allowMassStorage, long maxFileSize, UserHandle owner, String fsUuid, String state) {
        this.mId = (String) Preconditions.checkNotNull(id);
        this.mPath = (File) Preconditions.checkNotNull(path);
        this.mInternalPath = (File) Preconditions.checkNotNull(internalPath);
        this.mDescription = (String) Preconditions.checkNotNull(description);
        this.mPrimary = primary;
        this.mRemovable = removable;
        this.mEmulated = emulated;
        this.mAllowMassStorage = allowMassStorage;
        this.mMaxFileSize = maxFileSize;
        this.mOwner = (UserHandle) Preconditions.checkNotNull(owner);
        this.mFsUuid = fsUuid;
        this.mState = (String) Preconditions.checkNotNull(state);
    }

    private StorageVolume(Parcel in) {
        this.mId = in.readString();
        this.mPath = new File(in.readString());
        this.mInternalPath = new File(in.readString());
        this.mDescription = in.readString();
        boolean z = false;
        this.mPrimary = in.readInt() != 0;
        this.mRemovable = in.readInt() != 0;
        this.mEmulated = in.readInt() != 0;
        this.mAllowMassStorage = in.readInt() != 0 ? true : z;
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

    public String getInternalPath() {
        return this.mInternalPath.toString();
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
            return (int) Long.parseLong(this.mFsUuid.replace("-", ""), 16);
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
        if ((isPrimary() && directoryName == null) || (directoryName != null && !Environment.isStandardDirectory(directoryName))) {
            return null;
        }
        Intent intent = new Intent(ACTION_OPEN_EXTERNAL_DIRECTORY);
        intent.putExtra(EXTRA_STORAGE_VOLUME, this);
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
            buffer.append(" (");
            buffer.append(this.mFsUuid);
            buffer.append(")");
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
        pw.printPair("mPath", this.mPath);
        pw.printPair("mInternalPath", this.mInternalPath);
        pw.printPair("mDescription", this.mDescription);
        pw.printPair("mPrimary", Boolean.valueOf(this.mPrimary));
        pw.printPair("mRemovable", Boolean.valueOf(this.mRemovable));
        pw.printPair("mEmulated", Boolean.valueOf(this.mEmulated));
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
        parcel.writeString(this.mId);
        parcel.writeString(this.mPath.toString());
        parcel.writeString(this.mInternalPath.toString());
        parcel.writeString(this.mDescription);
        parcel.writeInt(this.mPrimary ? 1 : 0);
        parcel.writeInt(this.mRemovable ? 1 : 0);
        parcel.writeInt(this.mEmulated ? 1 : 0);
        parcel.writeInt(this.mAllowMassStorage ? 1 : 0);
        parcel.writeLong(this.mMaxFileSize);
        parcel.writeParcelable(this.mOwner, flags);
        parcel.writeString(this.mFsUuid);
        parcel.writeString(this.mState);
    }
}
