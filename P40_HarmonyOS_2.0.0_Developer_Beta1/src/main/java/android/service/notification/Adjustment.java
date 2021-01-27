package android.service.notification;

import android.annotation.SystemApi;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class Adjustment implements Parcelable {
    public static final Parcelable.Creator<Adjustment> CREATOR = new Parcelable.Creator<Adjustment>() {
        /* class android.service.notification.Adjustment.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Adjustment createFromParcel(Parcel in) {
            return new Adjustment(in);
        }

        @Override // android.os.Parcelable.Creator
        public Adjustment[] newArray(int size) {
            return new Adjustment[size];
        }
    };
    public static final String KEY_CONTEXTUAL_ACTIONS = "key_contextual_actions";
    public static final String KEY_GROUP_KEY = "key_group_key";
    public static final String KEY_IMPORTANCE = "key_importance";
    @SystemApi
    public static final String KEY_PEOPLE = "key_people";
    public static final String KEY_SNOOZE_CRITERIA = "key_snooze_criteria";
    public static final String KEY_TEXT_REPLIES = "key_text_replies";
    public static final String KEY_USER_SENTIMENT = "key_user_sentiment";
    private final CharSequence mExplanation;
    private String mIssuer;
    private final String mKey;
    private final String mPackage;
    private final Bundle mSignals;
    private final int mUser;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Keys {
    }

    @SystemApi
    public Adjustment(String pkg, String key, Bundle signals, CharSequence explanation, int user) {
        this.mPackage = pkg;
        this.mKey = key;
        this.mSignals = signals;
        this.mExplanation = explanation;
        this.mUser = user;
    }

    public Adjustment(String pkg, String key, Bundle signals, CharSequence explanation, UserHandle userHandle) {
        this.mPackage = pkg;
        this.mKey = key;
        this.mSignals = signals;
        this.mExplanation = explanation;
        this.mUser = userHandle.getIdentifier();
    }

    @SystemApi
    protected Adjustment(Parcel in) {
        if (in.readInt() == 1) {
            this.mPackage = in.readString();
        } else {
            this.mPackage = null;
        }
        if (in.readInt() == 1) {
            this.mKey = in.readString();
        } else {
            this.mKey = null;
        }
        if (in.readInt() == 1) {
            this.mExplanation = in.readCharSequence();
        } else {
            this.mExplanation = null;
        }
        this.mSignals = in.readBundle();
        this.mUser = in.readInt();
        this.mIssuer = in.readString();
    }

    public String getPackage() {
        return this.mPackage;
    }

    public String getKey() {
        return this.mKey;
    }

    public CharSequence getExplanation() {
        return this.mExplanation;
    }

    public Bundle getSignals() {
        return this.mSignals;
    }

    @SystemApi
    public int getUser() {
        return this.mUser;
    }

    public UserHandle getUserHandle() {
        return UserHandle.of(this.mUser);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (this.mPackage != null) {
            dest.writeInt(1);
            dest.writeString(this.mPackage);
        } else {
            dest.writeInt(0);
        }
        if (this.mKey != null) {
            dest.writeInt(1);
            dest.writeString(this.mKey);
        } else {
            dest.writeInt(0);
        }
        if (this.mExplanation != null) {
            dest.writeInt(1);
            dest.writeCharSequence(this.mExplanation);
        } else {
            dest.writeInt(0);
        }
        dest.writeBundle(this.mSignals);
        dest.writeInt(this.mUser);
        dest.writeString(this.mIssuer);
    }

    public String toString() {
        return "Adjustment{mSignals=" + this.mSignals + '}';
    }

    public void setIssuer(String issuer) {
        this.mIssuer = issuer;
    }

    public String getIssuer() {
        return this.mIssuer;
    }
}
