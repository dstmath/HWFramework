package android.app.backup;

import java.io.IOException;
import java.io.InputStream;

public class BackupDataInputStream extends InputStream {
    int dataSize;
    String key;
    BackupDataInput mData;
    byte[] mOneByte;

    BackupDataInputStream(BackupDataInput data) {
        this.mData = data;
    }

    public int read() throws IOException {
        byte[] one = this.mOneByte;
        if (this.mOneByte == null) {
            one = new byte[1];
            this.mOneByte = one;
        }
        this.mData.readEntityData(one, 0, 1);
        return one[0];
    }

    public int read(byte[] b, int offset, int size) throws IOException {
        return this.mData.readEntityData(b, offset, size);
    }

    public int read(byte[] b) throws IOException {
        return this.mData.readEntityData(b, 0, b.length);
    }

    public String getKey() {
        return this.key;
    }

    public int size() {
        return this.dataSize;
    }
}
