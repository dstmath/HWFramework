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
        /* class android.service.settings.suggestions.Suggestion.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Suggestion createFromParcel(Parcel in) {
            return new Suggestion(in);
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeCharSequence(this.mTitle);
        dest.writeCharSequence(this.mSummary);
        dest.writeParcelable(this.mIcon, flags);
        dest.writeInt(this.mFlags);
        dest.writeParcelable(this.mPendingIntent, flags);
    }

    public static class Builder {
        private int mFlags;
        private Icon mIcon;
        private final String mId;
        private PendingIntent mPendingIntent;
        private CharSequence mSummary;
        private CharSequence mTitle;

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
}
