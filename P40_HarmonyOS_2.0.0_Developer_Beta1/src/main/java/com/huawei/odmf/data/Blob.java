package com.huawei.odmf.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.exception.ODMFUnsupportedOperationException;
import com.huawei.odmf.utils.LOG;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Arrays;

public class Blob implements java.sql.Blob, Parcelable {
    public static final Parcelable.Creator<Blob> CREATOR = new Parcelable.Creator<Blob>() {
        /* class com.huawei.odmf.data.Blob.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Blob createFromParcel(Parcel parcel) {
            return new Blob(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Blob[] newArray(int i) {
            return new Blob[i];
        }
    };
    private byte[] binaryData;
    private boolean isClosed;
    private final Object lock;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

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

    public Blob(Parcel parcel) {
        this.binaryData = new byte[0];
        this.isClosed = false;
        this.lock = new Object();
        int readInt = parcel.readInt();
        if (readInt != -1) {
            this.binaryData = new byte[readInt];
            parcel.readByteArray(this.binaryData);
            return;
        }
        this.binaryData = null;
    }

    private byte[] getBinaryData() {
        byte[] bArr;
        synchronized (this.lock) {
            bArr = this.binaryData;
        }
        return bArr;
    }

    private void checkClosed() throws SQLException {
        synchronized (this.lock) {
            if (this.isClosed) {
                throw new ODMFRuntimeException("The blob is not initialized");
            }
        }
    }

    @Override // java.sql.Blob
    public long length() throws SQLException {
        checkClosed();
        return (long) getBinaryData().length;
    }

    @Override // java.sql.Blob
    public byte[] getBytes(long j, int i) throws SQLException {
        checkClosed();
        if (j >= 1) {
            int i2 = ((int) j) - 1;
            byte[] bArr = this.binaryData;
            if (i2 > bArr.length) {
                throw new ODMFIllegalArgumentException();
            } else if (i2 + i <= bArr.length) {
                byte[] bArr2 = new byte[i];
                System.arraycopy(getBinaryData(), i2, bArr2, 0, i);
                return bArr2;
            } else {
                throw new ODMFIllegalArgumentException();
            }
        } else {
            throw new ODMFIllegalArgumentException();
        }
    }

    @Override // java.sql.Blob
    public InputStream getBinaryStream() throws SQLException {
        checkClosed();
        return new ByteArrayInputStream(getBinaryData());
    }

    @Override // java.sql.Blob
    public long position(byte[] bArr, long j) throws SQLException {
        throw new ODMFUnsupportedOperationException();
    }

    @Override // java.sql.Blob
    public long position(java.sql.Blob blob, long j) throws SQLException {
        throw new ODMFUnsupportedOperationException();
    }

    @Override // java.sql.Blob
    public int setBytes(long j, byte[] bArr) throws SQLException {
        checkClosed();
        return setBytes(j, bArr, 0, bArr.length);
    }

    @Override // java.sql.Blob
    public int setBytes(long j, byte[] bArr, int i, int i2) throws SQLException {
        checkClosed();
        OutputStream binaryStream = setBinaryStream(j);
        try {
            binaryStream.write(bArr, i, i2);
            this.binaryData = ((ByteArrayOutputStream) binaryStream).toByteArray();
        } catch (IOException unused) {
            LOG.logE("An error occurred when write to Blob use an outputStream");
        } catch (Throwable th) {
            try {
                binaryStream.close();
            } catch (IOException unused2) {
                LOG.logE("An error occurred when close an outputStream in Blob");
            }
            throw th;
        }
        try {
            binaryStream.close();
        } catch (IOException unused3) {
            LOG.logE("An error occurred when close an outputStream in Blob");
        }
        return i2;
    }

    @Override // java.sql.Blob
    public OutputStream setBinaryStream(long j) throws SQLException {
        checkClosed();
        if (j >= 1) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(this.binaryData, 0, (int) (j - 1));
            return byteArrayOutputStream;
        }
        throw new ODMFIllegalArgumentException();
    }

    @Override // java.sql.Blob
    public void truncate(long j) throws SQLException {
        checkClosed();
        if (j < 0) {
            throw new ODMFIllegalArgumentException();
        } else if (j <= ((long) this.binaryData.length)) {
            int i = (int) j;
            byte[] bArr = new byte[i];
            System.arraycopy(getBinaryData(), 0, bArr, 0, i);
            this.binaryData = bArr;
        } else {
            throw new ODMFIllegalArgumentException();
        }
    }

    @Override // java.sql.Blob
    public void free() throws SQLException {
        synchronized (this.lock) {
            this.binaryData = null;
            this.isClosed = true;
        }
    }

    @Override // java.sql.Blob
    public InputStream getBinaryStream(long j, long j2) throws SQLException {
        checkClosed();
        if (j >= 1) {
            int i = ((int) j) - 1;
            byte[] bArr = this.binaryData;
            if (i > bArr.length) {
                throw new ODMFIllegalArgumentException();
            } else if (((long) i) + j2 <= ((long) bArr.length)) {
                return new ByteArrayInputStream(getBinaryData(), i, (int) j2);
            } else {
                throw new ODMFIllegalArgumentException();
            }
        } else {
            throw new ODMFIllegalArgumentException();
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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        byte[] bArr = this.binaryData;
        if (bArr != null) {
            parcel.writeInt(bArr.length);
            parcel.writeByteArray(this.binaryData);
            return;
        }
        parcel.writeInt(-1);
    }
}
