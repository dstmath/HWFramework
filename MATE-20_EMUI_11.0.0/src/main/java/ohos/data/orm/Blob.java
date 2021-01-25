package ohos.data.orm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class Blob implements java.sql.Blob {
    static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "Blob");
    private byte[] binaryData;
    private boolean isClosed;
    private final Object lock;

    public Blob() {
        this.binaryData = new byte[0];
        this.isClosed = false;
        this.lock = new Object();
    }

    public Blob(byte[] bArr) {
        this.binaryData = new byte[0];
        this.isClosed = false;
        this.lock = new Object();
        if (bArr != null) {
            this.binaryData = Arrays.copyOf(bArr, bArr.length);
        }
    }

    private byte[] getBinaryData() {
        byte[] bArr;
        synchronized (this.lock) {
            bArr = this.binaryData;
        }
        return bArr;
    }

    private void checkClosed() {
        synchronized (this.lock) {
            if (this.isClosed) {
                throw new IllegalStateException("The blob is not initialized");
            }
        }
    }

    @Override // java.sql.Blob
    public long length() {
        checkClosed();
        return (long) getBinaryData().length;
    }

    @Override // java.sql.Blob
    public byte[] getBytes(long j, int i) {
        checkClosed();
        if (j >= 1) {
            int i2 = ((int) j) - 1;
            byte[] bArr = this.binaryData;
            if (i2 > bArr.length) {
                throw new IllegalArgumentException();
            } else if (i2 + i <= bArr.length) {
                byte[] bArr2 = new byte[i];
                System.arraycopy(getBinaryData(), i2, bArr2, 0, i);
                return bArr2;
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override // java.sql.Blob
    public InputStream getBinaryStream() {
        checkClosed();
        return new ByteArrayInputStream(getBinaryData());
    }

    @Override // java.sql.Blob
    public long position(byte[] bArr, long j) {
        throw new UnsupportedOperationException();
    }

    @Override // java.sql.Blob
    public long position(java.sql.Blob blob, long j) {
        throw new UnsupportedOperationException();
    }

    @Override // java.sql.Blob
    public int setBytes(long j, byte[] bArr) {
        checkClosed();
        return setBytes(j, bArr, 0, bArr.length);
    }

    @Override // java.sql.Blob
    public int setBytes(long j, byte[] bArr, int i, int i2) {
        checkClosed();
        OutputStream binaryStream = setBinaryStream(j);
        try {
            binaryStream.write(bArr, i, i2);
            if (binaryStream instanceof ByteArrayOutputStream) {
                this.binaryData = ((ByteArrayOutputStream) binaryStream).toByteArray();
            }
        } catch (IOException unused) {
            HiLog.error(LABEL, "An error occurred when write to Blob use an outputStream", new Object[0]);
        } catch (Throwable th) {
            try {
                binaryStream.close();
            } catch (IOException unused2) {
                HiLog.error(LABEL, "An error occurred when close an outputStream in Blob", new Object[0]);
            }
            throw th;
        }
        try {
            binaryStream.close();
        } catch (IOException unused3) {
            HiLog.error(LABEL, "An error occurred when close an outputStream in Blob", new Object[0]);
        }
        return i2;
    }

    @Override // java.sql.Blob
    public OutputStream setBinaryStream(long j) {
        checkClosed();
        if (j >= 1) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(this.binaryData, 0, (int) (j - 1));
            return byteArrayOutputStream;
        }
        throw new IllegalArgumentException();
    }

    @Override // java.sql.Blob
    public void truncate(long j) {
        checkClosed();
        if (j < 0) {
            throw new IllegalArgumentException();
        } else if (j <= ((long) this.binaryData.length)) {
            int i = (int) j;
            byte[] bArr = new byte[i];
            System.arraycopy(getBinaryData(), 0, bArr, 0, i);
            this.binaryData = bArr;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override // java.sql.Blob
    public void free() {
        synchronized (this.lock) {
            this.binaryData = null;
            this.isClosed = true;
        }
    }

    @Override // java.sql.Blob
    public InputStream getBinaryStream(long j, long j2) {
        checkClosed();
        if (j >= 1) {
            int i = ((int) j) - 1;
            byte[] bArr = this.binaryData;
            if (i > bArr.length) {
                throw new IllegalArgumentException();
            } else if (((long) i) + j2 <= ((long) bArr.length)) {
                return new ByteArrayInputStream(getBinaryData(), i, (int) j2);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Arrays.equals(getBinaryData(), ((Blob) obj).getBinaryData());
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Arrays.hashCode(this.binaryData);
    }
}
