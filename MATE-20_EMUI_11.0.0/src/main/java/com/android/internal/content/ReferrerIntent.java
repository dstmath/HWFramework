package com.android.internal.content;

import android.annotation.UnsupportedAppUsage;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class ReferrerIntent extends Intent {
    public static final Parcelable.Creator<ReferrerIntent> CREATOR = new Parcelable.Creator<ReferrerIntent>() {
        /* class com.android.internal.content.ReferrerIntent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ReferrerIntent createFromParcel(Parcel source) {
            return new ReferrerIntent(source);
        }

        @Override // android.os.Parcelable.Creator
        public ReferrerIntent[] newArray(int size) {
            return new ReferrerIntent[size];
        }
    };
    @UnsupportedAppUsage
    public final String mReferrer;

    @UnsupportedAppUsage
    public ReferrerIntent(Intent baseIntent, String referrer) {
        super(baseIntent);
        this.mReferrer = referrer;
    }

    @Override // android.content.Intent, android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(this.mReferrer);
    }

    ReferrerIntent(Parcel in) {
        readFromParcel(in);
        this.mReferrer = in.readString();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ReferrerIntent)) {
            return false;
        }
        ReferrerIntent other = (ReferrerIntent) obj;
        if (!filterEquals(other) || !Objects.equals(this.mReferrer, other.mReferrer)) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (((17 * 31) + filterHashCode()) * 31) + Objects.hashCode(this.mReferrer);
    }
}
