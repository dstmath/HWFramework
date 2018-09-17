package android.app.backup;

import android.os.ParcelFileDescriptor;

public class FullBackupDataOutput {
    private final BackupDataOutput mData;
    private final long mQuota;
    private long mSize;

    public long getQuota() {
        return this.mQuota;
    }

    public FullBackupDataOutput(long quota) {
        this.mData = null;
        this.mQuota = quota;
        this.mSize = 0;
    }

    public FullBackupDataOutput(ParcelFileDescriptor fd, long quota) {
        this.mData = new BackupDataOutput(fd.getFileDescriptor(), quota);
        this.mQuota = quota;
    }

    public FullBackupDataOutput(ParcelFileDescriptor fd) {
        this(fd, -1);
    }

    public BackupDataOutput getData() {
        return this.mData;
    }

    public void addSize(long size) {
        if (size > 0) {
            this.mSize += size;
        }
    }

    public long getSize() {
        return this.mSize;
    }
}
