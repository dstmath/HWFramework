package android.service.settings.suggestions;

import android.annotation.SystemApi;
import android.app.PendingIntent;
import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class Suggestion implements Parcelable {
    public static final Parcelable.Creator<Suggestion> CREATOR = new Parcelable.Creator<Suggestion>() {
        public Suggestion createFromParcel(Parcel in) {
            return new Suggestion(in);
        }

        public Suggestion[] newArray(int size) {
            return new Suggestion[size];
        }
    };
    public static final int FLAG_HAS_BUTTON = 1;
    public static final int FLAG_ICON_TINTABLE = 2;
    private final int mFlags;
    private final Icon mIcon;
    private final String mId;
    private final PendingIntent mPendingIntent;
    private final CharSequence mSummary;
    private final CharSequence mTitle;

    public static class Builder {
        /* access modifiers changed from: private */
        public int mFlags;
        /* access modifiers changed from: private */
        public Icon mIcon;
        /* access modifiers changed from: private */
        public final String mId;
        /* access modifiers changed from: private */
        public PendingIntent mPendingIntent;
        /* access modifiers changed from: private */
        public CharSequence mSummary;
        /* access modifiers changed from: private */
        public CharSequence mTitle;

        public Builder(String id) {
            if (!TextUtils.isEmpty(id)) {
                this.mId = id;
                return;
            }
            throw new IllegalArgumentException("Suggestion id cannot be empty");
        }

        public Builder setTitle(CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Builder setSummary(CharSequence summary) {
            this.mSummary = summary;
            return this;
        }

        public Builder setIcon(Icon icon) {
            this.mIcon = icon;
            return this;
        }

        public Builder setFlags(int flags) {
            this.mFlags = flags;
            return this;
        }

        public Builder setPendingIntent(PendingIntent pendingIntent) {
            this.mPendingIntent = pendingIntent;
            return this;
        }

        public Suggestion build() {
            return new Suggestion(this);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Flags {
    }

    public String getId() {
        return this.mId;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public PendingIntent getPendingIntent() {
        return this.mPendingIntent;
    }

    private Suggestion(Builder builder) {
        this.mId = builder.mId;
        this.mTitle = builder.mTitle;
        this.mSummary = builder.mSummary;
        this.mIcon = builder.mIcon;
        this.mFlags = builder.mFlags;
        this.mPendingIntent = builder.mPendingIntent;
    }

    private Suggestion(Parcel in) {
        this.mId = in.readString();
        this.mTitle = in.readCharSequence();
        this.mSummary = in.readCharSequence();
        this.mIcon = (Icon) in.readParcelable(Icon.class.getClassLoader());
        this.mFlags = in.readInt();
        this.mPendingIntent = (PendingIntent) in.readParcelable(PendingIntent.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeCharSequence(this.mTitle);
        dest.writeCharSequence(this.mSummary);
        dest.writeParcelable(this.mIcon, flags);
        dest.writeInt(this.mFlags);
        dest.writeParcelable(this.mPendingIntent, flags);
    }
}
