package android.mtp;

import android.app.slice.Slice;
import com.android.internal.util.Preconditions;
import dalvik.system.VMRuntime;

public final class MtpObjectInfo {
    /* access modifiers changed from: private */
    public int mAssociationDesc;
    /* access modifiers changed from: private */
    public int mAssociationType;
    /* access modifiers changed from: private */
    public int mCompressedSize;
    /* access modifiers changed from: private */
    public long mDateCreated;
    /* access modifiers changed from: private */
    public long mDateModified;
    /* access modifiers changed from: private */
    public int mFormat;
    /* access modifiers changed from: private */
    public int mHandle;
    /* access modifiers changed from: private */
    public int mImagePixDepth;
    /* access modifiers changed from: private */
    public int mImagePixHeight;
    /* access modifiers changed from: private */
    public int mImagePixWidth;
    /* access modifiers changed from: private */
    public String mKeywords;
    /* access modifiers changed from: private */
    public String mName;
    /* access modifiers changed from: private */
    public int mParent;
    /* access modifiers changed from: private */
    public int mProtectionStatus;
    /* access modifiers changed from: private */
    public int mSequenceNumber;
    /* access modifiers changed from: private */
    public int mStorageId;
    /* access modifiers changed from: private */
    public int mThumbCompressedSize;
    /* access modifiers changed from: private */
    public int mThumbFormat;
    /* access modifiers changed from: private */
    public int mThumbPixHeight;
    /* access modifiers changed from: private */
    public int mThumbPixWidth;

    public static class Builder {
        private MtpObjectInfo mObjectInfo = new MtpObjectInfo();

        public Builder() {
            int unused = this.mObjectInfo.mHandle = -1;
        }

        public Builder(MtpObjectInfo objectInfo) {
            int unused = this.mObjectInfo.mHandle = -1;
            int unused2 = this.mObjectInfo.mAssociationDesc = objectInfo.mAssociationDesc;
            int unused3 = this.mObjectInfo.mAssociationType = objectInfo.mAssociationType;
            int unused4 = this.mObjectInfo.mCompressedSize = objectInfo.mCompressedSize;
            long unused5 = this.mObjectInfo.mDateCreated = objectInfo.mDateCreated;
            long unused6 = this.mObjectInfo.mDateModified = objectInfo.mDateModified;
            int unused7 = this.mObjectInfo.mFormat = objectInfo.mFormat;
            int unused8 = this.mObjectInfo.mImagePixDepth = objectInfo.mImagePixDepth;
            int unused9 = this.mObjectInfo.mImagePixHeight = objectInfo.mImagePixHeight;
            int unused10 = this.mObjectInfo.mImagePixWidth = objectInfo.mImagePixWidth;
            String unused11 = this.mObjectInfo.mKeywords = objectInfo.mKeywords;
            String unused12 = this.mObjectInfo.mName = objectInfo.mName;
            int unused13 = this.mObjectInfo.mParent = objectInfo.mParent;
            int unused14 = this.mObjectInfo.mProtectionStatus = objectInfo.mProtectionStatus;
            int unused15 = this.mObjectInfo.mSequenceNumber = objectInfo.mSequenceNumber;
            int unused16 = this.mObjectInfo.mStorageId = objectInfo.mStorageId;
            int unused17 = this.mObjectInfo.mThumbCompressedSize = objectInfo.mThumbCompressedSize;
            int unused18 = this.mObjectInfo.mThumbFormat = objectInfo.mThumbFormat;
            int unused19 = this.mObjectInfo.mThumbPixHeight = objectInfo.mThumbPixHeight;
            int unused20 = this.mObjectInfo.mThumbPixWidth = objectInfo.mThumbPixWidth;
        }

        public Builder setObjectHandle(int value) {
            int unused = this.mObjectInfo.mHandle = value;
            return this;
        }

        public Builder setAssociationDesc(int value) {
            int unused = this.mObjectInfo.mAssociationDesc = value;
            return this;
        }

        public Builder setAssociationType(int value) {
            int unused = this.mObjectInfo.mAssociationType = value;
            return this;
        }

        public Builder setCompressedSize(long value) {
            int unused = this.mObjectInfo.mCompressedSize = MtpObjectInfo.longToUint32(value, Slice.SUBTYPE_VALUE);
            return this;
        }

        public Builder setDateCreated(long value) {
            long unused = this.mObjectInfo.mDateCreated = value;
            return this;
        }

        public Builder setDateModified(long value) {
            long unused = this.mObjectInfo.mDateModified = value;
            return this;
        }

        public Builder setFormat(int value) {
            int unused = this.mObjectInfo.mFormat = value;
            return this;
        }

        public Builder setImagePixDepth(long value) {
            int unused = this.mObjectInfo.mImagePixDepth = MtpObjectInfo.longToUint32(value, Slice.SUBTYPE_VALUE);
            return this;
        }

        public Builder setImagePixHeight(long value) {
            int unused = this.mObjectInfo.mImagePixHeight = MtpObjectInfo.longToUint32(value, Slice.SUBTYPE_VALUE);
            return this;
        }

        public Builder setImagePixWidth(long value) {
            int unused = this.mObjectInfo.mImagePixWidth = MtpObjectInfo.longToUint32(value, Slice.SUBTYPE_VALUE);
            return this;
        }

        public Builder setKeywords(String value) {
            if (VMRuntime.getRuntime().getTargetSdkVersion() > 25) {
                Preconditions.checkNotNull(value);
            } else if (value == null) {
                value = "";
            }
            String unused = this.mObjectInfo.mKeywords = value;
            return this;
        }

        public Builder setName(String value) {
            Preconditions.checkNotNull(value);
            String unused = this.mObjectInfo.mName = value;
            return this;
        }

        public Builder setParent(int value) {
            int unused = this.mObjectInfo.mParent = value;
            return this;
        }

        public Builder setProtectionStatus(int value) {
            int unused = this.mObjectInfo.mProtectionStatus = value;
            return this;
        }

        public Builder setSequenceNumber(long value) {
            int unused = this.mObjectInfo.mSequenceNumber = MtpObjectInfo.longToUint32(value, Slice.SUBTYPE_VALUE);
            return this;
        }

        public Builder setStorageId(int value) {
            int unused = this.mObjectInfo.mStorageId = value;
            return this;
        }

        public Builder setThumbCompressedSize(long value) {
            int unused = this.mObjectInfo.mThumbCompressedSize = MtpObjectInfo.longToUint32(value, Slice.SUBTYPE_VALUE);
            return this;
        }

        public Builder setThumbFormat(int value) {
            int unused = this.mObjectInfo.mThumbFormat = value;
            return this;
        }

        public Builder setThumbPixHeight(long value) {
            int unused = this.mObjectInfo.mThumbPixHeight = MtpObjectInfo.longToUint32(value, Slice.SUBTYPE_VALUE);
            return this;
        }

        public Builder setThumbPixWidth(long value) {
            int unused = this.mObjectInfo.mThumbPixWidth = MtpObjectInfo.longToUint32(value, Slice.SUBTYPE_VALUE);
            return this;
        }

        public MtpObjectInfo build() {
            MtpObjectInfo result = this.mObjectInfo;
            this.mObjectInfo = null;
            return result;
        }
    }

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

    private static long uint32ToLong(int value) {
        return value < 0 ? 4294967296L + ((long) value) : (long) value;
    }

    /* access modifiers changed from: private */
    public static int longToUint32(long value, String valueName) {
        Preconditions.checkArgumentInRange(value, 0, 4294967295L, valueName);
        return (int) value;
    }
}
