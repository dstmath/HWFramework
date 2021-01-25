package android.service.notification;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class NotificationStats implements Parcelable {
    public static final Parcelable.Creator<NotificationStats> CREATOR = new Parcelable.Creator<NotificationStats>() {
        /* class android.service.notification.NotificationStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NotificationStats createFromParcel(Parcel in) {
            return new NotificationStats(in);
        }

        @Override // android.os.Parcelable.Creator
        public NotificationStats[] newArray(int size) {
            return new NotificationStats[size];
        }
    };
    public static final int DISMISSAL_AOD = 2;
    public static final int DISMISSAL_NOT_DISMISSED = -1;
    public static final int DISMISSAL_OTHER = 0;
    public static final int DISMISSAL_PEEK = 1;
    public static final int DISMISSAL_SHADE = 3;
    public static final int DISMISS_SENTIMENT_NEGATIVE = 0;
    public static final int DISMISS_SENTIMENT_NEUTRAL = 1;
    public static final int DISMISS_SENTIMENT_POSITIVE = 2;
    public static final int DISMISS_SENTIMENT_UNKNOWN = -1000;
    private boolean mDirectReplied;
    private int mDismissalSentiment = -1000;
    private int mDismissalSurface = -1;
    private boolean mExpanded;
    private boolean mInteracted;
    private boolean mSeen;
    private boolean mSnoozed;
    private boolean mViewedSettings;

    @Retention(RetentionPolicy.SOURCE)
    public @interface DismissalSentiment {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DismissalSurface {
    }

    public NotificationStats() {
    }

    @SystemApi
    protected NotificationStats(Parcel in) {
        boolean z = true;
        this.mSeen = in.readByte() != 0;
        this.mExpanded = in.readByte() != 0;
        this.mDirectReplied = in.readByte() != 0;
        this.mSnoozed = in.readByte() != 0;
        this.mViewedSettings = in.readByte() != 0;
        this.mInteracted = in.readByte() == 0 ? false : z;
        this.mDismissalSurface = in.readInt();
        this.mDismissalSentiment = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mSeen ? (byte) 1 : 0);
        dest.writeByte(this.mExpanded ? (byte) 1 : 0);
        dest.writeByte(this.mDirectReplied ? (byte) 1 : 0);
        dest.writeByte(this.mSnoozed ? (byte) 1 : 0);
        dest.writeByte(this.mViewedSettings ? (byte) 1 : 0);
        dest.writeByte(this.mInteracted ? (byte) 1 : 0);
        dest.writeInt(this.mDismissalSurface);
        dest.writeInt(this.mDismissalSentiment);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public boolean hasSeen() {
        return this.mSeen;
    }

    public void setSeen() {
        this.mSeen = true;
    }

    public boolean hasExpanded() {
        return this.mExpanded;
    }

    public void setExpanded() {
        this.mExpanded = true;
        this.mInteracted = true;
    }

    public boolean hasDirectReplied() {
        return this.mDirectReplied;
    }

    public void setDirectReplied() {
        this.mDirectReplied = true;
        this.mInteracted = true;
    }

    public boolean hasSnoozed() {
        return this.mSnoozed;
    }

    public void setSnoozed() {
        this.mSnoozed = true;
        this.mInteracted = true;
    }

    public boolean hasViewedSettings() {
        return this.mViewedSettings;
    }

    public void setViewedSettings() {
        this.mViewedSettings = true;
        this.mInteracted = true;
    }

    public boolean hasInteracted() {
        return this.mInteracted;
    }

    public int getDismissalSurface() {
        return this.mDismissalSurface;
    }

    public void setDismissalSurface(int dismissalSurface) {
        this.mDismissalSurface = dismissalSurface;
    }

    public void setDismissalSentiment(int dismissalSentiment) {
        this.mDismissalSentiment = dismissalSentiment;
    }

    public int getDismissalSentiment() {
        return this.mDismissalSentiment;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationStats that = (NotificationStats) o;
        if (this.mSeen != that.mSeen || this.mExpanded != that.mExpanded || this.mDirectReplied != that.mDirectReplied || this.mSnoozed != that.mSnoozed || this.mViewedSettings != that.mViewedSettings || this.mInteracted != that.mInteracted) {
            return false;
        }
        if (this.mDismissalSurface == that.mDismissalSurface) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((((((((((((this.mSeen ? 1 : 0) * 31) + (this.mExpanded ? 1 : 0)) * 31) + (this.mDirectReplied ? 1 : 0)) * 31) + (this.mSnoozed ? 1 : 0)) * 31) + (this.mViewedSettings ? 1 : 0)) * 31) + (this.mInteracted ? 1 : 0)) * 31) + this.mDismissalSurface;
    }

    public String toString() {
        return "NotificationStats{mSeen=" + this.mSeen + ", mExpanded=" + this.mExpanded + ", mDirectReplied=" + this.mDirectReplied + ", mSnoozed=" + this.mSnoozed + ", mViewedSettings=" + this.mViewedSettings + ", mInteracted=" + this.mInteracted + ", mDismissalSurface=" + this.mDismissalSurface + '}';
    }
}
