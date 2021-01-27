package android.mtp;

import com.android.internal.util.Preconditions;
import dalvik.system.VMRuntime;

public final class MtpObjectInfo {
    private int mAssociationDesc;
    private int mAssociationType;
    private int mCompressedSize;
    private long mDateCreated;
    private long mDateModified;
    private int mFormat;
    private int mHandle;
    private int mImagePixDepth;
    private int mImagePixHeight;
    private int mImagePixWidth;
    private String mKeywords;
    private String mName;
    private int mParent;
    private int mProtectionStatus;
    private int mSequenceNumber;
    private int mStorageId;
    private int mThumbCompressedSize;
    private int mThumbFormat;
    private int mThumbPixHeight;
    private int mThumbPixWidth;

    private MtpObjectInfo() {
        this.mName = "";
        this.mKeywords = "";
    }

    public final int getObjectHandle() {
        return this.mHandle;
    }

    public final int getStorageId() {
        return this.mStorageId;
    }

    public final int getFormat() {
        return this.mFormat;
    }

    public final int getProtectionStatus() {
        return this.mProtectionStatus;
    }

    public final int getCompressedSize() {
        Preconditions.checkState(this.mCompressedSize >= 0);
        return this.mCompressedSize;
    }

    public final long getCompressedSizeLong() {
        return uint32ToLong(this.mCompressedSize);
    }

    public final int getThumbFormat() {
        return this.mThumbFormat;
    }

    public final int getThumbCompressedSize() {
        Preconditions.checkState(this.mThumbCompressedSize >= 0);
        return this.mThumbCompressedSize;
    }

    public final long getThumbCompressedSizeLong() {
        return uint32ToLong(this.mThumbCompressedSize);
    }

    public final int getThumbPixWidth() {
        Preconditions.checkState(this.mThumbPixWidth >= 0);
        return this.mThumbPixWidth;
    }

    public final long getThumbPixWidthLong() {
        return uint32ToLong(this.mThumbPixWidth);
    }

    public final int getThumbPixHeight() {
        Preconditions.checkState(this.mThumbPixHeight >= 0);
        return this.mThumbPixHeight;
    }

    public final long getThumbPixHeightLong() {
        return uint32ToLong(this.mThumbPixHeight);
    }

    public final int getImagePixWidth() {
        Preconditions.checkState(this.mImagePixWidth >= 0);
        return this.mImagePixWidth;
    }

    public final long getImagePixWidthLong() {
        return uint32ToLong(this.mImagePixWidth);
    }

    public final int getImagePixHeight() {
        Preconditions.checkState(this.mImagePixHeight >= 0);
        return this.mImagePixHeight;
    }

    public final long getImagePixHeightLong() {
        return uint32ToLong(this.mImagePixHeight);
    }

    public final int getImagePixDepth() {
        Preconditions.checkState(this.mImagePixDepth >= 0);
        return this.mImagePixDepth;
    }

    public final long getImagePixDepthLong() {
        return uint32ToLong(this.mImagePixDepth);
    }

    public final int getParent() {
        return this.mParent;
    }

    public final int getAssociationType() {
        return this.mAssociationType;
    }

    public final int getAssociationDesc() {
        return this.mAssociationDesc;
    }

    public final int getSequenceNumber() {
        Preconditions.checkState(this.mSequenceNumber >= 0);
        return this.mSequenceNumber;
    }

    public final long getSequenceNumberLong() {
        return uint32ToLong(this.mSequenceNumber);
    }

    public final String getName() {
        return this.mName;
    }

    public final long getDateCreated() {
        return this.mDateCreated;
    }

    public final long getDateModified() {
        return this.mDateModified;
    }

    public final String getKeywords() {
        return this.mKeywords;
    }

    public static class Builder {
        private MtpObjectInfo mObjectInfo = new MtpObjectInfo();

        public Builder() {
            this.mObjectInfo.mHandle = -1;
        }

