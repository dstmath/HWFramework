package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TextMessage implements Parcelable {
    public static final Creator<TextMessage> CREATOR = new Creator<TextMessage>() {
        public TextMessage createFromParcel(Parcel in) {
            return new TextMessage(in, null);
        }

        public TextMessage[] newArray(int size) {
            return new TextMessage[size];
        }
    };
    public Duration duration;
    public Bitmap icon;
    public boolean iconSelfExplanatory;
    public boolean isHighPriority;
    public boolean responseNeeded;
    public String text;
    public String title;
    public boolean userClear;

    /* synthetic */ TextMessage(Parcel in, TextMessage -this1) {
        this(in);
    }

    TextMessage() {
        this.title = "";
        this.text = null;
        this.icon = null;
        this.iconSelfExplanatory = false;
        this.isHighPriority = false;
        this.responseNeeded = true;
        this.userClear = false;
        this.duration = null;
    }

    private TextMessage(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.title = "";
        this.text = null;
        this.icon = null;
        this.iconSelfExplanatory = false;
        this.isHighPriority = false;
        this.responseNeeded = true;
        this.userClear = false;
        this.duration = null;
        this.title = in.readString();
        this.text = in.readString();
        this.icon = (Bitmap) in.readParcelable(null);
        this.iconSelfExplanatory = in.readInt() == 1;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isHighPriority = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.responseNeeded = z;
        if (in.readInt() != 1) {
            z2 = false;
        }
        this.userClear = z2;
        this.duration = (Duration) in.readParcelable(null);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.title);
        dest.writeString(this.text);
        dest.writeParcelable(this.icon, 0);
        dest.writeInt(this.iconSelfExplanatory ? 1 : 0);
        if (this.isHighPriority) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.responseNeeded) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.userClear) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeParcelable(this.duration, 0);
    }

    public String toString() {
        return "title=" + this.title + " text=" + this.text + " icon=" + this.icon + " iconSelfExplanatory=" + this.iconSelfExplanatory + " isHighPriority=" + this.isHighPriority + " responseNeeded=" + this.responseNeeded + " userClear=" + this.userClear + " duration=" + this.duration;
    }
}
