package android.mtp;

import android.content.Context;
import android.os.storage.StorageVolume;

public class MtpStorage {
    private final String mDescription;
    private final long mMaxFileSize;
    private final String mPath;
    private final boolean mRemovable;
    private final long mReserveSpace;
    private final int mStorageId;

    public MtpStorage(StorageVolume volume, Context context) {
        this.mStorageId = volume.getStorageId();
        this.mPath = volume.getPath();
        this.mDescription = volume.getDescription(context);
        this.mReserveSpace = (((long) volume.getMtpReserveSpace()) * 1024) * 1024;
        this.mRemovable = volume.isRemovable();
        this.mMaxFileSize = volume.getMaxFileSize();
    }

    public final int getStorageId() {
        return this.mStorageId;
    }

    public final String getPath() {
        return this.mPath;
    }

    public final String getDescription() {
        return this.mDescription;
    }

    public final long getReserveSpace() {
        return this.mReserveSpace;
    }

    public final boolean isRemovable() {
        return this.mRemovable;
    }

    public long getMaxFileSize() {
        return this.mMaxFileSize;
    }
}
