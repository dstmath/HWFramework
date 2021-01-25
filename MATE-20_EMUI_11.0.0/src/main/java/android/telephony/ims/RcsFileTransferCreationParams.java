package android.telephony.ims;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public final class RcsFileTransferCreationParams implements Parcelable {
    public static final Parcelable.Creator<RcsFileTransferCreationParams> CREATOR = new Parcelable.Creator<RcsFileTransferCreationParams>() {
        /* class android.telephony.ims.RcsFileTransferCreationParams.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsFileTransferCreationParams createFromParcel(Parcel in) {
            return new RcsFileTransferCreationParams(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsFileTransferCreationParams[] newArray(int size) {
            return new RcsFileTransferCreationParams[size];
        }
    };
    private String mContentMimeType;
    private Uri mContentUri;
    private long mFileSize;
    private int mFileTransferStatus;
    private int mHeight;
    private long mMediaDuration;
    private String mPreviewMimeType;
    private Uri mPreviewUri;
    private String mRcsFileTransferSessionId;
    private long mTransferOffset;
    private int mWidth;

    public String getRcsFileTransferSessionId() {
        return this.mRcsFileTransferSessionId;
    }

    public Uri getContentUri() {
        return this.mContentUri;
    }

    public String getContentMimeType() {
        return this.mContentMimeType;
    }

    public long getFileSize() {
        return this.mFileSize;
    }

    public long getTransferOffset() {
        return this.mTransferOffset;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public long getMediaDuration() {
        return this.mMediaDuration;
    }

    public Uri getPreviewUri() {
        return this.mPreviewUri;
    }

    public String getPreviewMimeType() {
        return this.mPreviewMimeType;
    }

    public int getFileTransferStatus() {
        return this.mFileTransferStatus;
    }

    RcsFileTransferCreationParams(Builder builder) {
        this.mRcsFileTransferSessionId = builder.mRcsFileTransferSessionId;
        this.mContentUri = builder.mContentUri;
        this.mContentMimeType = builder.mContentMimeType;
        this.mFileSize = builder.mFileSize;
        this.mTransferOffset = builder.mTransferOffset;
        this.mWidth = builder.mWidth;
        this.mHeight = builder.mHeight;
        this.mMediaDuration = builder.mLength;
        this.mPreviewUri = builder.mPreviewUri;
        this.mPreviewMimeType = builder.mPreviewMimeType;
        this.mFileTransferStatus = builder.mFileTransferStatus;
    }

    public class Builder {
        private String mContentMimeType;
        private Uri mContentUri;
        private long mFileSize;
        private int mFileTransferStatus;
        private int mHeight;
        private long mLength;
        private String mPreviewMimeType;
        private Uri mPreviewUri;
        private String mRcsFileTransferSessionId;
        private long mTransferOffset;
        private int mWidth;

        public Builder() {
        }

        public Builder setFileTransferSessionId(String sessionId) {
            this.mRcsFileTransferSessionId = sessionId;
            return this;
        }

        public Builder setContentUri(Uri contentUri) {
            this.mContentUri = contentUri;
            return this;
        }

        public Builder setContentMimeType(String contentType) {
            this.mContentMimeType = contentType;
            return this;
        }

        public Builder setFileSize(long size) {
            this.mFileSize = size;
            return this;
        }

        public Builder setTransferOffset(long offset) {
            this.mTransferOffset = offset;
            return this;
        }

        public Builder setWidth(int width) {
            this.mWidth = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.mHeight = height;
            return this;
        }

        public Builder setMediaDuration(long length) {
            this.mLength = length;
            return this;
        }

        public Builder setPreviewUri(Uri previewUri) {
            this.mPreviewUri = previewUri;
            return this;
        }

        public Builder setPreviewMimeType(String previewType) {
            this.mPreviewMimeType = previewType;
            return this;
        }

        public Builder setFileTransferStatus(int status) {
            this.mFileTransferStatus = status;
            return this;
        }

        public RcsFileTransferCreationParams build() {
            return new RcsFileTransferCreationParams(this);
        }
    }

    private RcsFileTransferCreationParams(Parcel in) {
        this.mRcsFileTransferSessionId = in.readString();
        this.mContentUri = (Uri) in.readParcelable(Uri.class.getClassLoader());
        this.mContentMimeType = in.readString();
        this.mFileSize = in.readLong();
        this.mTransferOffset = in.readLong();
        this.mWidth = in.readInt();
        this.mHeight = in.readInt();
        this.mMediaDuration = in.readLong();
        this.mPreviewUri = (Uri) in.readParcelable(Uri.class.getClassLoader());
        this.mPreviewMimeType = in.readString();
        this.mFileTransferStatus = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mRcsFileTransferSessionId);
        dest.writeParcelable(this.mContentUri, flags);
        dest.writeString(this.mContentMimeType);
        dest.writeLong(this.mFileSize);
        dest.writeLong(this.mTransferOffset);
        dest.writeInt(this.mWidth);
        dest.writeInt(this.mHeight);
        dest.writeLong(this.mMediaDuration);
        dest.writeParcelable(this.mPreviewUri, flags);
        dest.writeString(this.mPreviewMimeType);
        dest.writeInt(this.mFileTransferStatus);
    }
}
