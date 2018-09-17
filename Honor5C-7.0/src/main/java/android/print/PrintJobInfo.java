package android.print;

import android.app.Notification;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech.Engine;
import com.android.internal.util.Preconditions;
import java.util.Arrays;

public final class PrintJobInfo implements Parcelable {
    public static final Creator<PrintJobInfo> CREATOR = null;
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
    private Bundle mAdvancedOptions;
    private int mAppId;
    private PrintAttributes mAttributes;
    private boolean mCanceling;
    private int mCopies;
    private long mCreationTime;
    private PrintDocumentInfo mDocumentInfo;
    private PrintJobId mId;
    private String mLabel;
    private PageRange[] mPageRanges;
    private PrinterId mPrinterId;
    private String mPrinterName;
    private float mProgress;
    private int mState;
    private CharSequence mStatus;
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
            this.mPrototype.mCopies = copies;
        }

        public void setAttributes(PrintAttributes attributes) {
            this.mPrototype.mAttributes = attributes;
        }

        public void setPages(PageRange[] pages) {
            this.mPrototype.mPageRanges = pages;
        }

        public void setProgress(float progress) {
            Preconditions.checkArgumentInRange(progress, 0.0f, Engine.DEFAULT_VOLUME, Notification.CATEGORY_PROGRESS);
            this.mPrototype.mProgress = progress;
        }

        public void setStatus(CharSequence status) {
            this.mPrototype.mStatus = status;
        }

        public void putAdvancedOption(String key, String value) {
            Preconditions.checkNotNull(key, "key cannot be null");
            if (this.mPrototype.mAdvancedOptions == null) {
                this.mPrototype.mAdvancedOptions = new Bundle();
            }
            this.mPrototype.mAdvancedOptions.putString(key, value);
        }

        public void putAdvancedOption(String key, int value) {
            if (this.mPrototype.mAdvancedOptions == null) {
                this.mPrototype.mAdvancedOptions = new Bundle();
            }
            this.mPrototype.mAdvancedOptions.putInt(key, value);
        }

        public PrintJobInfo build() {
            return this.mPrototype;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.print.PrintJobInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.print.PrintJobInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.print.PrintJobInfo.<clinit>():void");
    }

    public PrintJobInfo() {
        this.mProgress = ScaledLayoutParams.SCALE_UNSPECIFIED;
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
        boolean z = false;
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
        if (parcelables != null) {
            this.mPageRanges = new PageRange[parcelables.length];
            for (int i = 0; i < parcelables.length; i += STATE_CREATED) {
                this.mPageRanges[i] = (PageRange) parcelables[i];
            }
        }
        this.mAttributes = (PrintAttributes) parcel.readParcelable(null);
        this.mDocumentInfo = (PrintDocumentInfo) parcel.readParcelable(null);
        this.mProgress = parcel.readFloat();
        this.mStatus = parcel.readCharSequence();
        this.mStatusRes = parcel.readInt();
        this.mStatusResAppPackageName = parcel.readCharSequence();
        this.mCanceling = parcel.readInt() == STATE_CREATED;
        this.mAdvancedOptions = parcel.readBundle();
        if (this.mAdvancedOptions != null) {
            if (!this.mAdvancedOptions.containsKey(null)) {
                z = true;
            }
            Preconditions.checkArgument(z);
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
        Preconditions.checkArgumentInRange(progress, 0.0f, Engine.DEFAULT_VOLUME, Notification.CATEGORY_PROGRESS);
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
        if (creationTime < 0) {
            throw new IllegalArgumentException("creationTime must be non-negative.");
        }
        this.mCreationTime = creationTime;
    }

    public int getCopies() {
        return this.mCopies;
    }

    public void setCopies(int copyCount) {
        if (copyCount < STATE_CREATED) {
            throw new IllegalArgumentException("Copies must be more than one.");
        }
        this.mCopies = copyCount;
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
        return this.mAdvancedOptions != null ? this.mAdvancedOptions.containsKey(key) : false;
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
        int i = 0;
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
        if (this.mCanceling) {
            i = STATE_CREATED;
        }
        parcel.writeInt(i);
        parcel.writeBundle(this.mAdvancedOptions);
    }

    public String toString() {
        String printDocumentInfo;
        String str = null;
        StringBuilder builder = new StringBuilder();
        builder.append("PrintJobInfo{");
        builder.append("label: ").append(this.mLabel);
        builder.append(", id: ").append(this.mId);
        builder.append(", state: ").append(stateToString(this.mState));
        builder.append(", printer: ").append(this.mPrinterId);
        builder.append(", tag: ").append(this.mTag);
        builder.append(", creationTime: ").append(this.mCreationTime);
        builder.append(", copies: ").append(this.mCopies);
        builder.append(", attributes: ").append(this.mAttributes != null ? this.mAttributes.toString() : null);
        StringBuilder append = builder.append(", documentInfo: ");
        if (this.mDocumentInfo != null) {
            printDocumentInfo = this.mDocumentInfo.toString();
        } else {
            printDocumentInfo = null;
        }
        append.append(printDocumentInfo);
        builder.append(", cancelling: ").append(this.mCanceling);
        append = builder.append(", pages: ");
        if (this.mPageRanges != null) {
            printDocumentInfo = Arrays.toString(this.mPageRanges);
        } else {
            printDocumentInfo = null;
        }
        append.append(printDocumentInfo);
        builder.append(", hasAdvancedOptions: ").append(this.mAdvancedOptions != null);
        builder.append(", progress: ").append(this.mProgress);
        append = builder.append(", status: ");
        if (this.mStatus != null) {
            printDocumentInfo = this.mStatus.toString();
        } else {
            printDocumentInfo = null;
        }
        append.append(printDocumentInfo);
        builder.append(", statusRes: ").append(this.mStatusRes);
        StringBuilder append2 = builder.append(", statusResAppPackageName: ");
        if (this.mStatusResAppPackageName != null) {
            str = this.mStatusResAppPackageName.toString();
        }
        append2.append(str);
        builder.append("}");
        return builder.toString();
    }

    public static String stateToString(int state) {
        switch (state) {
            case STATE_CREATED /*1*/:
                return "STATE_CREATED";
            case STATE_QUEUED /*2*/:
                return "STATE_QUEUED";
            case STATE_STARTED /*3*/:
                return "STATE_STARTED";
            case STATE_BLOCKED /*4*/:
                return "STATE_BLOCKED";
            case STATE_COMPLETED /*5*/:
                return "STATE_COMPLETED";
            case STATE_FAILED /*6*/:
                return "STATE_FAILED";
            case STATE_CANCELED /*7*/:
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
        } catch (NameNotFoundException e) {
            return null;
        }
    }
}
