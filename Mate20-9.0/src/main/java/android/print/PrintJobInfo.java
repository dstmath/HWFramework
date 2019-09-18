package android.print;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public final class PrintJobInfo implements Parcelable {
    public static final Parcelable.Creator<PrintJobInfo> CREATOR = new Parcelable.Creator<PrintJobInfo>() {
        public PrintJobInfo createFromParcel(Parcel parcel) {
            return new PrintJobInfo(parcel);
        }

        public PrintJobInfo[] newArray(int size) {
            return new PrintJobInfo[size];
        }
    };
    public static final int STATE_ANY = -1;
    public static final int STATE_ANY_ACTIVE = -3;
    public static final int STATE_ANY_SCHEDULED = -4;
    public static final int STATE_ANY_VISIBLE_TO_CLIENTS = -2;
    public static final int STATE_BLOCKED = 4;
    public static final int STATE_CANCELED = 7;
    public static final int STATE_COMPLETED = 5;
    public static final int STATE_CREATED = 1;
    public static final int STATE_FAILED = 6;
    public static final int STATE_QUEUED = 2;
    public static final int STATE_STARTED = 3;
    /* access modifiers changed from: private */
    public Bundle mAdvancedOptions;
    private int mAppId;
    /* access modifiers changed from: private */
    public PrintAttributes mAttributes;
    private boolean mCanceling;
    /* access modifiers changed from: private */
    public int mCopies;
    private long mCreationTime;
    private PrintDocumentInfo mDocumentInfo;
    private PrintJobId mId;
    private String mLabel;
    /* access modifiers changed from: private */
    public PageRange[] mPageRanges;
    private PrinterId mPrinterId;
    private String mPrinterName;
    /* access modifiers changed from: private */
    public float mProgress;
    private int mState;
    /* access modifiers changed from: private */
    public CharSequence mStatus;
    private int mStatusRes;
    private CharSequence mStatusResAppPackageName;
    private String mTag;

    public static final class Builder {
        private final PrintJobInfo mPrototype;

        public Builder(PrintJobInfo prototype) {
            PrintJobInfo printJobInfo;
            if (prototype != null) {
                printJobInfo = new PrintJobInfo(prototype);
            } else {
                printJobInfo = new PrintJobInfo();
            }
            this.mPrototype = printJobInfo;
        }

        public void setCopies(int copies) {
            int unused = this.mPrototype.mCopies = copies;
        }

        public void setAttributes(PrintAttributes attributes) {
            PrintAttributes unused = this.mPrototype.mAttributes = attributes;
        }

        public void setPages(PageRange[] pages) {
            PageRange[] unused = this.mPrototype.mPageRanges = pages;
        }

        public void setProgress(float progress) {
            Preconditions.checkArgumentInRange(progress, 0.0f, 1.0f, "progress");
            float unused = this.mPrototype.mProgress = progress;
        }

        public void setStatus(CharSequence status) {
            CharSequence unused = this.mPrototype.mStatus = status;
        }

        public void putAdvancedOption(String key, String value) {
            Preconditions.checkNotNull(key, "key cannot be null");
            if (this.mPrototype.mAdvancedOptions == null) {
                Bundle unused = this.mPrototype.mAdvancedOptions = new Bundle();
            }
            this.mPrototype.mAdvancedOptions.putString(key, value);
        }

        public void putAdvancedOption(String key, int value) {
            if (this.mPrototype.mAdvancedOptions == null) {
                Bundle unused = this.mPrototype.mAdvancedOptions = new Bundle();
            }
            this.mPrototype.mAdvancedOptions.putInt(key, value);
        }

        public PrintJobInfo build() {
            return this.mPrototype;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public PrintJobInfo() {
        this.mProgress = -1.0f;
    }

    public PrintJobInfo(PrintJobInfo other) {
        this.mId = other.mId;
        this.mLabel = other.mLabel;
        this.mPrinterId = other.mPrinterId;
        this.mPrinterName = other.mPrinterName;
        this.mState = other.mState;
        this.mAppId = other.mAppId;
        this.mTag = other.mTag;
        this.mCreationTime = other.mCreationTime;
        this.mCopies = other.mCopies;
        this.mPageRanges = other.mPageRanges;
        this.mAttributes = other.mAttributes;
        this.mDocumentInfo = other.mDocumentInfo;
        this.mProgress = other.mProgress;
        this.mStatus = other.mStatus;
        this.mStatusRes = other.mStatusRes;
        this.mStatusResAppPackageName = other.mStatusResAppPackageName;
        this.mCanceling = other.mCanceling;
        this.mAdvancedOptions = other.mAdvancedOptions;
    }

    private PrintJobInfo(Parcel parcel) {
        this.mId = (PrintJobId) parcel.readParcelable(null);
        this.mLabel = parcel.readString();
        this.mPrinterId = (PrinterId) parcel.readParcelable(null);
        this.mPrinterName = parcel.readString();
        this.mState = parcel.readInt();
        this.mAppId = parcel.readInt();
        this.mTag = parcel.readString();
        this.mCreationTime = parcel.readLong();
        this.mCopies = parcel.readInt();
        Parcelable[] parcelables = parcel.readParcelableArray(null);
        boolean z = false;
        if (parcelables != null) {
            this.mPageRanges = new PageRange[parcelables.length];
            for (int i = 0; i < parcelables.length; i++) {
                this.mPageRanges[i] = (PageRange) parcelables[i];
            }
        }
        this.mAttributes = (PrintAttributes) parcel.readParcelable(null);
        this.mDocumentInfo = (PrintDocumentInfo) parcel.readParcelable(null);
        this.mProgress = parcel.readFloat();
        this.mStatus = parcel.readCharSequence();
        this.mStatusRes = parcel.readInt();
        this.mStatusResAppPackageName = parcel.readCharSequence();
        this.mCanceling = parcel.readInt() == 1 ? true : z;
        this.mAdvancedOptions = parcel.readBundle();
        if (this.mAdvancedOptions != null) {
            Preconditions.checkArgument(!this.mAdvancedOptions.containsKey(null));
        }
    }

    public PrintJobId getId() {
        return this.mId;
    }

    public void setId(PrintJobId id) {
        this.mId = id;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public PrinterId getPrinterId() {
        return this.mPrinterId;
    }

    public void setPrinterId(PrinterId printerId) {
        this.mPrinterId = printerId;
    }

    public String getPrinterName() {
        return this.mPrinterName;
    }

    public void setPrinterName(String printerName) {
        this.mPrinterName = printerName;
    }

    public int getState() {
        return this.mState;
    }

    public void setState(int state) {
        this.mState = state;
    }

    public void setProgress(float progress) {
        Preconditions.checkArgumentInRange(progress, 0.0f, 1.0f, "progress");
        this.mProgress = progress;
    }

    public void setStatus(CharSequence status) {
        this.mStatusRes = 0;
        this.mStatusResAppPackageName = null;
        this.mStatus = status;
    }

    public void setStatus(int status, CharSequence appPackageName) {
        this.mStatus = null;
        this.mStatusRes = status;
        this.mStatusResAppPackageName = appPackageName;
    }

    public int getAppId() {
        return this.mAppId;
    }

    public void setAppId(int appId) {
        this.mAppId = appId;
    }

    public String getTag() {
        return this.mTag;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    public long getCreationTime() {
        return this.mCreationTime;
    }

    public void setCreationTime(long creationTime) {
        if (creationTime >= 0) {
            this.mCreationTime = creationTime;
            return;
        }
        throw new IllegalArgumentException("creationTime must be non-negative.");
    }

    public int getCopies() {
        return this.mCopies;
    }

    public void setCopies(int copyCount) {
        if (copyCount >= 1) {
            this.mCopies = copyCount;
            return;
        }
        throw new IllegalArgumentException("Copies must be more than one.");
    }

    public PageRange[] getPages() {
        return this.mPageRanges;
    }

    public void setPages(PageRange[] pageRanges) {
        this.mPageRanges = pageRanges;
    }

    public PrintAttributes getAttributes() {
        return this.mAttributes;
    }

    public void setAttributes(PrintAttributes attributes) {
        this.mAttributes = attributes;
    }

    public PrintDocumentInfo getDocumentInfo() {
        return this.mDocumentInfo;
    }

    public void setDocumentInfo(PrintDocumentInfo info) {
        this.mDocumentInfo = info;
    }

    public boolean isCancelling() {
        return this.mCanceling;
    }

    public void setCancelling(boolean cancelling) {
        this.mCanceling = cancelling;
    }

    public boolean hasAdvancedOption(String key) {
        return this.mAdvancedOptions != null && this.mAdvancedOptions.containsKey(key);
    }

    public String getAdvancedStringOption(String key) {
        if (this.mAdvancedOptions != null) {
            return this.mAdvancedOptions.getString(key);
        }
        return null;
    }

    public int getAdvancedIntOption(String key) {
        if (this.mAdvancedOptions != null) {
            return this.mAdvancedOptions.getInt(key);
        }
        return 0;
    }

    public Bundle getAdvancedOptions() {
        return this.mAdvancedOptions;
    }

    public void setAdvancedOptions(Bundle options) {
        this.mAdvancedOptions = options;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mId, flags);
        parcel.writeString(this.mLabel);
        parcel.writeParcelable(this.mPrinterId, flags);
        parcel.writeString(this.mPrinterName);
        parcel.writeInt(this.mState);
        parcel.writeInt(this.mAppId);
        parcel.writeString(this.mTag);
        parcel.writeLong(this.mCreationTime);
        parcel.writeInt(this.mCopies);
        parcel.writeParcelableArray(this.mPageRanges, flags);
        parcel.writeParcelable(this.mAttributes, flags);
        parcel.writeParcelable(this.mDocumentInfo, 0);
        parcel.writeFloat(this.mProgress);
        parcel.writeCharSequence(this.mStatus);
        parcel.writeInt(this.mStatusRes);
        parcel.writeCharSequence(this.mStatusResAppPackageName);
        parcel.writeInt(this.mCanceling ? 1 : 0);
        parcel.writeBundle(this.mAdvancedOptions);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PrintJobInfo{");
        builder.append("label: ");
        builder.append(this.mLabel);
        builder.append(", id: ");
        builder.append(this.mId);
        builder.append(", state: ");
        builder.append(stateToString(this.mState));
        builder.append(", printer: " + this.mPrinterId);
        builder.append(", tag: ");
        builder.append(this.mTag);
        builder.append(", creationTime: " + this.mCreationTime);
        builder.append(", copies: ");
        builder.append(this.mCopies);
        StringBuilder sb = new StringBuilder();
        sb.append(", attributes: ");
        String str = null;
        sb.append(this.mAttributes != null ? this.mAttributes.toString() : null);
        builder.append(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(", documentInfo: ");
        sb2.append(this.mDocumentInfo != null ? this.mDocumentInfo.toString() : null);
        builder.append(sb2.toString());
        builder.append(", cancelling: " + this.mCanceling);
        StringBuilder sb3 = new StringBuilder();
        sb3.append(", pages: ");
        sb3.append(this.mPageRanges != null ? Arrays.toString(this.mPageRanges) : null);
        builder.append(sb3.toString());
        StringBuilder sb4 = new StringBuilder();
        sb4.append(", hasAdvancedOptions: ");
        sb4.append(this.mAdvancedOptions != null);
        builder.append(sb4.toString());
        builder.append(", progress: " + this.mProgress);
        StringBuilder sb5 = new StringBuilder();
        sb5.append(", status: ");
        sb5.append(this.mStatus != null ? this.mStatus.toString() : null);
        builder.append(sb5.toString());
        builder.append(", statusRes: " + this.mStatusRes);
        StringBuilder sb6 = new StringBuilder();
        sb6.append(", statusResAppPackageName: ");
        if (this.mStatusResAppPackageName != null) {
            str = this.mStatusResAppPackageName.toString();
        }
        sb6.append(str);
        builder.append(sb6.toString());
        builder.append("}");
        return builder.toString();
    }

    public static String stateToString(int state) {
        switch (state) {
            case 1:
                return "STATE_CREATED";
            case 2:
                return "STATE_QUEUED";
            case 3:
                return "STATE_STARTED";
            case 4:
                return "STATE_BLOCKED";
            case 5:
                return "STATE_COMPLETED";
            case 6:
                return "STATE_FAILED";
            case 7:
                return "STATE_CANCELED";
            default:
                return "STATE_UNKNOWN";
        }
    }

    public float getProgress() {
        return this.mProgress;
    }

    public CharSequence getStatus(PackageManager pm) {
        if (this.mStatusRes == 0) {
            return this.mStatus;
        }
        try {
            return pm.getResourcesForApplication(this.mStatusResAppPackageName.toString()).getString(this.mStatusRes);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            return null;
        }
    }
}
