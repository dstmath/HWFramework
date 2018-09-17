package com.android.internal.content;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable.Creator;

public class ReferrerIntent extends Intent {
    public static final Creator<ReferrerIntent> CREATOR = new Creator<ReferrerIntent>() {
        public ReferrerIntent createFromParcel(Parcel source) {
            return new ReferrerIntent(source);
        }

        public ReferrerIntent[] newArray(int size) {
            return new ReferrerIntent[size];
        }
    };
    public final String mReferrer;

    public ReferrerIntent(Intent baseIntent, String referrer) {
        super(baseIntent);
        this.mReferrer = referrer;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(this.mReferrer);
    }

    ReferrerIntent(Parcel in) {
        readFromParcel(in);
        this.mReferrer = in.readString();
    }
}
