package android.os.storage;

import android.content.Context;
import android.content.Intent;
import android.net.ProxyInfo;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Trace;
import android.os.UserHandle;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.io.CharArrayWriter;
import java.io.File;

public final class StorageVolume implements Parcelable {
    private static final String ACTION_OPEN_EXTERNAL_DIRECTORY = "android.os.storage.action.OPEN_EXTERNAL_DIRECTORY";
    public static final Creator<StorageVolume> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.storage.StorageVolume.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.storage.StorageVolume.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.storage.StorageVolume.<clinit>():void");
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
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mPrimary = z;
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
        return (int) (this.mMtpReserveSize / Trace.TRACE_TAG_DATABASE);
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
        if ((isPrimary() && directoryName == null) || (directoryName != null && !Environment.isStandardDirectory(directoryName))) {
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
        return STORAGE_ID_INVALID;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2 = 1;
        parcel.writeString(this.mId);
        parcel.writeInt(this.mStorageId);
        parcel.writeString(this.mPath.toString());
        parcel.writeString(this.mDescription);
        if (this.mPrimary) {
            i = 1;
        } else {
            i = STORAGE_ID_INVALID;
        }
        parcel.writeInt(i);
        if (this.mRemovable) {
            i = 1;
        } else {
            i = STORAGE_ID_INVALID;
        }
        parcel.writeInt(i);
        if (this.mEmulated) {
            i = 1;
        } else {
            i = STORAGE_ID_INVALID;
        }
        parcel.writeInt(i);
        parcel.writeLong(this.mMtpReserveSize);
        if (!this.mAllowMassStorage) {
            i2 = STORAGE_ID_INVALID;
        }
        parcel.writeInt(i2);
        parcel.writeLong(this.mMaxFileSize);
        parcel.writeParcelable(this.mOwner, flags);
        parcel.writeString(this.mFsUuid);
        parcel.writeString(this.mState);
    }
}
