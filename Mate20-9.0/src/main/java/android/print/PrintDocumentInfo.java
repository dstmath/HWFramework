package android.print;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class PrintDocumentInfo implements Parcelable {
    public static final int CONTENT_TYPE_DOCUMENT = 0;
    public static final int CONTENT_TYPE_PHOTO = 1;
    public static final int CONTENT_TYPE_UNKNOWN = -1;
    public static final Parcelable.Creator<PrintDocumentInfo> CREATOR = new Parcelable.Creator<PrintDocumentInfo>() {
        public PrintDocumentInfo createFromParcel(Parcel parcel) {
            return new PrintDocumentInfo(parcel);
        }

        public PrintDocumentInfo[] newArray(int size) {
            return new PrintDocumentInfo[size];
        }
    };
    public static final int PAGE_COUNT_UNKNOWN = -1;
    /* access modifiers changed from: private */
    public int mContentType;
    private long mDataSize;
    /* access modifiers changed from: private */
    public String mName;
    /* access modifiers changed from: private */
    public int mPageCount;

    public static final class Builder {
        private final PrintDocumentInfo mPrototype;

        public Builder(String name) {
            if (!TextUtils.isEmpty(name)) {
                this.mPrototype = new PrintDocumentInfo();
                String unused = this.mPrototype.mName = name;
                return;
            }
            throw new IllegalArgumentException("name cannot be empty");
        }

        public Builder setPageCount(int pageCount) {
            if (pageCount >= 0 || pageCount == -1) {
                int unused = this.mPrototype.mPageCount = pageCount;
                return this;
            }
            throw new IllegalArgumentException("pageCount must be greater than or equal to zero or DocumentInfo#PAGE_COUNT_UNKNOWN");
        }

        public Builder setContentType(int type) {
            int unused = this.mPrototype.mContentType = type;
            return this;
        }

        public PrintDocumentInfo build() {
            if (this.mPrototype.mPageCount == 0) {
                int unused = this.mPrototype.mPageCount = -1;
            }
            return new PrintDocumentInfo();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ContentType {
    }

    private PrintDocumentInfo() {
    }

    private PrintDocumentInfo(PrintDocumentInfo prototype) {
        this.mName = prototype.mName;
        this.mPageCount = prototype.mPageCount;
        this.mContentType = prototype.mContentType;
        this.mDataSize = prototype.mDataSize;
    }

    private PrintDocumentInfo(Parcel parcel) {
        this.mName = (String) Preconditions.checkStringNotEmpty(parcel.readString());
        this.mPageCount = parcel.readInt();
        Preconditions.checkArgument(this.mPageCount == -1 || this.mPageCount > 0);
        this.mContentType = parcel.readInt();
        this.mDataSize = Preconditions.checkArgumentNonnegative(parcel.readLong());
    }

    public String getName() {
        return this.mName;
    }

    public int getPageCount() {
        return this.mPageCount;
    }

    public int getContentType() {
        return this.mContentType;
    }

    public long getDataSize() {
        return this.mDataSize;
    }

    public void setDataSize(long dataSize) {
        this.mDataSize = dataSize;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mName);
        parcel.writeInt(this.mPageCount);
        parcel.writeInt(this.mContentType);
        parcel.writeLong(this.mDataSize);
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * ((31 * ((31 * 1) + (this.mName != null ? this.mName.hashCode() : 0))) + this.mContentType)) + this.mPageCount)) + ((int) this.mDataSize))) + ((int) (this.mDataSize >> 32));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrintDocumentInfo other = (PrintDocumentInfo) obj;
        if (TextUtils.equals(this.mName, other.mName) && this.mContentType == other.mContentType && this.mPageCount == other.mPageCount && this.mDataSize == other.mDataSize) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "PrintDocumentInfo{" + "name=" + this.mName + ", pageCount=" + this.mPageCount + ", contentType=" + contentTypeToString(this.mContentType) + ", dataSize=" + this.mDataSize + "}";
    }

    private String contentTypeToString(int contentType) {
        switch (contentType) {
            case 0:
                return "CONTENT_TYPE_DOCUMENT";
            case 1:
                return "CONTENT_TYPE_PHOTO";
            default:
                return "CONTENT_TYPE_UNKNOWN";
        }
    }
}
