package android.app.backup;

import android.os.ParcelFileDescriptor;

public class FullBackupDataOutput {
    private final BackupDataOutput mData;
    private final long mQuota;
    private long mSize;
    private final int mTransportFlags;

    public long getQuota() {
        return this.mQuota;
    }

    public int getTransportFlags() {
        return this.mTransportFlags;
    }

    public FullBackupDataOutput(long quota) {
        this.mData = null;
        this.mQuota = quota;
        this.mSize = 0;
        this.mTransportFlags = 0;
    }

    public FullBackupDataOutput(long quota, int transportFlags) {
        this.mData = null;
        this.mQuota = quota;
        this.mSize = 0;
        this.mTransportFlags = transportFlags;
    }

    public FullBackupDataOutput(ParcelFileDescriptor fd, long quota) {
        this.mData = new BackupDataOutput(fd.getFileDescriptor(), quota, 0);
        this.mQuota = quota;
        this.mTransportFlags = 0;
    }

    public FullBackupDataOutput(ParcelFileDescriptor fd, long quota, int transportFlags) {
        this.mData = new BackupDataOutput(fd.getFileDescriptor(), quota, transportFlags);
        this.mQuota = quota;
        this.mTransportFlags = transportFlags;
    }

    public FullBackupDataOutput(ParcelFileDescriptor fd) {
        this(fd, -1, 0);
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
