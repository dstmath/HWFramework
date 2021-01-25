package ohos.softnet.connect;

import java.io.IOException;
import java.io.InputStream;

public class DataPayload {
    private static final int BYTES = 1;
    private static final int FILE = 2;
    private static final int STREAM = 3;
    private byte[] mBytes;
    private File mFile;
    private long mId;
    private Stream mStream;
    private int mType;

    private DataPayload() {
    }

    public long getId() {
        return this.mId;
    }

    public int getType() {
        return this.mType;
    }

    public byte[] getInBytes() {
        return this.mBytes;
    }

    public File getInFile() {
        return this.mFile;
    }

    public Stream getInStream() {
        return this.mStream;
    }

    public static class File {
        private java.io.File mJavaIoFile;
        private int mParcelFileDescriptor;
        private long mSize;

        private File(java.io.File file, int i, long j) {
            this.mJavaIoFile = file;
            this.mParcelFileDescriptor = i;
            this.mSize = j;
        }

        public String toString() {
            return "{mJavaIoFile=" + this.mJavaIoFile.getName() + ",mParcelFileDescriptor=" + this.mParcelFileDescriptor + ",mSize=" + this.mSize + "}";
        }
    }

    public static class Stream {
        private InputStream mInputStream;
        private int mParcelFileDescriptor;

        private Stream(int i, InputStream inputStream) {
            this.mParcelFileDescriptor = i;
            this.mInputStream = inputStream;
        }

        public String toString() {
            int i;
            try {
                i = this.mInputStream.read();
            } catch (IOException unused) {
                i = 0;
            }
            return "{mParcelFileDescriptor=" + this.mParcelFileDescriptor + ",mInputStream=" + i + "}";
        }
    }

    public static class Builder {
        private DataPayload option = new DataPayload();

        public Builder id(long j) {
            this.option.mId = j;
            return this;
        }

        public Builder type(int i) {
            this.option.mType = i;
            return this;
        }

        public Builder bytes(byte[] bArr) {
            this.option.mBytes = bArr;
            return this;
        }

        public Builder file(File file) {
            this.option.mFile = file;
            return this;
        }

        public Builder stream(Stream stream) {
            this.option.mStream = stream;
            return this;
        }

        public DataPayload build() {
            return this.option;
        }
    }
}
