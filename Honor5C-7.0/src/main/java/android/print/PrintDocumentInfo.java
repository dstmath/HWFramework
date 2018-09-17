package android.print;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;

public final class PrintDocumentInfo implements Parcelable {
    public static final int CONTENT_TYPE_DOCUMENT = 0;
    public static final int CONTENT_TYPE_PHOTO = 1;
    public static final int CONTENT_TYPE_UNKNOWN = -1;
    public static final Creator<PrintDocumentInfo> CREATOR = null;
    public static final int PAGE_COUNT_UNKNOWN = -1;
    private int mContentType;
    private long mDataSize;
    private String mName;
    private int mPageCount;

    public static final class Builder {
        private final PrintDocumentInfo mPrototype;

        public Builder(String name) {
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("name cannot be empty");
            }
            this.mPrototype = new PrintDocumentInfo();
            this.mPrototype.mName = name;
        }

        public Builder setPageCount(int pageCount) {
            if (pageCount >= 0 || pageCount == PrintDocumentInfo.PAGE_COUNT_UNKNOWN) {
                this.mPrototype.mPageCount = pageCount;
                return this;
            }
            throw new IllegalArgumentException("pageCount must be greater than or equal to zero or DocumentInfo#PAGE_COUNT_UNKNOWN");
        }

        public Builder setContentType(int type) {
            this.mPrototype.mContentType = type;
            return this;
        }

        public PrintDocumentInfo build() {
            if (this.mPrototype.mPageCount == 0) {
                this.mPrototype.mPageCount = PrintDocumentInfo.PAGE_COUNT_UNKNOWN;
            }
            return new PrintDocumentInfo(null, null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.print.PrintDocumentInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.print.PrintDocumentInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.print.PrintDocumentInfo.<clinit>():void");
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
        boolean z;
        this.mName = (String) Preconditions.checkStringNotEmpty(parcel.readString());
        this.mPageCount = parcel.readInt();
        if (this.mPageCount == PAGE_COUNT_UNKNOWN || this.mPageCount > 0) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z);
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
        return CONTENT_TYPE_DOCUMENT;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mName);
        parcel.writeInt(this.mPageCount);
        parcel.writeInt(this.mContentType);
        parcel.writeLong(this.mDataSize);
    }

    public int hashCode() {
        return (((((((((this.mName != null ? this.mName.hashCode() : CONTENT_TYPE_DOCUMENT) + 31) * 31) + this.mContentType) * 31) + this.mPageCount) * 31) + ((int) this.mDataSize)) * 31) + ((int) (this.mDataSize >> 32));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PrintDocumentInfo other = (PrintDocumentInfo) obj;
        return TextUtils.equals(this.mName, other.mName) && this.mContentType == other.mContentType && this.mPageCount == other.mPageCount && this.mDataSize == other.mDataSize;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PrintDocumentInfo{");
        builder.append("name=").append(this.mName);
        builder.append(", pageCount=").append(this.mPageCount);
        builder.append(", contentType=").append(contentTypeToString(this.mContentType));
        builder.append(", dataSize=").append(this.mDataSize);
        builder.append("}");
        return builder.toString();
    }

    private String contentTypeToString(int contentType) {
        switch (contentType) {
            case CONTENT_TYPE_DOCUMENT /*0*/:
                return "CONTENT_TYPE_DOCUMENT";
            case CONTENT_TYPE_PHOTO /*1*/:
                return "CONTENT_TYPE_PHOTO";
            default:
                return "CONTENT_TYPE_UNKNOWN";
        }
    }
}
