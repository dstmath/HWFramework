package android.telecom;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Voicemail implements Parcelable {
    public static final Creator<Voicemail> CREATOR = new Creator<Voicemail>() {
        public Voicemail createFromParcel(Parcel in) {
            return new Voicemail(in, null);
        }

        public Voicemail[] newArray(int size) {
            return new Voicemail[size];
        }
    };
    private final Long mDuration;
    private final Boolean mHasContent;
    private final Long mId;
    private final Boolean mIsRead;
    private final String mNumber;
    private final PhoneAccountHandle mPhoneAccount;
    private final String mProviderData;
    private final String mSource;
    private final Long mTimestamp;
    private final String mTranscription;
    private final Uri mUri;

    public static class Builder {
        private Long mBuilderDuration;
        private boolean mBuilderHasContent;
        private Long mBuilderId;
        private Boolean mBuilderIsRead;
        private String mBuilderNumber;
        private PhoneAccountHandle mBuilderPhoneAccount;
        private String mBuilderSourceData;
        private String mBuilderSourcePackage;
        private Long mBuilderTimestamp;
        private String mBuilderTranscription;
        private Uri mBuilderUri;

        /* synthetic */ Builder(Builder -this0) {
            this();
        }

        private Builder() {
        }

        public Builder setNumber(String number) {
            this.mBuilderNumber = number;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            this.mBuilderTimestamp = Long.valueOf(timestamp);
            return this;
        }

        public Builder setPhoneAccount(PhoneAccountHandle phoneAccount) {
            this.mBuilderPhoneAccount = phoneAccount;
            return this;
        }

        public Builder setId(long id) {
            this.mBuilderId = Long.valueOf(id);
            return this;
        }

        public Builder setDuration(long duration) {
            this.mBuilderDuration = Long.valueOf(duration);
            return this;
        }

        public Builder setSourcePackage(String sourcePackage) {
            this.mBuilderSourcePackage = sourcePackage;
            return this;
        }

        public Builder setSourceData(String sourceData) {
            this.mBuilderSourceData = sourceData;
            return this;
        }

        public Builder setUri(Uri uri) {
            this.mBuilderUri = uri;
            return this;
        }

        public Builder setIsRead(boolean isRead) {
            this.mBuilderIsRead = Boolean.valueOf(isRead);
            return this;
        }

        public Builder setHasContent(boolean hasContent) {
            this.mBuilderHasContent = hasContent;
            return this;
        }

        public Builder setTranscription(String transcription) {
            this.mBuilderTranscription = transcription;
            return this;
        }

        public Voicemail build() {
            long j = 0;
            this.mBuilderId = Long.valueOf(this.mBuilderId == null ? -1 : this.mBuilderId.longValue());
            this.mBuilderTimestamp = Long.valueOf(this.mBuilderTimestamp == null ? 0 : this.mBuilderTimestamp.longValue());
            if (this.mBuilderDuration != null) {
                j = this.mBuilderDuration.longValue();
            }
            this.mBuilderDuration = Long.valueOf(j);
            this.mBuilderIsRead = Boolean.valueOf(this.mBuilderIsRead == null ? false : this.mBuilderIsRead.booleanValue());
            return new Voicemail(this.mBuilderTimestamp, this.mBuilderNumber, this.mBuilderPhoneAccount, this.mBuilderId, this.mBuilderDuration, this.mBuilderSourcePackage, this.mBuilderSourceData, this.mBuilderUri, this.mBuilderIsRead, Boolean.valueOf(this.mBuilderHasContent), this.mBuilderTranscription, null);
        }
    }

    /* synthetic */ Voicemail(Parcel in, Voicemail -this1) {
        this(in);
    }

    /* synthetic */ Voicemail(Long timestamp, String number, PhoneAccountHandle phoneAccountHandle, Long id, Long duration, String source, String providerData, Uri uri, Boolean isRead, Boolean hasContent, String transcription, Voicemail -this11) {
        this(timestamp, number, phoneAccountHandle, id, duration, source, providerData, uri, isRead, hasContent, transcription);
    }

    private Voicemail(Long timestamp, String number, PhoneAccountHandle phoneAccountHandle, Long id, Long duration, String source, String providerData, Uri uri, Boolean isRead, Boolean hasContent, String transcription) {
        this.mTimestamp = timestamp;
        this.mNumber = number;
        this.mPhoneAccount = phoneAccountHandle;
        this.mId = id;
        this.mDuration = duration;
        this.mSource = source;
        this.mProviderData = providerData;
        this.mUri = uri;
        this.mIsRead = isRead;
        this.mHasContent = hasContent;
        this.mTranscription = transcription;
    }

    public static Builder createForInsertion(long timestamp, String number) {
        return new Builder().setNumber(number).setTimestamp(timestamp);
    }

    public static Builder createForUpdate(long id, String sourceData) {
        return new Builder().setId(id).setSourceData(sourceData);
    }

    public long getId() {
        return this.mId.longValue();
    }

    public String getNumber() {
        return this.mNumber;
    }

    public PhoneAccountHandle getPhoneAccount() {
        return this.mPhoneAccount;
    }

    public long getTimestampMillis() {
        return this.mTimestamp.longValue();
    }

    public long getDuration() {
        return this.mDuration.longValue();
    }

    public String getSourcePackage() {
        return this.mSource;
    }

    public String getSourceData() {
        return this.mProviderData;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public boolean isRead() {
        return this.mIsRead.booleanValue();
    }

    public boolean hasContent() {
        return this.mHasContent.booleanValue();
    }

    public String getTranscription() {
        return this.mTranscription;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mTimestamp.longValue());
        dest.writeCharSequence(this.mNumber);
        if (this.mPhoneAccount == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.mPhoneAccount.writeToParcel(dest, flags);
        }
        dest.writeLong(this.mId.longValue());
        dest.writeLong(this.mDuration.longValue());
        dest.writeCharSequence(this.mSource);
        dest.writeCharSequence(this.mProviderData);
        if (this.mUri == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.mUri.writeToParcel(dest, flags);
        }
        if (this.mIsRead.booleanValue()) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        if (this.mHasContent.booleanValue()) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeCharSequence(this.mTranscription);
    }

    private Voicemail(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mTimestamp = Long.valueOf(in.readLong());
        this.mNumber = (String) in.readCharSequence();
        if (in.readInt() > 0) {
            this.mPhoneAccount = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(in);
        } else {
            this.mPhoneAccount = null;
        }
        this.mId = Long.valueOf(in.readLong());
        this.mDuration = Long.valueOf(in.readLong());
        this.mSource = (String) in.readCharSequence();
        this.mProviderData = (String) in.readCharSequence();
        if (in.readInt() > 0) {
            this.mUri = (Uri) Uri.CREATOR.createFromParcel(in);
        } else {
            this.mUri = null;
        }
        if (in.readInt() > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsRead = Boolean.valueOf(z);
        if (in.readInt() <= 0) {
            z2 = false;
        }
        this.mHasContent = Boolean.valueOf(z2);
        this.mTranscription = (String) in.readCharSequence();
    }
}