        public Builder(MtpObjectInfo objectInfo) {
            this.mObjectInfo.mHandle = -1;
            this.mObjectInfo.mAssociationDesc = objectInfo.mAssociationDesc;
            this.mObjectInfo.mAssociationType = objectInfo.mAssociationType;
            this.mObjectInfo.mCompressedSize = objectInfo.mCompressedSize;
            this.mObjectInfo.mDateCreated = objectInfo.mDateCreated;
            this.mObjectInfo.mDateModified = objectInfo.mDateModified;
            this.mObjectInfo.mFormat = objectInfo.mFormat;
            this.mObjectInfo.mImagePixDepth = objectInfo.mImagePixDepth;
            this.mObjectInfo.mImagePixHeight = objectInfo.mImagePixHeight;
            this.mObjectInfo.mImagePixWidth = objectInfo.mImagePixWidth;
            this.mObjectInfo.mKeywords = objectInfo.mKeywords;
            this.mObjectInfo.mName = objectInfo.mName;
            this.mObjectInfo.mParent = objectInfo.mParent;
            this.mObjectInfo.mProtectionStatus = objectInfo.mProtectionStatus;
            this.mObjectInfo.mSequenceNumber = objectInfo.mSequenceNumber;
            this.mObjectInfo.mStorageId = objectInfo.mStorageId;
            this.mObjectInfo.mThumbCompressedSize = objectInfo.mThumbCompressedSize;
            this.mObjectInfo.mThumbFormat = objectInfo.mThumbFormat;
            this.mObjectInfo.mThumbPixHeight = objectInfo.mThumbPixHeight;
            this.mObjectInfo.mThumbPixWidth = objectInfo.mThumbPixWidth;
        }

        public Builder setObjectHandle(int value) {
            this.mObjectInfo.mHandle = value;
            return this;
        }

        public Builder setAssociationDesc(int value) {
            this.mObjectInfo.mAssociationDesc = value;
            return this;
        }

        public Builder setAssociationType(int value) {
            this.mObjectInfo.mAssociationType = value;
            return this;
        }

        public Builder setCompressedSize(long value) {
            this.mObjectInfo.mCompressedSize = MtpObjectInfo.longToUint32(value, "value");
            return this;
        }

        public Builder setDateCreated(long value) {
            this.mObjectInfo.mDateCreated = value;
            return this;
        }

        public Builder setDateModified(long value) {
            this.mObjectInfo.mDateModified = value;
            return this;
        }

        public Builder setFormat(int value) {
            this.mObjectInfo.mFormat = value;
            return this;
        }

        public Builder setImagePixDepth(long value) {
            this.mObjectInfo.mImagePixDepth = MtpObjectInfo.longToUint32(value, "value");
            return this;
        }

        public Builder setImagePixHeight(long value) {
            this.mObjectInfo.mImagePixHeight = MtpObjectInfo.longToUint32(value, "value");
            return this;
        }

        public Builder setImagePixWidth(long value) {
            this.mObjectInfo.mImagePixWidth = MtpObjectInfo.longToUint32(value, "value");
            return this;
        }

        public Builder setKeywords(String value) {
            if (VMRuntime.getRuntime().getTargetSdkVersion() > 25) {
                Preconditions.checkNotNull(value);
            } else if (value == null) {
                value = "";
            }
            this.mObjectInfo.mKeywords = value;
            return this;
        }

        public Builder setName(String value) {
            Preconditions.checkNotNull(value);
            this.mObjectInfo.mName = value;
            return this;
        }

        public Builder setParent(int value) {
            this.mObjectInfo.mParent = value;
            return this;
        }

        public Builder setProtectionStatus(int value) {
            this.mObjectInfo.mProtectionStatus = value;
            return this;
        }

        public Builder setSequenceNumber(long value) {
            this.mObjectInfo.mSequenceNumber = MtpObjectInfo.longToUint32(value, "value");
            return this;
        }

        public Builder setStorageId(int value) {
            this.mObjectInfo.mStorageId = value;
            return this;
        }

        public Builder setThumbCompressedSize(long value) {
            this.mObjectInfo.mThumbCompressedSize = MtpObjectInfo.longToUint32(value, "value");
            return this;
        }

        public Builder setThumbFormat(int value) {
            this.mObjectInfo.mThumbFormat = value;
            return this;
        }

        public Builder setThumbPixHeight(long value) {
            this.mObjectInfo.mThumbPixHeight = MtpObjectInfo.longToUint32(value, "value");
            return this;
        }

        public Builder setThumbPixWidth(long value) {
            this.mObjectInfo.mThumbPixWidth = MtpObjectInfo.longToUint32(value, "value");
            return this;
        }

        public MtpObjectInfo build() {
            MtpObjectInfo result = this.mObjectInfo;
            this.mObjectInfo = null;
            return result;
        }
    }

    private static long uint32ToLong(int value) {
        return value < 0 ? ((long) value) + 4294967296L : (long) value;
    }

    /* access modifiers changed from: private */
    public static int longToUint32(long value, String valueName) {
        Preconditions.checkArgumentInRange(value, 0, 4294967295L, valueName);
        return (int) value;
    }
}
