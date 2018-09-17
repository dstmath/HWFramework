package android.service.notification;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class Adjustment implements Parcelable {
    public static final Creator<Adjustment> CREATOR = new Creator<Adjustment>() {
        public Adjustment createFromParcel(Parcel in) {
            return new Adjustment(in);
        }

        public Adjustment[] newArray(int size) {
            return new Adjustment[size];
        }
    };
    public static final String KEY_PEOPLE = "key_people";
    public static final String KEY_SNOOZE_CRITERIA = "key_snooze_criteria";
    private final CharSequence mExplanation;
    private final String mKey;
    private final String mPackage;
    private final Bundle mSignals;
    private final int mUser;

    public Adjustment(String pkg, String key, Bundle signals, CharSequence explanation, int user) {
        this.mPackage = pkg;
        this.mKey = key;
        this.mSignals = signals;
        this.mExplanation = explanation;
        this.mUser = user;
    }

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

    public int getUser() {
        return this.mUser;
    }

    public int describeContents() {
        return 0;
    }

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
    }
}
