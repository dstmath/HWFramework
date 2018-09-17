package android.content.res;

import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AssetFileDescriptor implements Parcelable, Closeable {
    public static final Creator<AssetFileDescriptor> CREATOR = new Creator<AssetFileDescriptor>() {
        public AssetFileDescriptor createFromParcel(Parcel in) {
            return new AssetFileDescriptor(in);
        }

        public AssetFileDescriptor[] newArray(int size) {
            return new AssetFileDescriptor[size];
        }
    };
    public static final long UNKNOWN_LENGTH = -1;
    private final Bundle mExtras;
    private final ParcelFileDescriptor mFd;
    private final long mLength;
    private final long mStartOffset;

    public static class AutoCloseInputStream extends android.os.ParcelFileDescriptor.AutoCloseInputStream {
        private long mRemaining;

        public AutoCloseInputStream(AssetFileDescriptor fd) throws IOException {
            super(fd.getParcelFileDescriptor());
            super.skip(fd.getStartOffset());
            this.mRemaining = (long) ((int) fd.getLength());
        }

        public int available() throws IOException {
            if (this.mRemaining >= 0) {
                return this.mRemaining < 2147483647L ? (int) this.mRemaining : Integer.MAX_VALUE;
            } else {
                return super.available();
            }
        }

        public int read() throws IOException {
            byte[] buffer = new byte[1];
            if (read(buffer, 0, 1) == -1) {
                return -1;
            }
            return buffer[0] & 255;
        }

        public int read(byte[] buffer, int offset, int count) throws IOException {
            if (this.mRemaining < 0) {
                return super.read(buffer, offset, count);
            }
            if (this.mRemaining == 0) {
                return -1;
            }
            if (((long) count) > this.mRemaining) {
                count = (int) this.mRemaining;
            }
            int res = super.read(buffer, offset, count);
            if (res >= 0) {
                this.mRemaining -= (long) res;
            }
            return res;
        }

        public int read(byte[] buffer) throws IOException {
            return read(buffer, 0, buffer.length);
        }

        public long skip(long count) throws IOException {
            if (this.mRemaining < 0) {
                return super.skip(count);
            }
            if (this.mRemaining == 0) {
                return -1;
            }
            if (count > this.mRemaining) {
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

        public synchronized void reset() throws IOException {
            if (this.mRemaining < 0) {
                super.reset();
            }
        }
    }

    public static class AutoCloseOutputStream extends android.os.ParcelFileDescriptor.AutoCloseOutputStream {
        private long mRemaining;

        public AutoCloseOutputStream(AssetFileDescriptor fd) throws IOException {
            super(fd.getParcelFileDescriptor());
            if (fd.getParcelFileDescriptor().seekTo(fd.getStartOffset()) < 0) {
                throw new IOException("Unable to seek");
            }
            this.mRemaining = (long) ((int) fd.getLength());
        }

        public void write(byte[] buffer, int offset, int count) throws IOException {
            if (this.mRemaining < 0) {
                super.write(buffer, offset, count);
            } else if (this.mRemaining != 0) {
                if (((long) count) > this.mRemaining) {
                    count = (int) this.mRemaining;
                }
                super.write(buffer, offset, count);
                this.mRemaining -= (long) count;
            }
        }

        public void write(byte[] buffer) throws IOException {
            if (this.mRemaining < 0) {
                super.write(buffer);
            } else if (this.mRemaining != 0) {
                int count = buffer.length;
                if (((long) count) > this.mRemaining) {
                    count = (int) this.mRemaining;
                }
                super.write(buffer);
                this.mRemaining -= (long) count;
            }
        }

        public void write(int oneByte) throws IOException {
            if (this.mRemaining < 0) {
                super.write(oneByte);
            } else if (this.mRemaining != 0) {
                super.write(oneByte);
                this.mRemaining--;
            }
        }
    }

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
        if (this.mLength >= 0) {
            return this.mLength;
        }
        long len = this.mFd.getStatSize();
        if (len < 0) {
            len = -1;
        }
        return len;
    }

    public long getDeclaredLength() {
        return this.mLength;
    }

    public void close() throws IOException {
        this.mFd.close();
    }

    public FileInputStream createInputStream() throws IOException {
        if (this.mLength < 0) {
            return new android.os.ParcelFileDescriptor.AutoCloseInputStream(this.mFd);
        }
        return new AutoCloseInputStream(this);
    }

    public FileOutputStream createOutputStream() throws IOException {
        if (this.mLength < 0) {
            return new android.os.ParcelFileDescriptor.AutoCloseOutputStream(this.mFd);
        }
        return new AutoCloseOutputStream(this);
    }

    public String toString() {
        return "{AssetFileDescriptor: " + this.mFd + " start=" + this.mStartOffset + " len=" + this.mLength + "}";
    }

    public int describeContents() {
        return this.mFd.describeContents();
    }

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
        this.mFd = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(src);
        this.mStartOffset = src.readLong();
        this.mLength = src.readLong();
        if (src.readInt() != 0) {
            this.mExtras = src.readBundle();
        } else {
            this.mExtras = null;
        }
    }
}
