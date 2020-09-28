package com.huawei.softnet.connect;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

public class DataPayload implements Parcelable {
    private static final int BYTES = 1;
    public static final Parcelable.Creator<DataPayload> CREATOR = new Parcelable.Creator<DataPayload>() {
        /* class com.huawei.softnet.connect.DataPayload.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DataPayload createFromParcel(Parcel in) {
            return new DataPayload(in);
        }

        @Override // android.os.Parcelable.Creator
        public DataPayload[] newArray(int size) {
            return new DataPayload[size];
        }
    };
    private static final int FILE = 2;
    private static final String F_RESULT_CODE = "F_RESULT_CODE: ";
    private static final int PARCEL_FILE_SIZE = 0;
    private static final int PARCEL_FLAG = 0;
    private static final int RESULT_CODE_ERROR_READ_PARCEL = 2;
    private static final int RESULT_CODE_ERROR_WRITE_PARCEL = 3;
    private static final int STREAM = 3;
    private static final String TAG = "DataPayload";
    private byte[] mBytes;
    private File mFile;
    private long mId;
    private Stream mStream;
    private int mType;

    protected DataPayload(Parcel in) {
        this.mId = in.readLong();
        this.mType = in.readInt();
        int i = this.mType;
        if (i == 1) {
            this.mBytes = in.createByteArray();
        } else if (i == 2) {
            ParcelFileDescriptor fileParcel = (ParcelFileDescriptor) in.readParcelable(ParcelFileDescriptor.class.getClassLoader());
            java.io.File file = new java.io.File(in.readString());
            if (fileParcel != null) {
                this.mFile = new File(file, fileParcel, fileParcel.getStatSize());
            } else {
                this.mFile = new File(file, fileParcel, 0);
            }
        } else {
            this.mStream = new Stream((ParcelFileDescriptor) in.readParcelable(ParcelFileDescriptor.class.getClassLoader()), null);
        }
        Log.i(TAG, "data deserialize success");
    }

    private DataPayload() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeInt(this.mType);
        int i = this.mType;
        if (i == 1) {
            dest.writeByteArray(this.mBytes);
        } else if (i == 2) {
            try {
                dest.writeParcelable(this.mFile.mParcelFileDescriptor, flags);
                dest.writeString(this.mFile.mJavaIoFile.getCanonicalPath());
            } catch (IOException | SecurityException e) {
                Log.e(TAG, "F_RESULT_CODE: 2reason: fail to serialize data");
            }
        } else {
            dest.writeParcelable(this.mStream.mParcelFileDescriptor, flags);
        }
        Log.i(TAG, "data serialize success");
    }

    public int describeContents() {
        return 0;
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
        private ParcelFileDescriptor mParcelFileDescriptor;
        private long mSize;

        private File(java.io.File javaIoFile, ParcelFileDescriptor parcelFileDescriptor, long size) {
            this.mJavaIoFile = javaIoFile;
            this.mParcelFileDescriptor = parcelFileDescriptor;
            this.mSize = size;
        }
    }

    public static class Stream {
        private InputStream mInputStream;
        private ParcelFileDescriptor mParcelFileDescriptor;

        private Stream(ParcelFileDescriptor parcelFileDescriptor, InputStream inputStream) {
            this.mParcelFileDescriptor = parcelFileDescriptor;
            this.mInputStream = inputStream;
        }
    }

    public static class Builder {
        private DataPayload option = new DataPayload();

        public Builder id(long id) {
            this.option.mId = id;
            return this;
        }

        public Builder type(int type) {
            this.option.mType = type;
            return this;
        }

        public Builder bytes(byte[] bytes) {
            this.option.mBytes = bytes;
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
