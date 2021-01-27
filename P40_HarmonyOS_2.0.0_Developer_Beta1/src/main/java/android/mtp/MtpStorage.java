package android.mtp;

import android.annotation.UnsupportedAppUsage;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;

public class MtpStorage {
    private final String mDescription;
    private final long mMaxFileSize;
    private final String mPath;
    private final boolean mRemovable;
    private final int mStorageId;
    private final String mVolumeName;

    public MtpStorage(StorageVolume volume, int storageId) {
        this.mStorageId = storageId;
        this.mPath = volume.getInternalPath();
        this.mDescription = volume.getDescription(null);
        this.mRemovable = volume.isRemovable();
        this.mMaxFileSize = volume.getMaxFileSize();
        if (volume.isPrimary()) {
            this.mVolumeName = MediaStore.VOLUME_EXTERNAL_PRIMARY;
        } else {
            this.mVolumeName = volume.getNormalizedUuid();
        }
    }

    @UnsupportedAppUsage
    public final int getStorageId() {
        return this.mStorageId;
    }

    @UnsupportedAppUsage
    public final String getPath() {
        return this.mPath;
    }

    public final String getDescription() {
        return this.mDescription;
    }

    public final boolean isRemovable() {
        return this.mRemovable;
    }

    public long getMaxFileSize() {
        return this.mMaxFileSize;
    }

    public String getVolumeName() {
        return this.mVolumeName;
    }
}
