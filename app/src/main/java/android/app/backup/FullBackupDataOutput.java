package android.app.backup;

import android.os.ParcelFileDescriptor;

public class FullBackupDataOutput {
    private final BackupDataOutput mData;
    private long mSize;

    public FullBackupDataOutput() {
        this.mData = null;
        this.mSize = 0;
    }

    public FullBackupDataOutput(ParcelFileDescriptor fd) {
        this.mData = new BackupDataOutput(fd.getFileDescriptor());
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
