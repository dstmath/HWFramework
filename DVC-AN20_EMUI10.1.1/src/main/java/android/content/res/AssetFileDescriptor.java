package android.content.res;

import android.annotation.UnsupportedAppUsage;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AssetFileDescriptor implements Parcelable, Closeable {
    public static final Parcelable.Creator<AssetFileDescriptor> CREATOR = new Parcelable.Creator<AssetFileDescriptor>() {
        /* class android.content.res.AssetFileDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AssetFileDescriptor createFromParcel(Parcel in) {
            return new AssetFileDescriptor(in);
        }

        @Override // android.os.Parcelable.Creator
        public AssetFileDescriptor[] newArray(int size) {
            return new AssetFileDescriptor[size];
        }
    };
    public static final long UNKNOWN_LENGTH = -1;
    private final Bundle mExtras;
    @UnsupportedAppUsage
    private final ParcelFileDescriptor mFd;
    @UnsupportedAppUsage
    private final long mLength;
    @UnsupportedAppUsage
    private final long mStartOffset;

    public AssetFileDescriptor(ParcelFileDescriptor fd, long startOffset, long length) {
        this(fd, startOffset, length, null);
    }

    public AssetFileDescriptor(ParcelFileDescriptor fd, long startOffset, long length, Bundle extras) {
        if (fd == null) {
            throw new IllegalArgumentException("fd must not be null");
        } else if (length >= 0 || startOffset == 0) {
            this.mFd = fd;
            this.mStartOffset = startOffset;
            this.mLength = length;
            this.mExtras = extras;
        } else {
            throw new IllegalArgumentException("startOffset must be 0 when using UNKNOWN_LENGTH");
        }
    }

    public ParcelFileDescriptor getParcelFileDescriptor() {
        return this.mFd;
    }

    public FileDescriptor getFileDescriptor() {
        return this.mFd.getFileDescriptor();
    }

    public long getStartOffset() {
        return this.mStartOffset;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public long getLength() {
        long j = this.mLength;
        if (j >= 0) {
            return j;
        }
        long len = this.mFd.getStatSize();
        if (len >= 0) {
            return len;
        }
        return -1;
    }

    public long getDeclaredLength() {
        return this.mLength;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.mFd.close();
    }

    public FileInputStream createInputStream() throws IOException {
        if (this.mLength < 0) {
            return new ParcelFileDescriptor.AutoCloseInputStream(this.mFd);
        }
        return new AutoCloseInputStream(this);
    }

    public FileOutputStream createOutputStream() throws IOException {
        if (this.mLength < 0) {
            return new ParcelFileDescriptor.AutoCloseOutputStream(this.mFd);
        }
        return new AutoCloseOutputStream(this);
    }

    public String toString() {
        return "{AssetFileDescriptor: " + this.mFd + " start=" + this.mStartOffset + " len=" + this.mLength + "}";
    }

    public static class AutoCloseInputStream extends ParcelFileDescriptor.AutoCloseInputStream {
        private long mRemaining;

        public AutoCloseInputStream(AssetFileDescriptor fd) throws IOException {
            super(fd.getParcelFileDescriptor());
            super.skip(fd.getStartOffset());
            this.mRemaining = (long) ((int) fd.getLength());
        }

        @Override // java.io.FileInputStream, java.io.InputStream
        public int available() throws IOException {
            long j = this.mRemaining;
            if (j < 0) {
                return super.available();
            }
            if (j < 2147483647L) {
                return (int) j;
            }
            return Integer.MAX_VALUE;
        }

        @Override // java.io.FileInputStream, android.os.ParcelFileDescriptor.AutoCloseInputStream, java.io.InputStream
        public int read() throws IOException {
            byte[] buffer = new byte[1];
            if (read(buffer, 0, 1) == -1) {
                return -1;
            }
            return buffer[0] & 255;
        }

        @Override // java.io.FileInputStream, android.os.ParcelFileDescriptor.AutoCloseInputStream, java.io.InputStream
        public int read(byte[] buffer, int offset, int count) throws IOException {
            long j = this.mRemaining;
            if (j < 0) {
                return super.read(buffer, offset, count);
            }
            if (j == 0) {
                return -1;
            }
            if (((long) count) > j) {
                count = (int) j;
            }
            int res = super.read(buffer, offset, count);
            if (res >= 0) {
                this.mRemaining -= (long) res;
            }
            return res;
        }

        @Override // java.io.FileInputStream, android.os.ParcelFileDescriptor.AutoCloseInputStream, java.io.InputStream
        public int read(byte[] buffer) throws IOException {
            return read(buffer, 0, buffer.length);
        }

        @Override // java.io.FileInputStream, java.io.InputStream
        public long skip(long count) throws IOException {
            long j = this.mRemaining;
            if (j < 0) {
                return super.skip(count);
            }
            if (j == 0) {
                return -1;
            }
            if (count > j) {
                count = this.mRemaining;
            }
            long res = super.skip(count);
            if (res >= 0) {
                this.mRemaining -= res;
            }
            return res;
        }

        public void mark(int readlimit) {
            if (this.mRemaining < 0) {
                super.mark(readlimit);
            }
        }

        public boolean markSupported() {
            if (this.mRemaining >= 0) {
                return false;
            }
            return super.markSupported();
        }

        @Override // java.io.InputStream
        public synchronized void reset() throws IOException {
            if (this.mRemaining < 0) {
                super.reset();
            }
        }
    }

    public static class AutoCloseOutputStream extends ParcelFileDescriptor.AutoCloseOutputStream {
        private long mRemaining;

        public AutoCloseOutputStream(AssetFileDescriptor fd) throws IOException {
            super(fd.getParcelFileDescriptor());
            if (fd.getParcelFileDescriptor().seekTo(fd.getStartOffset()) >= 0) {
                this.mRemaining = (long) ((int) fd.getLength());
                return;
            }
            throw new IOException("Unable to seek");
        }

        @Override // java.io.OutputStream, java.io.FileOutputStream
        public void write(byte[] buffer, int offset, int count) throws IOException {
            long j = this.mRemaining;
            if (j < 0) {
                super.write(buffer, offset, count);
            } else if (j != 0) {
                if (((long) count) > j) {
                    count = (int) j;
                }
                super.write(buffer, offset, count);
                this.mRemaining -= (long) count;
            }
        }

        @Override // java.io.OutputStream, java.io.FileOutputStream
        public void write(byte[] buffer) throws IOException {
            long j = this.mRemaining;
            if (j < 0) {
                super.write(buffer);
            } else if (j != 0) {
                int count = buffer.length;
                if (((long) count) > j) {
                    count = (int) j;
                }
                super.write(buffer);
                this.mRemaining -= (long) count;
            }
        }

        @Override // java.io.OutputStream, java.io.FileOutputStream
        public void write(int oneByte) throws IOException {
            long j = this.mRemaining;
            if (j < 0) {
                super.write(oneByte);
            } else if (j != 0) {
                super.write(oneByte);
                this.mRemaining--;
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return this.mFd.describeContents();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        this.mFd.writeToParcel(out, flags);
        out.writeLong(this.mStartOffset);
        out.writeLong(this.mLength);
        if (this.mExtras != null) {
            out.writeInt(1);
            out.writeBundle(this.mExtras);
            return;
        }
        out.writeInt(0);
    }

    AssetFileDescriptor(Parcel src) {
        this.mFd = ParcelFileDescriptor.CREATOR.createFromParcel(src);
        this.mStartOffset = src.readLong();
        this.mLength = src.readLong();
        if (src.readInt() != 0) {
            this.mExtras = src.readBundle();
        } else {
            this.mExtras = null;
        }
    }
}
