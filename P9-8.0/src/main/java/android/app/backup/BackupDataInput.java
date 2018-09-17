package android.app.backup;

import java.io.FileDescriptor;
import java.io.IOException;

public class BackupDataInput {
    long mBackupReader;
    private EntityHeader mHeader = new EntityHeader();
    private boolean mHeaderReady;

    private static class EntityHeader {
        int dataSize;
        String key;

        /* synthetic */ EntityHeader(EntityHeader -this0) {
            this();
        }

        private EntityHeader() {
        }
    }

    private static native long ctor(FileDescriptor fileDescriptor);

    private static native void dtor(long j);

    private native int readEntityData_native(long j, byte[] bArr, int i, int i2);

    private native int readNextHeader_native(long j, EntityHeader entityHeader);

    private native int skipEntityData_native(long j);

    public BackupDataInput(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException();
        }
        this.mBackupReader = ctor(fd);
        if (this.mBackupReader == 0) {
            throw new RuntimeException("Native initialization failed with fd=" + fd);
        }
    }

    protected void finalize() throws Throwable {
        try {
            dtor(this.mBackupReader);
        } finally {
            super.finalize();
        }
    }

    public boolean readNextHeader() throws IOException {
        int result = readNextHeader_native(this.mBackupReader, this.mHeader);
        if (result == 0) {
            this.mHeaderReady = true;
            return true;
        } else if (result > 0) {
            this.mHeaderReady = false;
            return false;
        } else {
            this.mHeaderReady = false;
            throw new IOException("failed: 0x" + Integer.toHexString(result));
        }
    }

    public String getKey() {
        if (this.mHeaderReady) {
            return this.mHeader.key;
        }
        throw new IllegalStateException("Entity header not read");
    }

    public int getDataSize() {
        if (this.mHeaderReady) {
            return this.mHeader.dataSize;
        }
        throw new IllegalStateException("Entity header not read");
    }

    public int readEntityData(byte[] data, int offset, int size) throws IOException {
        if (this.mHeaderReady) {
            int result = readEntityData_native(this.mBackupReader, data, offset, size);
            if (result >= 0) {
                return result;
            }
            throw new IOException("result=0x" + Integer.toHexString(result));
        }
        throw new IllegalStateException("Entity header not read");
    }

    public void skipEntityData() throws IOException {
        if (this.mHeaderReady) {
            skipEntityData_native(this.mBackupReader);
            return;
        }
        throw new IllegalStateException("Entity header not read");
    }
}
